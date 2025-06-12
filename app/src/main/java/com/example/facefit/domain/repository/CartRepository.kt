package com.example.facefit.domain.repository

import com.example.facefit.data.models.requests.AddToCartRequest
import com.example.facefit.data.models.requests.UpdateCartItemRequest
import com.example.facefit.domain.models.CartData
import com.example.facefit.domain.utils.Resource

interface CartRepository {
    suspend fun getCart(): Resource<CartData>
    suspend fun addToCart(request: AddToCartRequest): Resource<CartData>
    suspend fun updateCartItem(itemId: String, request: UpdateCartItemRequest): Resource<CartData>
    suspend fun removeCartItem(itemId: String): Resource<CartData>
    suspend fun clearCart(): Resource<CartData>
}