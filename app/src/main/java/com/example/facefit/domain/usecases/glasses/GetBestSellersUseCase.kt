package com.example.facefit.domain.usecases.glasses

import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.repository.GlassesRepository
import com.example.facefit.domain.utils.Resource
import javax.inject.Inject

class GetBestSellersUseCase @Inject constructor(
    private val repository: GlassesRepository
) {
    suspend operator fun invoke(): Resource<List<Glasses>> {
        return repository.getBestSellers()
    }
}