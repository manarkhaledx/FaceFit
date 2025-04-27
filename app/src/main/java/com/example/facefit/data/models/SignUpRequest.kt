package com.example.facefit.data.models



data class SignUpRequest( //أنت ترسله للسيرفر (Data you send)
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String,
    val password: String,
    val confirmPassword: String
)

