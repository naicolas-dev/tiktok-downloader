package com.naicolasdev.tiktokdownloader

import com.google.gson.annotations.SerializedName

data class RapidApiResponse(
    @SerializedName("video") val video: String?,
    @SerializedName("download_link") val downloadLink: String?,
    @SerializedName("thumbnail") val thumbnail: String?,
    @SerializedName("video_img") val videoImg: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("caption") val caption: String?,
    @SerializedName("error") val error: Boolean?,
    @SerializedName("medias") val medias: List<RapidApiMedia>? = null,
    
    // Suporte caso venha envelopado em 'data'
    @SerializedName("data") val data: RapidApiInnerData? = null
) {
    /** Retorna a melhor URL de vídeo disponível na resposta flexível */
    fun extractVideoUrl(): String? {
        return video ?: downloadLink ?: medias?.firstOrNull()?.url ?: data?.video ?: data?.medias?.firstOrNull()?.url
    }
}

data class RapidApiInnerData(
    @SerializedName("video") val video: String?,
    @SerializedName("thumbnail") val thumbnail: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("medias") val medias: List<RapidApiMedia>? = null
)

data class RapidApiMedia(
    @SerializedName("url") val url: String?,
    @SerializedName("quality") val quality: String?
)
