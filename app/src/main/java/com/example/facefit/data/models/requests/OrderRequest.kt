package com.example.facefit.data.models.requests

data class OrderRequest(
    val address: String,
    val phone: String,
    val paymentMethod: String
)