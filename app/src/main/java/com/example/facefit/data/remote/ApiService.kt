package com.example.facefit.data.remote

import com.example.facefit.data.models.requests.AddToCartRequest
import com.example.facefit.data.models.requests.CreatePrescriptionRequest
import com.example.facefit.data.models.requests.LoginRequest
import com.example.facefit.data.models.requests.SignUpRequest
import com.example.facefit.data.models.requests.SubmitReviewRequest
import com.example.facefit.data.models.requests.UpdateCartItemRequest
import com.example.facefit.data.models.requests.UpdateUserRequest
import com.example.facefit.data.models.responses.CartResponse
import com.example.facefit.data.models.responses.CheckEmailResponse
import com.example.facefit.data.models.responses.FavoritesResponse
import com.example.facefit.data.models.responses.LoginResponse
import com.example.facefit.data.models.responses.PrescriptionResponse
import com.example.facefit.data.models.responses.ReviewsResponse
import com.example.facefit.data.models.responses.SignUpResponse
import com.example.facefit.data.models.responses.UserResponse
import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.models.User
import com.example.facefit.ui.utils.Constants
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
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
        // This is a common pattern for Retrofit methods but often the body is directly in the interface.
        // If this causes issues, you can remove the { return getReviews(token, glassesId) }
        // Retrofit will implement it for you.
        // It's likely fine, but worth noting.
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


    @PUT(Constants.UPDATE_USER_PROFILE_ENDPOINT)
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateUserRequest
    ): Response<Unit>

    @Multipart
    @POST(Constants.UPLOAD_PROFILE_PICTURE_ENDPOINT)
    suspend fun uploadProfilePicture(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Response<UserResponse>

    @POST(Constants.CREATE_PRESCRIPTION_ENDPOINT)
    suspend fun createPrescription(
        @Header("Authorization") token: String,
        @Body request: CreatePrescriptionRequest
    ): Response<PrescriptionResponse>

    @POST(Constants.ADD_TO_CART_ENDPOINT)
    suspend fun addToCart(
        @Header("Authorization") token: String,
        @Body request: AddToCartRequest
    ): Response<CartResponse>

    @GET(Constants.GET_CART_ENDPOINT)
    suspend fun getCart(
        @Header("Authorization") token: String
    ): Response<CartResponse>

    @PUT(Constants.UPDATE_CART_ITEM_ENDPOINT)
    suspend fun updateCartItem(
        @Header("Authorization") token: String,
        @Path("cartItemId") cartItemId: String,
        @Body request: UpdateCartItemRequest
    ): Response<CartResponse>

    @DELETE(Constants.REMOVE_CART_ITEM_ENDPOINT)
    suspend fun removeCartItem(
        @Header("Authorization") token: String,
        @Path("cartItemId") cartItemId: String
    ): Response<CartResponse>

    @DELETE(Constants.CLEAR_CART_ENDPOINT)
    suspend fun clearCart(
        @Header("Authorization") token: String
    ): Response<CartResponse>
}