package com.example.facefit.data.models.requests

import com.google.gson.annotations.SerializedName

data class AddToCartRequest(
    val glassesId: String,
    val quantity: Int = 1,
    val color: String,
    val size: String,
    val lenseType: String,
    val lensSpecification: String? = null,
    val lensPrice: Double? = null,
    @SerializedName("prescription")
    val prescriptionId: String? = null
)