package com.naicolasdev.tiktokdownloader.ui.home

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.naicolasdev.tiktokdownloader.SocialMediaData
import com.naicolasdev.tiktokdownloader.ui.components.GlassPanel
import com.naicolasdev.tiktokdownloader.ui.theme.*

/**
 * Card de resultado com opções de download de vídeo e áudio.
 * 
 * @param data Dados do vídeo do TikTok
 * @param onDownloadVideoClick Callback para download de vídeo (MP4)
 * @param onDownloadAudioClick Callback para download de áudio (MP3), null se áudio indisponível
 */
@Composable
fun ResultCard(
    data: SocialMediaData,
    onDownloadVideoClick: () -> Unit,
    onDownloadAudioClick: (() -> Unit)? = null
) {
    GlassPanel(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (onDownloadAudioClick != null) 196.dp else 156.dp)
        ) {
            // Left: Cover Image
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariantDark)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(data.coverUrl ?: "") // Fallback for null cover
                        .crossfade(true)
                        .build(),
                    contentDescription = "Cover",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right: Content Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Title and Badges
                Column {
                    Text(
                        text = data.title ?: "Sem título",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Badges
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // HD Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AccentPrimary.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "HD",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = AccentPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Sem marca d'água",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary
                            )
                        )
                    }
                }

                // Download Buttons
                Column {
                    // MP4 Download Button (always visible)
                    Button(
                        onClick = onDownloadVideoClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentPrimary,
                            contentColor = OnAccentPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Salvar Vídeo MP4",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Salvar Vídeo",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = OnAccentPrimary,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }

                    // MP3 Download Button (only if audio available)
                    if (onDownloadAudioClick != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedButton(
                            onClick = onDownloadAudioClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TextPrimary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "Salvar Áudio MP3",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Salvar Áudio (MP3)",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
