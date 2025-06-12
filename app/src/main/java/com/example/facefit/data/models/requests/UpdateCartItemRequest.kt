package com.example.facefit.data.models.requests

data class UpdateCartItemRequest(
    val cartItemId: String,
    val quantity: Int,
    val prescriptionId: String? = null
)