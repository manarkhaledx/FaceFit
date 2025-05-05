package com.example.facefit.domain.repository

import com.example.facefit.data.models.requests.LoginRequest
import com.example.facefit.data.models.requests.SignUpRequest
import com.example.facefit.data.models.responses.LoginResponse
import com.example.facefit.data.models.responses.SignUpResponse
import retrofit2.Response

interface AuthRepository {
    suspend fun signupUser(signUpRequest: SignUpRequest): Response<SignUpResponse>
    suspend fun loginUser(loginRequest: LoginRequest): Response<LoginResponse>
}
