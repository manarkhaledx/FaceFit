package com.example.facefit.data.models.responses

import com.example.facefit.domain.models.Review

data class ReviewsResponse(
    val status: String,
    val data: List<Review>
)