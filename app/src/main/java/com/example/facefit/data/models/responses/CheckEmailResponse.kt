package com.example.facefit.data.models.responses

data class CheckEmailResponse(
    val exists: Boolean,
    val message: String? = null
)