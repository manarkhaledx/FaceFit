package com.example.facefit.data.models.responses

import com.example.facefit.data.models.User
import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("data")
    val data: LoginData
)

data class LoginData(
    @SerializedName("customer")
    val user: User,
    @SerializedName("token")
    val token: String
)
