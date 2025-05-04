package com.example.facefit.ui.utils

// Add this to your models package
data class ValidationError(
    val field: String?,
    val message: String?
)

data class ErrorResponse(
    val errors: List<ValidationError>?
)