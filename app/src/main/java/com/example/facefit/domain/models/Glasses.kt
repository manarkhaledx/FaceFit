package com.example.facefit.domain.models

data class Glasses(
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
    val colors: List<String>,
    val numberOfRatings: Int = 0,
    val rate: Double = 0.0,
    // TODO(): List<Review>
    val reviews: List<String> = emptyList(),
    val createdAt: String,
    val numberOfSells: Int = 0,
){
    fun isInStock(): Boolean = stock > 0
    fun isSunglasses(): Boolean = type == "sunglasses"
}