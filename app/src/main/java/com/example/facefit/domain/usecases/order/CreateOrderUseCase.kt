package com.example.facefit.domain.usecases.order

import com.example.facefit.data.models.responses.OrderResponse
import com.example.facefit.domain.repository.OrderRepository
import com.example.facefit.domain.utils.Resource
import javax.inject.Inject

class CreateOrderUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(
        address: String,
        phone: String,
        paymentMethod: String
    ): Resource<OrderResponse> {
        return repository.createOrder(address, phone, paymentMethod)
    }
}