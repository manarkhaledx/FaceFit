package com.example.facefit.data.repository

import android.util.Log
import com.example.facefit.data.local.TokenManager
import com.example.facefit.data.models.requests.AddToCartRequest
import com.example.facefit.data.models.requests.UpdateCartItemRequest
import com.example.facefit.data.remote.ApiService
import com.example.facefit.domain.models.CartData
import com.example.facefit.domain.repository.CartRepository
import com.example.facefit.domain.utils.Resource
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : CartRepository {

    override suspend fun getCart(): Resource<CartData> {
        return try {
            val token = tokenManager.getToken() ?: return Resource.Error("Not authenticated")
            val response = apiService.getCart("Bearer $token")
            if (response.isSuccessful) {
                Resource.Success(response.body()?.data ?: return Resource.Error("Cart data is null"))
            } else {
                val errorBody = response.errorBody()?.string() // Read error body if exists
                Log.e("CartRepository", """
        Failed to fetch cart. 
        Code: ${response.code()}
        Message: ${response.message()}
        Error Body: $errorBody
    """.trimIndent())
                Resource.Error(errorBody ?: "HTTP ${response.code()}") // Use error body or status code
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun addToCart(request: AddToCartRequest): Resource<CartData> {
        return try {
            val token = tokenManager.getToken() ?: return Resource.Error("Not authenticated")
            val response = apiService.addToCart("Bearer $token", request)
            if (response.isSuccessful) {
                getCart() // Return updated cart
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (errorBody?.contains("Insufficient stock") == true) {
                    errorBody.substringAfter("\"error\":\"").substringBefore("\"")
                } else {
                    response.message()
                }
                Log.e("CartRepository", """
                Failed to fetch cart. 
                Code: ${response.code()}
                Message: ${response.message()}
                Error Body: $errorBody
                """.trimIndent())
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun updateCartItem(itemId: String, request: UpdateCartItemRequest): Resource<CartData> {
        return try {
            val token = tokenManager.getToken() ?: return Resource.Error("Not authenticated")
            val response = apiService.updateCartItem("Bearer $token", itemId, request)
            if (response.isSuccessful) {
                getCart() // Return updated cart
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (errorBody?.contains("Insufficient stock") == true) {
                    errorBody.substringAfter("\"error\":\"").substringBefore("\"")
                } else {
                    response.message()
                }
                Log.e("CartRepository", """
                Failed to update cart item. 
                Code: ${response.code()}
                Message: $errorMessage
                Error Body: $errorBody
            """.trimIndent())
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun removeCartItem(itemId: String): Resource<CartData> {
        return try {
            val token = tokenManager.getToken() ?: return Resource.Error("Not authenticated")
            val response = apiService.removeCartItem("Bearer $token", itemId)
            if (response.isSuccessful) {
                getCart() // Return updated cart
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CartRepository", """
                Failed to fetch cart. 
                Code: ${response.code()}
                Message: ${response.message()}
                Error Body: $errorBody
                """.trimIndent())
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun clearCart(): Resource<CartData> {
        return try {
            val token = tokenManager.getToken() ?: return Resource.Error("Not authenticated")
            val response = apiService.clearCart("Bearer $token")
            if (response.isSuccessful) {
                getCart() // Return updated cart (should be empty)
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}