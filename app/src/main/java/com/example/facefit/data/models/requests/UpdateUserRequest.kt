package com.example.facefit.data.models.requests

data class UpdateUserRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val address: String? = null
)