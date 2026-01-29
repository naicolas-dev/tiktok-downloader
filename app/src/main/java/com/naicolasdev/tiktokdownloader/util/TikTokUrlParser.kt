package com.naicolasdev.tiktokdownloader.util

/**
 * Utilitário para parsing e validação de URLs do TikTok.
 */
object TikTokUrlParser {

    // Padrões de URL do TikTok suportados
    private val TIKTOK_URL_REGEX = Regex(
        """https?://(?:(?:vm|vt|www|m)\.)?tiktok\.com/[^\s]+""",
        RegexOption.IGNORE_CASE
    )

    // Hosts válidos do TikTok
    private val VALID_TIKTOK_HOSTS = listOf(
        "tiktok.com",
        "vm.tiktok.com",
        "vt.tiktok.com",
        "www.tiktok.com",
        "m.tiktok.com"
    )

    /**
     * Extrai a primeira URL válida do TikTok de um texto.
     * O TikTok pode enviar texto junto com a URL, ex:
     * "Olha esse vídeo! https://vm.tiktok.com/abc #fyp"
     *
     * @param text Texto que pode conter uma URL do TikTok
     * @return URL extraída e normalizada, ou null se não encontrada
     */
    fun extractTikTokUrl(text: String?): String? {
        if (text.isNullOrBlank()) return null

        val trimmedText = text.trim()

        // Tenta encontrar uma URL do TikTok no texto
        val matchResult = TIKTOK_URL_REGEX.find(trimmedText)

        return matchResult?.value?.let { url ->
            normalizeUrl(url)
        }
    }

    /**
     * Valida se uma URL é do TikTok.
     *
     * @param url URL a ser validada
     * @return true se for uma URL válida do TikTok
     */
    fun isValidTikTokUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false

        return try {
            val trimmedUrl = url.trim().lowercase()
            VALID_TIKTOK_HOSTS.any { host ->
                trimmedUrl.contains(host)
            } && TIKTOK_URL_REGEX.matches(url.trim())
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Normaliza a URL removendo espaços e caracteres inválidos.
     */
    private fun normalizeUrl(url: String): String {
        return url
            .trim()
            .replace(Regex("""\s+"""), "") // Remove espaços internos
            .removeSuffix("/") // Remove barra final se houver
    }

    /**
     * Extrai texto de um Intent, tentando EXTRA_TEXT primeiro,
     * depois fallback para ClipData.
     *
     * @param extraText Valor de Intent.EXTRA_TEXT
     * @param clipData Valor de Intent.clipData
     * @return Texto extraído ou null
     */
    fun extractTextFromIntent(
        extraText: String?,
        clipData: android.content.ClipData?
    ): String? {
        // Tenta EXTRA_TEXT primeiro
        if (!extraText.isNullOrBlank()) {
            return extraText
        }

        // Fallback para ClipData
        if (clipData != null && clipData.itemCount > 0) {
            val item = clipData.getItemAt(0)
            val clipText = item?.text?.toString()
            if (!clipText.isNullOrBlank()) {
                return clipText
            }
        }

        return null
    }
}
