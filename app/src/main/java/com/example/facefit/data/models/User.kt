package com.example.facefit.data.models

data class User( //هو الجسم الرئيسي داخل Response.
    val _id: String,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String,
    val password: String,
    val createdAt: String,
    val updatedAt: String
)
