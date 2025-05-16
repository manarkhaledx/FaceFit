package com.example.facefit.domain.usecases.reviews

import com.example.facefit.domain.models.Review
import com.example.facefit.domain.repository.ReviewRepository
import com.example.facefit.domain.utils.Resource
import javax.inject.Inject

class GetReviewsUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(token: String, glassesId: String): Resource<List<Review>> {
        return repository.getReviews(token, glassesId)
    }
}