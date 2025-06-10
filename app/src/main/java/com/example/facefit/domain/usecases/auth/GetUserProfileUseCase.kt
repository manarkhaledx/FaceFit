package com.example.facefit.domain.usecases.auth

import com.example.facefit.domain.models.User
import com.example.facefit.domain.repository.UserRepository
import com.example.facefit.domain.utils.Resource
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(token: String): Resource<User> {
        return repository.getUserProfile(token)
    }

}