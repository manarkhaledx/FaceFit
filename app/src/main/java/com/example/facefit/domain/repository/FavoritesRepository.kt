package com.example.facefit.domain.repository

import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.utils.Resource

interface FavoritesRepository {
    suspend fun getFavorites(token: String): Resource<List<Glasses>>
    suspend fun toggleFavorite(token: String, glassesId: String): Resource<Unit>
}