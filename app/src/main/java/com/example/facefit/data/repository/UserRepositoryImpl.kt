package com.example.facefit.data.repository

import com.example.facefit.data.local.TokenManager
import com.example.facefit.data.mapper.toDomainUser
import com.example.facefit.data.mapper.toUpdateRequest
import com.example.facefit.data.remote.ApiService
import com.example.facefit.domain.models.User
import com.example.facefit.domain.repository.UserRepository
import com.example.facefit.domain.utils.Resource
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
                    Resource.Error("Unexpected response structure")
                }
            } else {
                Resource.Error("Error: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Exception: ${e.message}")
        }
    }

    override suspend fun updateUserProfile(user: User): Resource<Unit> {
        return try {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) return Resource.Error("Missing token")

            val response = apiService.updateUserProfile(
                token = "Bearer $token",
                request = user.toUpdateRequest()
            )

            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Update failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Exception: ${e.message}")
        }
    }





    override suspend fun checkEmailExists(email: String): Resource<Boolean> {
        return try {
            val response = apiService.checkEmailExists(email)
            if (response.isSuccessful) {
                // Handle different response structures
                val body = response.body()
                when {
                    body != null -> Resource.Success(body.exists)
                    else -> Resource.Error("Invalid response format")
                }
            } else {
                Resource.Error("Failed to check email: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.message}")
        }
    }

}
