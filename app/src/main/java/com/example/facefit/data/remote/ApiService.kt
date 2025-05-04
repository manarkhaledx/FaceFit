package com.example.facefit.data.remote

import com.example.facefit.data.models.SignUpRequest
import com.example.facefit.data.models.SignUpResponse
import com.example.facefit.ui.utils.Constants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST(Constants.SIGNUP_ENDPOINT)
    suspend fun signupUser(@Body request: SignUpRequest): Response<SignUpResponse>
}
