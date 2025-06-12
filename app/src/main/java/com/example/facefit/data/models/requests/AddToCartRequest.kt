package com.example.facefit.data.models.requests

data class AddToCartRequest(
    val glassesId: String,
    val quantity: Int = 1,
    val color: String,
    val size: String,
    val lenseType: String,
    val lensSpecification: String? = null,
    val lensPrice: Double? = null,
    val prescriptionId: String? = null
)