package com.example.facefit.domain.models

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val address: String?,
    val profilePicture: String?
)