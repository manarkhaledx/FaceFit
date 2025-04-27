package com.example.facefit.domain.usecases

import com.example.facefit.data.models.SignUpRequest
import com.example.facefit.domain.repository.AuthRepository

class SignUpUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(signUpRequest: SignUpRequest) = authRepository.signupUser(signUpRequest)
}
