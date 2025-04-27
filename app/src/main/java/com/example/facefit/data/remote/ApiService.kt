package com.example.facefit.data.remote

import com.example.facefit.data.models.SignUpRequest
import com.example.facefit.data.models.SignUpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/api/auth/signup")
    suspend fun signupUser(@Body request: SignUpRequest): Response<SignUpResponse>
}