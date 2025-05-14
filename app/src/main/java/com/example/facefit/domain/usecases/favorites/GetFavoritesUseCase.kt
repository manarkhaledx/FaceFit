package com.example.facefit.domain.usecases.favorites

import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.repository.FavoritesRepository
import com.example.facefit.domain.utils.Resource
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val repository: FavoritesRepository
) {
    suspend operator fun invoke(token: String): Resource<List<Glasses>> {
        return repository.getFavorites(token)
    }
}