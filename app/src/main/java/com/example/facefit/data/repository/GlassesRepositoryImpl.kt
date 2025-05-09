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
            println("Repository - Filtering with minPrice: $minPrice, maxPrice: $maxPrice")

            // Create price range map according to backend expectations
            val priceRange = mutableMapOf<String, String>()
            minPrice?.let { priceRange["price[gte]"] = it.toString() }
            maxPrice?.let { priceRange["price[lte]"] = it.toString() }

            val response = apiService.filterGlasses(
                type = type,
                gender = gender,
                size = null,
                priceRange = if (priceRange.isNotEmpty()) priceRange else null,
                shape = shape,
                material = material,
                sort = sort
            )

            println("API Response: ${response.code()} - ${response.message()}")
            println("Received ${response.body()?.size ?: 0} products")

            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error(
                    context.getString(R.string.failed_to_filter_glasses, response.message()),
                    createPlaceholderGlasses(6)
                )
            }
        } catch (e: Exception) {
            println("Filter Exception: ${e.message}")
            Resource.Error(
                e.message ?: context.getString(R.string.an_error_occurred),
                createPlaceholderGlasses(6)
            )
        }
    }

    private fun createPlaceholderGlasses(count: Int): List<Glasses> {
        return List(count) { index ->
            Glasses(
                id = "placeholder_$index",
                name = "Loading...",
                price = 0.0,
                stock = 0,
                images = listOf(R.drawable.eye_glasses.toString()),
                shape = "",
                weight = 0.0,
                size = "",
                material = "",
                type = "",
                gender = "",
                colors = listOf("#000000"),
                createdAt = "",
                numberOfRatings = 0,
                rate = 0.0,
                reviews = emptyList(),
                numberOfSells = 0,
                isFavorite = false
            )
        }
    }
}