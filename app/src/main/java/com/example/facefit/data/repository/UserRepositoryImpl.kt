package com.example.facefit.data.repository

import android.util.Log
import com.example.facefit.data.local.TokenManager
import com.example.facefit.data.mapper.toDomainUser
import com.example.facefit.data.mapper.toUpdateRequest
import com.example.facefit.data.remote.ApiService
import com.example.facefit.domain.models.User
import com.example.facefit.domain.repository.UserRepository
import com.example.facefit.domain.utils.Resource
import okhttp3.MultipartBody // Make sure this import is present!
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : UserRepository {

    override suspend fun getUserProfile(token: String): Resource<User> {
        return try {
            val response = apiService.getCustomerProfile("Bearer $token")
            if (response.isSuccessful) {
                val userResponse = response.body()
                if (userResponse != null && userResponse.status == "success") {
                    Resource.Success(userResponse.data.toDomainUser())
                } else {
                    Resource.Error("Unexpected response structure or status: ${response.message()}")
                }
            } else {
                Resource.Error(
                    response.errorBody()?.string() ?: "Error fetching profile: ${response.code()}"
                )
            }
        } catch (e: Exception) {
            Resource.Error("Exception fetching profile: ${e.message}")
        }
    }

    override suspend fun updateUserProfile(user: User): Resource<Unit> {
        return try {

            val token = tokenManager.getToken()
            Log.d("UploadDebug", "Token before upload: $token") // ← أضف دي هنا
            if (token.isNullOrEmpty()) return Resource.Error("Missing token")


            val response = apiService.updateUserProfile(
                token = "Bearer $token",
                request = user.toUpdateRequest()
            )

            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Update failed with unknown error")
            }
        } catch (e: Exception) {
            Resource.Error("Exception updating profile: ${e.message}")
        }
    }

    // Ensure this implementation matches exactly
    override suspend fun uploadProfilePicture(image: MultipartBody.Part): Resource<User> {
        return try {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) return Resource.Error("Missing token")

            val response = apiService.uploadProfilePicture(
                token = "Bearer $token",
                image = image
            )

            if (response.isSuccessful) {
                val userResponse = response.body()

                Log.d("UploadDebug", "Raw API userResponse = $userResponse")
                Log.d("UploadDebug", "Raw data = ${userResponse?.data}")

                if (userResponse != null && userResponse.status == "success") {
                    val updatedProfilePicture = userResponse.data?.profilePicture
                    val currentUser = getUserProfile(token).let {
                        if (it is Resource.Success && it.data != null) it.data else null
                    }

                    return if (currentUser != null && updatedProfilePicture != null) {
                        val newUser = currentUser.copy(profilePicture = updatedProfilePicture)
                        Resource.Success(newUser)
                    } else {
                        Resource.Error("Failed to combine user data with new profile picture.")
                    }
                } else {
                    Resource.Error("Failed to upload profile picture: Unexpected response structure or status.")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Resource.Error(errorBody ?: "Failed to upload profile picture: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Exception uploading profile picture: ${e.message}")
        }
    }
}
