package com.example.gamelib.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GameDto (
    val id: Int,
    val title: String,

    @SerializedName("short_description")
    val shortDescription: String,

    val genre: String,
    val platform: String,
    val developer: String,
    val thumbnail: String? = null
)
