package com.example.facefit.domain.usecases.cart

import com.example.facefit.data.models.requests.AddToCartRequest
import com.example.facefit.data.models.requests.UpdateCartItemRequest
import com.example.facefit.domain.models.CartData
import com.example.facefit.domain.repository.CartRepository
import com.example.facefit.domain.utils.Resource
import javax.inject.Inject

class GetCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(): Resource<CartData> {
        return repository.getCart()
    }
}

class AddToCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(request: AddToCartRequest): Resource<CartData> {
        return repository.addToCart(request)
    }
}

class UpdateCartItemUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(
        itemId: String,
        quantity: Int,
        prescriptionId: String? = null
    ): Resource<CartData> {
        return repository.updateCartItem(
            itemId,
            UpdateCartItemRequest(itemId, quantity, prescriptionId)
        )
    }
}

class RemoveCartItemUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(itemId: String): Resource<CartData> {
        return repository.removeCartItem(itemId)
    }
}

class ClearCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(): Resource<CartData> {
        return repository.clearCart()
    }
}

class GetCartItemCountUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(): Resource<Int> {
        return when (val result = repository.getCart()) {
            is Resource.Success -> Resource.Success(result.data?.items?.sumOf { it.quantity } ?: 0)
            is Resource.Error -> Resource.Error(result.message ?: "Unknown error")
            is Resource.Loading -> Resource.Loading()
        }
    }
}