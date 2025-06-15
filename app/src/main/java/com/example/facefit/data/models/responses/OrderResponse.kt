package com.example.facefit.data.models.responses

data class OrderResponse(
    val status: String,
    val data: OrderData
)

data class OrderData(
    val status: String
)