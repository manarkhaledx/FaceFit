package com.example.facefit.domain.models

import com.google.gson.annotations.SerializedName

data class Glasses(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val price: Double,
    val stock: Int,
    val images: List<String>,
    val shape: String,
    val weight: Double,
    val size: String,
    val material: String,
    val type: String,
    val gender: String,
    val colors: List<String> = listOf("#000000"),
    val createdAt: String,
    val numberOfSells: Int = 0,
    val isFavorite: Boolean = false,
    val tryOn: Boolean = false,
    val arModels: ARModels? = null
) {
    fun isInStock(): Boolean = stock > 0
    fun isSunglasses(): Boolean = type == "sunglasses"
}

data class ARModels(
    @SerializedName("modelArmsOBJ") val armsObj: String?,
    @SerializedName("modelArmsMTL") val armsMtl: String?,
    @SerializedName("modelLensesOBJ") val lensesObj: String?,
    @SerializedName("modelLensesMTL") val lensesMtl: String?,
    @SerializedName("modelFrameOBJ") val frameObj: String?,
    @SerializedName("modelFrameMTL") val frameMtl: String?,
    @SerializedName("modelArmsMaterial") val armsMaterials: List<String>?,
    @SerializedName("modelFrameMaterial") val frameMaterials: List<String>?
)