package com.example.facefit.domain.models

data class UserOrderResponse(
    val status: String,
    val data: List<Order>
)

data class Order(
    val _id: String,
    val date: String,
    val status: String,
    val subtotal: Double,
    val total: Double,
    val paymentMethod: String,
    val address: String,
    val phone: String,
    val items: List<OrderItem>
)

data class OrderItem(
    val item: OrderProduct,
    val quantity: Int,
    val price: Double
)

data class OrderProduct(
    val _id: String,
    val name: String,
    val price: Double,
    val images: List<String>
)