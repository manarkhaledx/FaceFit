package com.example.facefit.data.repository

import android.annotation.SuppressLint
import android.content.Context
import com.example.facefit.R
import com.example.facefit.data.remote.ApiService
import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.repository.GlassesRepository
import com.example.facefit.domain.utils.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GlassesRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) : GlassesRepository {
    override suspend fun getBestSellers(): Resource<List<Glasses>> {
        return try {
            val response = apiService.getBestSellers()
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error(context.getString(R.string.failed_to_fetch_best_sellers), createPlaceholderGlasses(3))
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: context.getString(R.string.an_error_occurred), createPlaceholderGlasses(3))
        }
    }

    override suspend fun getNewArrivals(): Resource<List<Glasses>> {
        return try {
            val response = apiService.getNewArrivals()
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error(context.getString(R.string.failed_to_fetch_new_arrivals), createPlaceholderGlasses(3))
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: context.getString(R.string.an_error_occurred), createPlaceholderGlasses(3))
        }
    }

    override suspend fun getAllGlasses(): Resource<List<Glasses>> {
        return try {
            val response = apiService.getAllGlasses()
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error(context.getString(R.string.error_loading_products), createPlaceholderGlasses(6))
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: context.getString(R.string.an_error_occurred), createPlaceholderGlasses(6))
        }
    }

    @SuppressLint("StringFormatInvalid")
    override suspend fun filterGlasses(
        type: String?,
        gender: String?,
        minPrice: Double?,
        maxPrice: Double?,
        shape: String?,
        material: String?,
        sort: String?
    ): Resource<List<Glasses>> {
        return try {
            val priceRange = mutableMapOf<String, String>()
            if (minPrice != null) {
                priceRange["price[gte]"] = minPrice.toString()
            }
            if (maxPrice != null) {
                priceRange["price[lte]"] = maxPrice.toString()
            }

            val response = apiService.filterGlasses(
                type = type,
                gender = gender,
                size = null,
                priceRange = priceRange,
                shape = shape,
                material = material,
                sort = sort
            )


            if (response.isSuccessful) {
                val products = response.body() ?: emptyList()
                Resource.Success(products)
            } else {
                Resource.Error(
                    "Failed to filter glasses: ${response.message()} (${response.code()})",
                    createPlaceholderGlasses(6)
                )
            }
        } catch (e: Exception) {
            Resource.Error(
                "Error: ${e.message ?: "Unknown error"}",
                createPlaceholderGlasses(6)
            )
        }
    }

    private fun createPlaceholderGlasses(count: Int): List<Glasses> {
        return List(count) { index ->
            Glasses(
                id = "placeholder_$index",
                name = "Placeholder Glasses $index",
                price = 0.0,
                images = emptyList(),
                colors = emptyList(),
                isFavorite = false,
                stock = 0,
                shape = "",
                weight = 0.0,
                size = "",
                material = "",
                type = "",
                gender = "",
                createdAt = "",
                tryOn = false,
                arModels = null
            )
        }
    }

    override suspend fun getGlassesById(id: String): Resource<Glasses> {
        return try {
            val response = apiService.getGlassesById(id)
            if (response.isSuccessful) {
                val glassesList = response.body() ?: emptyList()
                if (glassesList.isNotEmpty()) {
                    Resource.Success(glassesList.first())
                } else {
                    Resource.Error(context.getString(R.string.product_not_found), createPlaceholderGlasses(1).first())
                }
            } else {
                Resource.Error(context.getString(R.string.failed_to_fetch_product), createPlaceholderGlasses(1).first())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: context.getString(R.string.an_error_occurred), createPlaceholderGlasses(1).first())
        }
    }

    override suspend fun getRecommendedGlasses(
        currentProductId: String,
        gender: String,
        type: String,
        material: String
    ): Resource<List<Glasses>> {
        return try {
            val response = apiService.filterGlasses(
                type = type,
                gender = gender,
                material = material,
                size = null,
                priceRange = emptyMap(),
                shape = null,
                sort = null
            )

            if (response.isSuccessful) {
                val allMatchingProducts = response.body() ?: emptyList()
                // Filter out the current product and limit to 4 recommendations
                val recommendations = allMatchingProducts
                    .filter { it.id != currentProductId }
                    .take(6)
                Resource.Success(recommendations)
            } else {
                Resource.Error(
                    "Failed to get recommendations: ${response.message()}",
                    createPlaceholderGlasses(6)
                )
            }
        } catch (e: Exception) {
            Resource.Error(
                "Error: ${e.message ?: "Unknown error"}",
                createPlaceholderGlasses(6)
            )
        }
    }


}