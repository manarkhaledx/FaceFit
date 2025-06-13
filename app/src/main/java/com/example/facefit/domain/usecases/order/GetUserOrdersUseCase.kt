package com.example.facefit.domain.usecases.order

import com.example.facefit.domain.models.UserOrderResponse
import com.example.facefit.domain.repository.OrderRepository
import com.example.facefit.domain.utils.Resource
import javax.inject.Inject

class GetUserOrdersUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(): Resource<UserOrderResponse> {
        return repository.getUserOrders()
    }
}