package com.example.facefit.domain.usecases.favorites

import com.example.facefit.domain.repository.FavoritesRepository
import com.example.facefit.domain.utils.Resource
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: FavoritesRepository
) {
    suspend operator fun invoke(token: String, glassesId: String): Resource<Unit> {
        return repository.toggleFavorite(token, glassesId)
    }
}