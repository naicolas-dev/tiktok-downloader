package com.naicolasdev.tiktokdownloader

import androidx.compose.ui.res.painterResource
import com.naicolasdev.tiktokdownloader.R

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
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
import com.naicolasdev.tiktokdownloader.ui.components.TikTokGradientButton
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

// ==================== API and ViewModel ====================

interface TikWmApiService {
    @FormUrlEncoded
    @POST("api/")
    suspend fun getVideoInfo(
        @Field("url") url: String,
        @Field("hd") hd: Int = 1
    ): TikWmResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://www.tikwm.com/"

    val api: TikWmApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TikWmApiService::class.java)
    }
}

sealed class UiState {
    data object Idle : UiState()
    data object Loading : UiState()
    data class Success(val data: TikWmData) : UiState()
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
            _uiState.value = UiState.Error("Por favor, cole um link válido do TikTok.")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getVideoInfo(url)
                }
                if (response.code == 0 && response.data != null) {
                    _uiState.value = UiState.Success(response.data)
                } else {
                    _uiState.value = UiState.Error(response.msg.ifEmpty { "Não foi possível encontrar o vídeo." })
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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Processa o intent inicial (quando app é aberto via share)
        handleIntent(intent)
        
        setContent {
            TikTokSaverTheme {
                AmbientGlowBackground()
                
                Scaffold(
                    containerColor = Color.Transparent,
                    contentColor = TextWhite
                ) { paddingValues ->
                    MainScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        viewModel = viewModel
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
                
                val text = com.naicolasdev.tiktokdownloader.util.TikTokUrlParser
                    .extractTextFromIntent(extraText, clipData)
                
                val tiktokUrl = com.naicolasdev.tiktokdownloader.util.TikTokUrlParser
                    .extractTikTokUrl(text)
                
                if (tiktokUrl != null) {
                    viewModel.processSharedUrl(tiktokUrl)
                } else if (text != null) {
                    // Texto recebido mas não é URL do TikTok
                    Toast.makeText(
                        this,
                        "Link do TikTok não encontrado no texto compartilhado.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            
            // ACTION_VIEW: Deep link (ex: clicar em link do TikTok)
            action == Intent.ACTION_VIEW -> {
                val uri = intent.data?.toString()
                if (uri != null && com.naicolasdev.tiktokdownloader.util.TikTokUrlParser.isValidTikTokUrl(uri)) {
                    viewModel.processSharedUrl(uri)
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val urlInput by viewModel.urlInput.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // HEADER
        // Title with Gradient "Downloader"
        Text(
            text = buildAnnotatedString {
                append("TikTok ")
                withStyle(
                    SpanStyle(
                        brush = Brush.horizontalGradient(listOf(TikTokCyan, TikTokPink))
                    )
                ) {
                    append("Downloader")
                }
            },
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Subtitle
        Text(
            text = "Baixe vídeos sem marca d'água em HD",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // INPUT PANEL (Glass)
        GlassPanel {
            Column {
                HtmlInput(
                    value = urlInput,
                    onValueChange = { viewModel.updateUrl(it) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TikTokGradientButton(
                    text = if (uiState is UiState.Loading) "Processando..." else "Baixar Agora",
                    onClick = { viewModel.fetchVideoInfo() },
                    isLoading = uiState is UiState.Loading
                )

                // Error Message
                AnimatedVisibility(visible = uiState is UiState.Error) {
                    val error = (uiState as? UiState.Error)?.message ?: ""
                    Box(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                            .background(Color(0xFFFE2C55).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFFE2C55).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = error,
                            color = Color(0xFFF87171), // text-red-400
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
                        downloadMedia(context, data.play ?: "", data.title ?: "video", MediaType.VIDEO)
                    },
                    onDownloadAudioClick = if (data.hasAudio) {
                        { downloadMedia(context, data.music ?: "", data.title ?: "audio", MediaType.AUDIO) }
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
                color = TextGrayDark
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_github),
                contentDescription = "GitHub",
                tint = TextGrayDark,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Nicolas Viana Alves",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextGrayDark
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
 */
fun downloadMedia(context: Context, url: String, title: String, mediaType: MediaType) {
    if (url.isEmpty()) {
        Toast.makeText(context, "URL inválida", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val (fileName, directory, mimeType, description) = when (mediaType) {
            MediaType.VIDEO -> Quadruple(
                "TikTok_Downloader_${System.currentTimeMillis()}.mp4",
                Environment.DIRECTORY_MOVIES,
                "video/mp4",
                "Baixando MP4..."
            )
            MediaType.AUDIO -> Quadruple(
                "TikTok_Downloader_${System.currentTimeMillis()}.mp3",
                Environment.DIRECTORY_MUSIC,
                "audio/mpeg",
                "Baixando MP3..."
            )
        }

        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle("TikTok Downloader")
            setDescription(description)
            setMimeType(mimeType)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(directory, fileName)
        }
        
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        
        val mediaName = if (mediaType == MediaType.VIDEO) "vídeo" else "áudio"
        Toast.makeText(context, "Download de $mediaName iniciado...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

/** Helper data class for download parameters */
private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
