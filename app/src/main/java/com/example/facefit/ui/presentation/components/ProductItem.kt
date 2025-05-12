package com.example.facefit.ui.presentation.components

import com.example.facefit.domain.models.Glasses

data class ProductItem(
    val id: String,
    val name: String,
    val price: String,
    val imageUrl: String? = null,
    val isFavorite: Boolean,
    val isPlaceholder: Boolean = false
)


fun Glasses.toProductItem(): ProductItem {
    val isPlaceholder = id?.startsWith("placeholder_") ?: false
    return ProductItem(
        id = this.id,
        name = if (isPlaceholder) "Loading..." else this.name,
        price = if (isPlaceholder) "---" else "EGP ${this.price}",
        imageUrl = this.images.firstOrNull(),
        isFavorite = this.isFavorite,
        isPlaceholder = isPlaceholder
    )
}