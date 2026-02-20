package com.naicolasdev.tiktokdownloader

import androidx.compose.ui.res.painterResource
import com.naicolasdev.tiktokdownloader.R

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.content.ContentValues
import android.widget.Toast
import android.os.Build
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.naicolasdev.tiktokdownloader.ui.components.AmbientGlowBackground
import com.naicolasdev.tiktokdownloader.ui.components.GlassPanel
import com.naicolasdev.tiktokdownloader.ui.components.HtmlInput
import com.naicolasdev.tiktokdownloader.ui.components.PrimaryButton
import com.naicolasdev.tiktokdownloader.ui.home.ResultCard
import com.naicolasdev.tiktokdownloader.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit

// ==================== API and ViewModel ====================

interface TikWmApiService {
    @FormUrlEncoded
    @POST("api/")
    suspend fun getVideoInfo(
        @Field("url") url: String,
        @Field("hd") hd: Int = 1
    ): TikWmResponse
}

interface RapidApiService {
    @retrofit2.http.GET("download")
    suspend fun downloadMedia(
        @retrofit2.http.Header("X-RapidAPI-Key") apiKey: String,
        @retrofit2.http.Header("X-RapidAPI-Host") apiHost: String = "instagram-reels-downloader-api.p.rapidapi.com",
        @retrofit2.http.Query("url") url: String
    ): RapidApiResponse
}

interface TwitterApiService {
    @retrofit2.http.GET("api/v1/x-media/info")
    suspend fun downloadMedia(
        @retrofit2.http.Header("x-rapidapi-key") apiKey: String,
        @retrofit2.http.Header("x-rapidapi-host") apiHost: String = "youtube-video-audio-downloader.p.rapidapi.com",
        @retrofit2.http.Query("url") url: String
    ): TwitterApiResponse
}

object RetrofitClients {
    private const val TIKWM_BASE_URL = "https://www.tikwm.com/"
    
    // Base URL da RapidAPI (EaseAPI default route - Instagram)
    private const val RAPIDAPI_BASE_URL = "https://instagram-reels-downloader-api.p.rapidapi.com/" 

    // Base URL da RapidAPI (Beatom - Twitter)
    private const val TWITTER_API_BASE_URL = "https://youtube-video-audio-downloader.p.rapidapi.com/"

    val tikWmApi: TikWmApiService by lazy {
        Retrofit.Builder()
            .baseUrl(TIKWM_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TikWmApiService::class.java)
    }

    val rapidApi: RapidApiService by lazy {
        Retrofit.Builder()
            .baseUrl(RAPIDAPI_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RapidApiService::class.java)
    }

    val twitterApi: TwitterApiService by lazy {
        Retrofit.Builder()
            .baseUrl(TWITTER_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TwitterApiService::class.java)
    }
}

sealed class UiState {
    data object Idle : UiState()
    data object Loading : UiState()
    data class Success(val data: SocialMediaData) : UiState()
    data class Error(val message: String) : UiState()
}

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    fun updateUrl(url: String) {
        _urlInput.value = url
    }

    /**
     * Processa uma URL compartilhada de outro app.
     * Atualiza o campo de input e dispara automaticamente o fetch.
     */
    fun processSharedUrl(url: String) {
        _urlInput.value = url
        fetchVideoInfo()
    }

    fun fetchVideoInfo() {
        val url = _urlInput.value.trim()
        if (url.isEmpty()) {
            _uiState.value = UiState.Error("Por favor, cole um link válido.")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                if (url.contains("tiktok.com")) {
                    // Usar TikWm para TikTok
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClients.tikWmApi.getVideoInfo(url)
                    }
                    if (response.code == 0 && response.data != null) {
                        val media = SocialMediaData(
                            title = response.data.title,
                            coverUrl = response.data.cover,
                            videoUrl = response.data.play,
                            audioUrl = response.data.music
                        )
                        _uiState.value = UiState.Success(media)
                    } else {
                        _uiState.value = UiState.Error(response.msg.ifEmpty { "Vídeo não encontrado." })
                    }
                } else if (url.contains("instagram.com")) {
                    // Usar RapidAPI (EaseAPI) para Instagram
                    
                    // IMPORTANTE: Insira sua X-RapidAPI-Key aqui!
                    val RAPID_API_KEY = "508fd13a3bmsh910469a518b2df6p10630djsndff6498a69c9" 
                    
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClients.rapidApi.downloadMedia(apiKey = RAPID_API_KEY, url = url)
                    }

                    val videoUrlExtracted = response.extractVideoUrl()

                    if (!videoUrlExtracted.isNullOrBlank() && response.error != true) {
                        val media = SocialMediaData(
                            title = response.title ?: response.caption ?: response.data?.title ?: "Instagram Reel",
                            coverUrl = response.thumbnail ?: response.videoImg ?: response.data?.thumbnail,
                            videoUrl = videoUrlExtracted,
                            audioUrl = null 
                        )
                        _uiState.value = UiState.Success(media)
                    } else {
                        _uiState.value = UiState.Error("Não foi possível extrair a mídia do Instagram (Verifique sua API Key ou o formato do link).")
                    }
                } else if (url.contains("twitter.com") || url.contains("x.com")) {
                    // Usar RapidAPI (Beatom) para Twitter (X)
                    
                    val RAPID_API_KEY = "508fd13a3bmsh910469a518b2df6p10630djsndff6498a69c9" 
                    
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClients.twitterApi.downloadMedia(apiKey = RAPID_API_KEY, url = url)
                    }

                    val videoUrlExtracted = response.extractVideoUrl()

                   // Se o status retornado indicar erro, lidamos com isso
                   if (response.status == "error") {
                        _uiState.value = UiState.Error(response.message ?: "Erro ao buscar vídeo do Twitter.")
                   } else if (!videoUrlExtracted.isNullOrBlank()) {
                        val media = SocialMediaData(
                            title = response.title ?: response.data?.title ?: "Twitter Video",
                            coverUrl = response.thumbnail ?: response.data?.thumbnail,
                            videoUrl = videoUrlExtracted,
                            audioUrl = null 
                        )
                        _uiState.value = UiState.Success(media)
                    } else {
                        _uiState.value = UiState.Error("Mídia não encontrada no link do Twitter fornecido.")
                    }
                } else {
                    _uiState.value = UiState.Error("Plataforma não suportada (Use TikTok, Instagram ou X/Twitter).")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Erro de conexão: ${e.localizedMessage}")
            }
        }
    }
}

// ==================== Main Activity UI ====================

class MainActivity : ComponentActivity() {
    
    // ViewModel compartilhado - necessário para acessar em onNewIntent()
    private val viewModel: MainViewModel by lazy {
        androidx.lifecycle.ViewModelProvider(this)[MainViewModel::class.java]
    }
    
    // Launcher para solicitar permissão de Notificação no Android 13+
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Caso fosse necessário tratar a recusa
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Setup Notification Channel para os downloads OkHttp (Instagram/X)
        createNotificationChannel()
        askNotificationPermission()

        // Processa o intent inicial (quando app é aberto via share)
        handleIntent(intent)
        
        setContent {
            com.naicolasdev.tiktokdownloader.ui.theme.TikTokSaverTheme {
                AmbientGlowBackground()
                
                val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
                
                Scaffold(
                    snackbarHost = { 
                        androidx.compose.material3.SnackbarHost(snackbarHostState) { data ->
                            androidx.compose.material3.Snackbar(
                                snackbarData = data,
                                containerColor = com.naicolasdev.tiktokdownloader.ui.theme.SurfaceVariantDark,
                                contentColor = com.naicolasdev.tiktokdownloader.ui.theme.TextPrimary,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                            )
                        } 
                    },
                    containerColor = Color.Transparent,
                    contentColor = com.naicolasdev.tiktokdownloader.ui.theme.TextPrimary
                ) { paddingValues ->
                    MainScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        viewModel = viewModel,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
    
    /**
     * Chamado quando a Activity já está rodando e recebe um novo Intent.
     * Isso acontece com launchMode="singleTask" quando o usuário compartilha
     * um novo link enquanto o app já está aberto.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Atualiza o intent atual
        handleIntent(intent)
    }
    
    /**
     * Extrai a URL do TikTok do Intent e processa.
     */
    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        
        val action = intent.action
        val type = intent.type
        
        when {
            // ACTION_SEND: Compartilhamento de texto (ex: do TikTok)
            action == Intent.ACTION_SEND && type == "text/plain" -> {
                val extraText = intent.getStringExtra(Intent.EXTRA_TEXT)
                val clipData = intent.clipData
                
                val text = com.naicolasdev.tiktokdownloader.util.SocialMediaUrlParser
                    .extractTextFromIntent(extraText, clipData)
                
                val socialMediaUrl = com.naicolasdev.tiktokdownloader.util.SocialMediaUrlParser
                    .extractUrl(text)
                
                if (socialMediaUrl != null) {
                    viewModel.processSharedUrl(socialMediaUrl)
                } else if (text != null) {
                    Toast.makeText(
                        this,
                        "Link de mídia não encontrado no texto compartilhado.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            
            // ACTION_VIEW: Deep link (ex: clicar em link do TikTok)
            action == Intent.ACTION_VIEW -> {
                val uri = intent.data?.toString()
                if (uri != null && com.naicolasdev.tiktokdownloader.util.SocialMediaUrlParser.isValidUrl(uri)) {
                    viewModel.processSharedUrl(uri)
                }
            }
        }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Downloads (Instagram e Twitter)"
            val descriptionText = "Notificações para downloads de mídia via OkHttp"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("DOWNLOAD_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(),
    snackbarHostState: androidx.compose.material3.SnackbarHostState
) {
    val uiState by viewModel.uiState.collectAsState()
    val urlInput by viewModel.urlInput.collectAsState()
    val context = LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    val showMessage = { message: String ->
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    Column(
        modifier = modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // HEADER
        Text(
            text = "Video Downloader",
            style = MaterialTheme.typography.displayLarge,
            color = com.naicolasdev.tiktokdownloader.ui.theme.TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Badges
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            val badgeModifier = Modifier
                .background(com.naicolasdev.tiktokdownloader.ui.theme.SurfaceVariantDark, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
            val textStyle = MaterialTheme.typography.labelSmall.copy(color = com.naicolasdev.tiktokdownloader.ui.theme.TextSecondary)

            Box(modifier = badgeModifier) { Text("TikTok", style = textStyle) }
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = badgeModifier) { Text("Instagram", style = textStyle) }
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = badgeModifier) { Text("X (Twitter)", style = textStyle) }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // INPUT PANEL (Glass-less)
        GlassPanel {
            Column {
                HtmlInput(
                    value = urlInput,
                    onValueChange = { viewModel.updateUrl(it) },
                    onPasteClick = {
                        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clipData = clipboardManager.primaryClip
                        if (clipData != null && clipData.itemCount > 0) {
                            val pasteText = clipData.getItemAt(0).text
                            if (pasteText != null) {
                                viewModel.updateUrl(pasteText.toString())
                            }
                        }
                    },
                    onClearClick = { viewModel.updateUrl("") }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                com.naicolasdev.tiktokdownloader.ui.components.PrimaryButton(
                    text = if (uiState is UiState.Loading) "Processando..." else "Baixar",
                    onClick = { viewModel.fetchVideoInfo() },
                    isLoading = uiState is UiState.Loading,
                    enabled = urlInput.trim().isNotEmpty()
                )

                // Error Message
                AnimatedVisibility(visible = uiState is UiState.Error) {
                    val error = (uiState as? UiState.Error)?.message ?: ""
                    Box(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                            .background(com.naicolasdev.tiktokdownloader.ui.theme.ErrorBg, RoundedCornerShape(8.dp))
                            .border(1.dp, com.naicolasdev.tiktokdownloader.ui.theme.ErrorColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = error,
                            color = com.naicolasdev.tiktokdownloader.ui.theme.ErrorColor,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // RESULT PANEL
        AnimatedVisibility(
            visible = uiState is UiState.Success,
            enter = fadeIn() + slideInVertically()
        ) {
            val data = (uiState as? UiState.Success)?.data
            if (data != null) {
                ResultCard(
                    data = data,
                    onDownloadVideoClick = {
                        downloadMedia(context, data.videoUrl ?: "", data.title ?: "video", MediaType.VIDEO) { msg -> showMessage(msg) }
                    },
                    onDownloadAudioClick = if (data.hasAudio) {
                        { downloadMedia(context, data.audioUrl ?: "", data.title ?: "audio", MediaType.AUDIO) { msg -> showMessage(msg) } }
                    } else null
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        
        // FOOTER
        val uriHandler = LocalUriHandler.current
        Row(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null, // No ripple for text link style
                    onClick = { uriHandler.openUri("https://github.com/naicolas-dev") }
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Desenvolvido por ",
                style = MaterialTheme.typography.labelSmall,
                color = com.naicolasdev.tiktokdownloader.ui.theme.TextSecondary
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_github),
                contentDescription = "GitHub",
                tint = com.naicolasdev.tiktokdownloader.ui.theme.TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Nicolas Viana Alves",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = com.naicolasdev.tiktokdownloader.ui.theme.TextSecondary
            )
        }
    }
}

/**
 * Tipos de mídia disponíveis para download
 */
enum class MediaType {
    VIDEO,
    AUDIO
}

/**
 * Função genérica para download de mídia (vídeo ou áudio).
 * 
 * @param context Contexto Android
 * @param url URL da mídia a ser baixada
 * @param title Título do conteúdo (usado para nome do arquivo)
 * @param mediaType Tipo de mídia: VIDEO ou AUDIO
 * @param onMessage Callback para emitir mensagens no Snackbar
 */
fun downloadMedia(context: Context, url: String, title: String, mediaType: MediaType, onMessage: (String) -> Unit) {
    if (url.isEmpty()) {
        onMessage("URL inválida")
        return
    }

    try {
        val (fileName, directory, mimeType, description) = when (mediaType) {
            MediaType.VIDEO -> Quadruple(
                "Video_Downloader_${System.currentTimeMillis()}.mp4",
                Environment.DIRECTORY_MOVIES,
                "video/mp4",
                "Download do Vídeo MP4 iniciado..."
            )
            MediaType.AUDIO -> Quadruple(
                "Video_Downloader_${System.currentTimeMillis()}.mp3",
                Environment.DIRECTORY_MUSIC,
                "audio/mpeg",
                "Download do Áudio MP3 iniciado..."
            )
        }

        if (url.contains("twimg.com") || url.contains("twitter.com") || url.contains("instagram") || url.contains("cdninstagram")) {
            onMessage(description)
            downloadWithOkHttp(context, url, fileName, mimeType, directory, onMessage)
            return
        }

        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle("Video Downloader")
            setDescription(description)
            setMimeType(mimeType)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(directory, fileName)
            addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
            addRequestHeader("Accept-Encoding", "identity") // Fix para falhas TLS do DownloadManager
        }
        
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        
        val mediaName = if (mediaType == MediaType.VIDEO) "vídeo" else "áudio"
        onMessage("Download de $mediaName iniciado...")
    } catch (e: Exception) {
        onMessage("Erro: ${e.message}")
    }
}

/**
 * Função genérica para exibir notificações de sistema (Heads-Up)
 */
private fun showNotification(context: Context, title: String, content: String, isSticky: Boolean = false) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
    }

    val notificationId = System.currentTimeMillis().toInt()
    val builder = NotificationCompat.Builder(context, "DOWNLOAD_CHANNEL")
        .setSmallIcon(R.drawable.ic_launcher_foreground) // Ícone transparente/aplicativo
        .setContentTitle(title)
        .setContentText(content)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setOngoing(isSticky)
        .setAutoCancel(!isSticky)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(if (isSticky) 999 else notificationId, builder.build())
}

/**
 * Fallback download via OkHttp em uma corrotina global (ou lançada customizada).
 */
fun downloadWithOkHttp(context: Context, url: String, fileName: String, mimeType: String, directory: String, onMessage: (String) -> Unit) {
    showNotification(context, "Baixando Mídia", "Iniciando download...", isSticky = true)

    kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful || response.body == null) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(999)
                showNotification(context, "Erro no Download", "Falha ao baixar mídia via servidor alternativo.")

                withContext(Dispatchers.Main) {
                    onMessage("Falha no download via OkHttp")
                }
                return@launch
            }

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, directory + "/VideoDownloader")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val collection = if (mimeType.startsWith("video/")) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            }

            val itemUri = context.contentResolver.insert(collection, values)
            if (itemUri != null) {
                context.contentResolver.openOutputStream(itemUri).use { outputStream ->
                    response.body!!.byteStream().use { inputStream ->
                        inputStream.copyTo(outputStream!!)
                    }
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    context.contentResolver.update(itemUri, values, null, null)
                }

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(999)
                showNotification(context, "Download Concluído!", fileName)

                withContext(Dispatchers.Main) {
                    onMessage("Download concluído: $fileName")
                }
            }
        } catch (e: Exception) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(999)
            showNotification(context, "Erro no Download", "Falha ao salvar o arquivo: ${e.localizedMessage}")

            withContext(Dispatchers.Main) {
                onMessage("Erro no download: ${e.localizedMessage}")
            }
        }
    }
}

/** Helper data class for download parameters */
private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
