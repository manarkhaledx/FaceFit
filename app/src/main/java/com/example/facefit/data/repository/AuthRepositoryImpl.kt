package com.example.facefit.data.repository

import com.example.facefit.data.models.SignUpRequest
import com.example.facefit.data.models.SignUpResponse
import com.example.facefit.data.remote.ApiService
import com.example.facefit.domain.repository.AuthRepository
import retrofit2.Response

class AuthRepositoryImpl(
    private val apiService: ApiService
) : AuthRepository {
    override suspend fun signupUser(signUpRequest: SignUpRequest): Response<SignUpResponse> {
        return apiService.signupUser(signUpRequest)
    }
}
