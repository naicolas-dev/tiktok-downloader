package com.naicolasdev.tiktokdownloader.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TikTokPink,
    secondary = TikTokCyan,
    background = BgDark,
    surface = BgDark,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun TikTokSaverTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
