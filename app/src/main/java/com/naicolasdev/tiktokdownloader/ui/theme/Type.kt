package com.naicolasdev.tiktokdownloader.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.naicolasdev.tiktokdownloader.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val OutfitFont = GoogleFont("Outfit")

val OutfitFamily = FontFamily(
    Font(googleFont = OutfitFont, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = OutfitFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = OutfitFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = OutfitFont, fontProvider = provider, weight = FontWeight.Bold)
)

val Typography = Typography(
    // H1 (Hero Title)
    displayLarge = TextStyle(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        letterSpacing = (-1).sp, // tracking-tight
        color = TextWhite
    ),
    // Button Text
    labelLarge = TextStyle(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = TextWhite
    ),
    // Input Text
    bodyLarge = TextStyle(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = TextWhite
    ),
    // Placeholder / Subtitles
    bodyMedium = TextStyle(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = TextGray
    ),
    // Badges / Small Text
    labelSmall = TextStyle(
        fontFamily = OutfitFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = TextGray
    )
)
