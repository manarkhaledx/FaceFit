package com.example.facefit.data.remote

import com.example.facefit.data.models.requests.LoginRequest
import com.example.facefit.data.models.requests.SignUpRequest
import com.example.facefit.data.models.responses.LoginResponse
import com.example.facefit.data.models.responses.SignUpResponse
import com.example.facefit.domain.models.Glasses
import com.example.facefit.ui.utils.Constants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface ApiService {
    @POST(Constants.SIGNUP_ENDPOINT)
    suspend fun signupUser(@Body request: SignUpRequest): Response<SignUpResponse>

    @POST(Constants.LOGIN_ENDPOINT)
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @GET(Constants.BESTSELLERS_ENDPOINT)
    suspend fun getBestSellers(): Response<List<Glasses>>

    @GET(Constants.NEWARRIVALS_ENDPOINT)
    suspend fun getNewArrivals(): Response<List<Glasses>>

    @GET(Constants.ALL_GLASSES_ENDPOINT)
    suspend fun getAllGlasses(): Response<List<Glasses>>

    @GET(Constants.FILTER_GLASSES_ENDPOINT)
    suspend fun filterGlasses(
        @Query("type") type: String? = null,
        @Query("gender") gender: String? = null,
        @Query("size") size: String? = null,
        @QueryMap priceRange: Map<String, String>? = null,
        @Query("shape") shape: String? = null,
        @Query("material") material: String? = null,
        @Query("sort") sort: String? = null
    ): Response<List<Glasses>>
}
