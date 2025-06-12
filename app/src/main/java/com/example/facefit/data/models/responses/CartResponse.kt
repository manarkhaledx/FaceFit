package com.example.facefit.data.models.responses

import com.example.facefit.domain.models.CartData

data class CartResponse(
    val status: String,
    val data: CartData
)