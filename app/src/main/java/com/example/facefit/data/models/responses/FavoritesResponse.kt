package com.example.facefit.data.models.responses

import com.google.gson.annotations.SerializedName

data class FavoritesResponse<T>(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: T
)