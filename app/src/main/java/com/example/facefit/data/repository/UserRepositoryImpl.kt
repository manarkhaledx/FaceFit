package com.example.facefit.data.repository

import com.example.facefit.data.mapper.toDomainUser
import com.example.facefit.data.models.responses.UserResponse
import com.example.facefit.data.remote.ApiService
import com.example.facefit.domain.models.User
import com.example.facefit.domain.repository.UserRepository
import com.example.facefit.domain.utils.Resource
import retrofit2.Response
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
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
}
