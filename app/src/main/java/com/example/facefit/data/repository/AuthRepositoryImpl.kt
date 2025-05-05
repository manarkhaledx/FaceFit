package com.example.facefit.data.repository

import com.example.facefit.data.models.requests.LoginRequest
import com.example.facefit.data.models.requests.SignUpRequest
import com.example.facefit.data.models.responses.LoginResponse
import com.example.facefit.data.models.responses.SignUpResponse
import com.example.facefit.data.remote.ApiService
import com.example.facefit.domain.repository.AuthRepository
import retrofit2.Response
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AuthRepository {
    override suspend fun signupUser(signUpRequest: SignUpRequest): Response<SignUpResponse> {
        return apiService.signupUser(signUpRequest)
    }

    override suspend fun loginUser(loginRequest: LoginRequest): Response<LoginResponse> {
        return apiService.loginUser(loginRequest)
    }
}
