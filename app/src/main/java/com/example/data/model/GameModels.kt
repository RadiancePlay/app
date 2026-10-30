package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GamesResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "count") val count: Int? = null,
    @Json(name = "data") val data: List<GameItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GameItemDto(
    @Json(name = "id") val id: Long,
    @Json(name = "title") val title: String,
    @Json(name = "package") val packageName: String,
    @Json(name = "version") val version: String,
    @Json(name = "language") val language: String? = null,
    @Json(name = "image") val image: String,
    @Json(name = "download-link") val downloadLink: String
)

@JsonClass(generateAdapter = true)
data class AppVersionResponse(
    @Json(name = "version") val version: String,
    @Json(name = "download") val download: String
)

