package com.example.facefit.data.models

import com.google.gson.annotations.SerializedName

data class User(
    val _id: String,
    val firstName: String,
    val lastName: String,
    @SerializedName("phoneNumber")
    val phone: String,
    val email: String,
    val password: String,
)
