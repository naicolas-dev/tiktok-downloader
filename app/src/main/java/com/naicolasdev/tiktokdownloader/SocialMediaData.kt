package com.naicolasdev.tiktokdownloader

import com.google.gson.annotations.SerializedName

// --- Cobalt API Models ---

data class CobaltRequest(
    @SerializedName("url") val url: String,
    @SerializedName("videoQuality") val videoQuality: String = "1080", // Opções comuns: max, 1080, 720, etc.
    @SerializedName("filenamePattern") val filenamePattern: String = "nerd",
    @SerializedName("isAudioOnly") val isAudioOnly: Boolean = false,
    @SerializedName("isTTFullAudio") val isTTFullAudio: Boolean = false,
    @SerializedName("isAudioMuted") val isAudioMuted: Boolean = false,
    @SerializedName("dubLang") val dubLang: Boolean = false,
    @SerializedName("disableMetadata") val disableMetadata: Boolean = false
)

data class CobaltResponse(
    @SerializedName("status") val status: String,
    @SerializedName("url") val url: String?, // Retornado quando status = "redirect" ou "tunnel"
    @SerializedName("picker") val picker: List<CobaltPickerItem>?, // Retornado quando status = "picker" (múltiplas fotos/vídeos)
    @SerializedName("text") val text: String?, // Título/Descrição extraída
    @SerializedName("audio") val audio: String? // Pode vir separado em alguns casos (raro na Cobalt)
)

data class CobaltPickerItem(
    @SerializedName("type") val type: String, // "video", "photo", "gif"
    @SerializedName("url") val url: String,
    @SerializedName("thumb") val thumb: String?
)

// --- Internal App Model ---

/**
 * Modelo genérico usado pela UI para exibir o resultado.
 * Independe da API (atualmente preenchido pela Cobalt).
 */
data class SocialMediaData(
    val title: String?,
    val coverUrl: String?, // Thumbnail
    val videoUrl: String?,
    val audioUrl: String? = null
) {
    val hasAudio: Boolean get() = !audioUrl.isNullOrBlank()
}
