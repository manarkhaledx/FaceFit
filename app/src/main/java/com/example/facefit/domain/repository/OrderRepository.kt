package com.example.facefit.domain.repository

import com.example.facefit.data.models.responses.OrderResponse
import com.example.facefit.domain.models.UserOrderResponse
import com.example.facefit.domain.utils.Resource

interface OrderRepository {
    suspend fun createOrder(address: String, phone: String, paymentMethod: String): Resource<OrderResponse>
    suspend fun getUserOrders(): Resource<UserOrderResponse>
}