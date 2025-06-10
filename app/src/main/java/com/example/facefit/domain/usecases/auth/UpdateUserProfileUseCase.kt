package com.example.facefit.domain.usecases.auth

import com.example.facefit.domain.models.User
import com.example.facefit.domain.repository.UserRepository
import com.example.facefit.domain.utils.Resource
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Resource<Unit> {
        return userRepository.updateUserProfile(user)
    }
}
