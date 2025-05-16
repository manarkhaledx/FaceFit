package com.example.facefit.domain.usecases.auth

import com.example.facefit.data.models.User
import com.example.facefit.domain.repository.ReviewRepository
import com.example.facefit.domain.utils.Resource
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(token: String): Resource<User> {
        return repository.getUserProfile(token)
    }
}