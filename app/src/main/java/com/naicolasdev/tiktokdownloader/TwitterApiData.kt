package com.naicolasdev.tiktokdownloader

import com.google.gson.annotations.SerializedName

data class TwitterApiResponse(
    @SerializedName("status") val status: String?, // Ex: "success" ou "error"
    @SerializedName("message") val message: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("video") val video: String?,
    @SerializedName("thumbnail") val thumbnail: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("data") val data: TwitterApiData? = null
) {
    /** Retorna a URL principal do vídeo tentando diferentes chaves que a API pode mandar */
    fun extractVideoUrl(): String? {
        // Primeiro tenta as chaves diretas, caso a estrutura mude. 
        // Em seguida, busca na lista de links retornada pela API atual
        return video ?: url ?: data?.video ?: data?.url ?: data?.links?.firstOrNull()?.downloadUrl
    }
}

data class TwitterApiData(
    @SerializedName("video") val video: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("thumbnail") val thumbnail: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("links") val links: List<TwitterApiVideo>? = null
)

data class TwitterApiVideo(
    @SerializedName("download_url") val downloadUrl: String?,
    @SerializedName("resolution") val resolution: String?,
    @SerializedName("type") val type: String?
)
