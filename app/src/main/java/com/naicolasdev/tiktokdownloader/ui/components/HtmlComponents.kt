package com.naicolasdev.tiktokdownloader.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.naicolasdev.tiktokdownloader.ui.theme.*

// ==================== GLASS PANEL ====================
// .glass-panel { background: rgba(255, 255, 255, 0.03); border: 1px solid rgba(255, 255, 255, 0.1); }
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = GlassBg,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Box(modifier = Modifier.padding(24.dp)) {
            content()
        }
    }
}

// ==================== GLOW BACKGROUND ====================
// .glow-bg { background: radial-gradient(...); filter: blur(60px); }
@Composable
fun AmbientGlowBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // We replicate the top-left/center glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .alpha(0.6f)
                .blur(60.dp) // Heavy blur
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            TikTokCyan.copy(alpha = 0.15f),
                            TikTokPink.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = Offset.Unspecified, // Defaults to center of box
                        radius = 500f
                    )
                )
        )
    }
}

// ==================== GRADIENT BUTTON ====================
@Composable
fun TikTokGradientButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.horizontalGradient(
        listOf(ButtonStart, ButtonEnd)
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer {
                shadowElevation = 16.dp.toPx()
                shape = RoundedCornerShape(8.dp)
                clip = true
            },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        enabled = !isLoading,
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .then(if (isLoading) Modifier.alpha(0.7f) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Download, // Using Download as proxy for arrow
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ==================== HTML INPUT ====================
@Composable
fun HtmlInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // .bg-gray-900 rounded-lg flex items-center p-1
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BgInput)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Link Icon
        Icon(
            imageVector = Icons.Default.Download, // Generic link/download icon
            contentDescription = null,
            tint = TextGrayDark,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = "Cole o link do TikTok aqui...",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextGrayDark)
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextWhite),
                cursorBrush = SolidColor(TikTokCyan),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
