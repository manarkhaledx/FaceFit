package com.example.facefit.domain.models

import com.google.gson.annotations.SerializedName

data class CartItem(
    @SerializedName("_id") val id: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("item") val glasses: Glasses,
    @SerializedName("size") val size: String,
    @SerializedName("price") val price: Double,
    @SerializedName("color") val color: String,
    @SerializedName("lenseType") val lensType: String,
    @SerializedName("lensSpecification") val lensSpecification: String,
    @SerializedName("lensPrice") val lensPrice: Double,
    @SerializedName("prescription") val prescription: PrescriptionData?
)

data class CartItemWithGlasses(
    val cartItem: CartItem,
    val glasses: Glasses?
)