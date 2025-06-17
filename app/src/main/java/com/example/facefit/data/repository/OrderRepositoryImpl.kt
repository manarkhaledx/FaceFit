package com.example.facefit.data.repository

import android.util.Log
import com.example.facefit.data.local.TokenManager
import com.example.facefit.data.models.requests.OrderRequest
import com.example.facefit.data.models.responses.OrderResponse
import com.example.facefit.data.remote.ApiService
import com.example.facefit.domain.models.UserOrderResponse
import com.example.facefit.domain.repository.OrderRepository
import com.example.facefit.domain.utils.Resource
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : OrderRepository {

    override suspend fun createOrder(
        address: String,
        phone: String,
        paymentMethod: String
    ): Resource<OrderResponse> {
        return try {
            val token = tokenManager.getToken() ?: return Resource.Error("Not authenticated")
            val response = apiService.createOrder(
                "Bearer $token",
                OrderRequest(address, phone, paymentMethod)
            )

            if (response.isSuccessful) {
                val orderResponse = response.body()
                if (orderResponse != null && orderResponse.status == "success") {
                    Resource.Success(orderResponse)
                } else {
                    Resource.Error("Unexpected response structure or status: ${response.message()}")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(
                    "OrderRepository", """
                    Failed to create order. 
                    Code: ${response.code()}
                    Message: ${response.message()}
                    Error Body: $errorBody
                """.trimIndent()
                )

                // Parse stock error specifically
                val errorMessage = if (errorBody?.contains("Stock validation failed") == true) {
                    errorBody.substringAfter("\"error\":\"").substringBefore("\"")
                } else {
                    errorBody ?: "HTTP ${response.code()}"
                }

                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred while creating order")
        }
    }

    override suspend fun getUserOrders(): Resource<UserOrderResponse> {
        return try {
            val token = tokenManager.getToken() ?: return Resource.Error("Not authenticated")
            val response = apiService.getUserOrders("Bearer $token")

            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Empty response")
            } else {
                Resource.Error(response.errorBody()?.string() ?: "Unknown error")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}