package com.example.facefit.domain.models

import com.google.gson.annotations.SerializedName

data class CartData(
    val _id: String,
    val items: List<CartItem>,
    @SerializedName("total")
    val totalAmount: Double
)