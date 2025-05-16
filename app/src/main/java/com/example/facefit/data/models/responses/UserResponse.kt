package com.example.facefit.data.models.responses

import com.example.facefit.data.models.User

data class UserResponse(
    val status: String,
    val data: User
)