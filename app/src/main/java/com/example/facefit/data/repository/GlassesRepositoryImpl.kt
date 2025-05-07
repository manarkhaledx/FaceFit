package com.example.facefit.data.repository

import com.example.facefit.data.remote.ApiService
import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.repository.GlassesRepository
import com.example.facefit.domain.utils.Resource
import javax.inject.Inject

class GlassesRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : GlassesRepository {
    override suspend fun getBestSellers(): Resource<List<Glasses>> {
        return try {
            val response = apiService.getBestSellers()
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error("Failed to fetch best sellers", createPlaceholderGlasses())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred", createPlaceholderGlasses())
        }
    }

    override suspend fun getNewArrivals(): Resource<List<Glasses>> {
        return try {
            val response = apiService.getNewArrivals()
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error("Failed to fetch new arrivals", createPlaceholderGlasses())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred", createPlaceholderGlasses())
        }
    }

    private fun createPlaceholderGlasses(): List<Glasses> {
        return List(3) { index ->
            Glasses(
                id = "placeholder_$index",
                name = "Loading...",
                price = 0.0,
                stock = 0,
                images = emptyList(),
                shape = "",
                weight = 0.0,
                size = "",
                material = "",
                type = "",
                gender = "",
                colors = emptyList(),
                createdAt = "",
                numberOfRatings = 0,
                rate = 0.0,
                reviews = emptyList(),
                numberOfSells = 0
            )
        }
    }
}