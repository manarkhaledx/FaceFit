package com.example.facefit.domain.usecases.auth

import com.example.facefit.data.models.requests.SignUpRequest
import com.example.facefit.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(signUpRequest: SignUpRequest) =
        authRepository.signupUser(signUpRequest)
}