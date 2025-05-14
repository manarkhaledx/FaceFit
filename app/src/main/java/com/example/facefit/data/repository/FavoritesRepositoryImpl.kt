package com.example.facefit.data.repository

import android.util.Log
import com.example.facefit.data.remote.ApiService
import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.repository.FavoritesRepository
import com.example.facefit.domain.utils.Resource
import javax.inject.Inject

class FavoritesRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : FavoritesRepository {
    override suspend fun getFavorites(token: String): Resource<List<Glasses>> {
        return try {
            val response = apiService.getFavorites("Bearer $token")
            if (response.isSuccessful) {
                Resource.Success(response.body()?.data ?: emptyList())
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("FavoritesRepo", "Failed to fetch favorites: $errorBody")
                Resource.Error("Failed to fetch favorites: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("FavoritesRepo", "Error fetching favorites", e)
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun toggleFavorite(token: String, glassesId: String): Resource<Unit> {
        return try {
            val response = apiService.toggleFavorite("Bearer $token", glassesId)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to toggle favorite")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }
}