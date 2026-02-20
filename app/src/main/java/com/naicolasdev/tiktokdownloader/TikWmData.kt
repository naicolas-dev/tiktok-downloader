package com.naicolasdev.tiktokdownloader

import com.google.gson.annotations.SerializedName

data class TikWmResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("msg") val msg: String,
    @SerializedName("data") val data: TikWmData?
)

data class TikWmData(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("cover") val cover: String?,
    @SerializedName("play") val play: String?,
    @SerializedName("music") val music: String?,
    @SerializedName("author") val author: TikWmAuthor?
)

data class TikWmAuthor(
    @SerializedName("nickname") val nickname: String?,
    @SerializedName("avatar") val avatar: String?
)
