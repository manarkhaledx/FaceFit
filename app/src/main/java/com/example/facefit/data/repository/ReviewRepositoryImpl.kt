package com.example.facefit.data.repository

import com.example.facefit.data.models.User
import com.example.facefit.data.models.requests.SubmitReviewRequest
import com.example.facefit.data.remote.ApiService
import com.example.facefit.domain.models.Review
import com.example.facefit.domain.repository.ReviewRepository
import com.example.facefit.domain.utils.Resource
import javax.inject.Inject

class ReviewRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ReviewRepository {
    override suspend fun getReviews(token: String, glassesId: String): Resource<List<Review>> {
        return try {
            val formattedToken = if (!token.startsWith("Bearer ")) "Bearer $token" else token
            val response = apiService.getReviews(formattedToken, glassesId)
            if (response.isSuccessful) {
                Resource.Success(response.body()?.data ?: emptyList())
            } else {
                Resource.Error("Failed to fetch reviews: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun getUserProfile(token: String): Resource<User> {
        return try {
            val response = apiService.getCustomerProfile(token)
            if (response.isSuccessful) {
                Resource.Success(response.body()?.data ?: throw Exception("No profile data"))
            } else {
                Resource.Error("Failed to fetch profile: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun submitReview(
        token: String,
        glassesId: String,
        rating: Int,
        comment: String
    ): Resource<Unit> {
        return try {
            if (rating !in 1..5) {
                return Resource.Error("Rating must be between 1 and 5")
            }
            if (comment.isBlank()) {
                return Resource.Error("Comment cannot be empty")
            }
            val formattedToken = if (!token.startsWith("Bearer ")) "Bearer $token" else token
            val request = SubmitReviewRequest(glassesId, rating, comment)
            val response = apiService.submitReview(formattedToken, request)

            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Resource.Error(errorBody ?: "Failed to submit review")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error occurred")
        }
    }
}