package com.example.facefit.domain.usecases.auth

import com.example.facefit.data.models.requests.LoginRequest
import com.example.facefit.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(loginRequest: LoginRequest) =
        authRepository.loginUser(loginRequest)
}