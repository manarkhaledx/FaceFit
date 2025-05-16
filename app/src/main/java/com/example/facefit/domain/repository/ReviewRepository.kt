package com.example.facefit.domain.repository

import com.example.facefit.data.models.User
import com.example.facefit.domain.models.Review
import com.example.facefit.domain.utils.Resource

interface ReviewRepository {
    suspend fun getReviews(token: String, glassesId: String): Resource<List<Review>>
    suspend fun getUserProfile(token: String): Resource<User>
    suspend fun submitReview(
        token: String,
        glassesId: String,
        rating: Int,
        comment: String
    ): Resource<Unit>
}