package com.example.facefit.data.remote

import com.example.facefit.data.models.requests.LoginRequest
import com.example.facefit.data.models.requests.SignUpRequest
import com.example.facefit.data.models.requests.SubmitReviewRequest
import com.example.facefit.data.models.responses.FavoritesResponse
import com.example.facefit.data.models.responses.LoginResponse
import com.example.facefit.data.models.responses.ReviewsResponse
import com.example.facefit.data.models.responses.SignUpResponse
import com.example.facefit.data.models.responses.UserResponse
import com.example.facefit.domain.models.Glasses
import com.example.facefit.ui.utils.Constants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
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
        @QueryMap priceRange: Map<String, String> = emptyMap(),
        @Query("shape") shape: String? = null,
        @Query("material") material: String? = null,
        @Query("sort") sort: String? = null
    ): Response<List<Glasses>>

    @GET(Constants.SINGLE_GLASSES_ENDPOINT)
    suspend fun getGlassesById(@Query("_id") id: String): Response<List<Glasses>>

    @GET(Constants.GET_FAVORITES_ENDPOINT)
    suspend fun getFavorites(@Header("Authorization") token: String): Response<FavoritesResponse<List<Glasses>>>

    @POST(Constants.FAVORITES_ENDPOINT)
    suspend fun toggleFavorite(
        @Header("Authorization") token: String,
        @Path("glassesid") glassesId: String
    ): Response<Unit>

    @GET(Constants.GET_REVIEWS_ENDPOINT)
    suspend fun getReviews(
        @Header("Authorization") token: String,
        @Path("glassesId") glassesId: String
    ): Response<ReviewsResponse> {
        return getReviews(token, glassesId)
    }

    @GET(Constants.GET_CUSTOMER_PROFILE_ENDPOINT)
    suspend fun getCustomerProfile(
        @Header("Authorization") token: String
    ): Response<UserResponse>

    @POST(Constants.SUBMIT_REVIEW_ENDPOINT)
    suspend fun submitReview(
        @Header("Authorization") token: String,
        @Body request: SubmitReviewRequest
    ): Response<Unit>

}
