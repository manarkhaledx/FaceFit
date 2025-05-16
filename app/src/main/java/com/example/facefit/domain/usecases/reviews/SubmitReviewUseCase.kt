package com.example.facefit.domain.usecases.reviews

import com.example.facefit.domain.repository.ReviewRepository
import com.example.facefit.domain.utils.Resource
import javax.inject.Inject

class SubmitReviewUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(
        token: String,
        glassesId: String,
        rating: Int,
        comment: String
    ): Resource<Unit> {
        return repository.submitReview(token, glassesId, rating, comment)
    }
}