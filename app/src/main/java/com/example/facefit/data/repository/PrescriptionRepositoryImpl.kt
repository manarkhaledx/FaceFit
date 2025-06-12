package com.example.facefit.data.repository

import android.util.Log
import com.example.facefit.data.local.TokenManager
import com.example.facefit.data.models.requests.CreatePrescriptionRequest
import com.example.facefit.data.models.responses.PrescriptionResponse
import com.example.facefit.data.remote.ApiService
import com.example.facefit.domain.repository.PrescriptionRepository
import com.example.facefit.domain.utils.Resource
import javax.inject.Inject

class PrescriptionRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : PrescriptionRepository {

    override suspend fun createPrescription(request: CreatePrescriptionRequest): Resource<PrescriptionResponse> {
        return try {
            val token = tokenManager.getToken() ?: return Resource.Error("Not authenticated")
            val response = apiService.createPrescription("Bearer $token", request)
            if (response.isSuccessful) {
                Log.e("PrescriptionRepository", "Prescription created successfully: ${response.body()}")
                Resource.Success(response.body() ?: return Resource.Error("Response body is null"))
            } else {
                Log.e("PrescriptionRepository", """
                    Failed to create prescription. 
                    Code: ${response.code()}
                    Message: ${response.message()}
                    Error Body: ${response.errorBody()?.string()}
                """.trimIndent())
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}