package com.example.facefit.domain.repository

import com.example.facefit.data.models.SignUpRequest
import com.example.facefit.data.models.SignUpResponse
import retrofit2.Response

interface AuthRepository {
    suspend fun signupUser(signUpRequest: SignUpRequest): Response<SignUpResponse>
}
