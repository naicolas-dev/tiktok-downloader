package com.naicolasdev.tiktokdownloader.util

/**
 * Utilitário para parsing e validação de URLs de Redes Sociais.
 */
object SocialMediaUrlParser {

    private val TIKTOK_REGEX = Regex(
        """https?://(?:(?:vm|vt|www|m)\.)?tiktok\.com/[^\s]+""",
        RegexOption.IGNORE_CASE
    )
    
    // Suporta links como instagram.com/p/..., instagram.com/reel/...
    private val INSTAGRAM_REGEX = Regex(
        """https?://(?:www\.)?instagram\.com/(?:p|reel|tv|reels)/[^\s/?]+""",
        RegexOption.IGNORE_CASE
    )
    
    // Suporta links como twitter.com/user/status/123 ou x.com/user/status/123
    private val TWITTER_REGEX = Regex(
        """https?://(?:www\.)?(?:twitter\.com|x\.com)/[^/]+/status/[^\s/?]+""",
        RegexOption.IGNORE_CASE
    )

    /**
     * Extrai a primeira URL válida de rede social de um texto.
     */
    fun extractUrl(text: String?): String? {
        if (text.isNullOrBlank()) return null
        val trimmedText = text.trim()

        var matchResult = TIKTOK_REGEX.find(trimmedText)
        if (matchResult != null) return normalizeUrl(matchResult.value)

        matchResult = INSTAGRAM_REGEX.find(trimmedText)
        if (matchResult != null) return normalizeUrl(matchResult.value)

        matchResult = TWITTER_REGEX.find(trimmedText)
        if (matchResult != null) return normalizeUrl(matchResult.value)

        // Fallback genérico para tentar extrair e depois validar
        val fallbackRegex = Regex("""https?://[^\s]+""")
        val fallbackMatch = fallbackRegex.find(trimmedText)
        if (fallbackMatch != null) {
            val url = fallbackMatch.value
            if (isValidUrl(url)) return normalizeUrl(url)
        }

        return null
    }

    /**
     * Valida se uma URL é suportada (TikTok, Instagram ou Twitter).
     */
    fun isValidUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val lowerUrl = url.trim().lowercase()
        return lowerUrl.contains("tiktok.com") || 
               lowerUrl.contains("instagram.com") || 
               lowerUrl.contains("twitter.com") || 
               lowerUrl.contains("x.com")
    }

    private fun normalizeUrl(url: String): String {
        return url
            .trim()
            .replace(Regex("""\s+"""), "")
    }

    fun extractTextFromIntent(
        extraText: String?,
        clipData: android.content.ClipData?
    ): String? {
        if (!extraText.isNullOrBlank()) return extraText
        if (clipData != null && clipData.itemCount > 0) {
            val item = clipData.getItemAt(0)
            val clipText = item?.text?.toString()
            if (!clipText.isNullOrBlank()) return clipText
        }
        return null
    }
}
