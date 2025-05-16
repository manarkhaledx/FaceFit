package com.example.facefit.data.models.requests

data class SubmitReviewRequest(
    val glassesId: String,
    val rating: Int,
    val comment: String
)