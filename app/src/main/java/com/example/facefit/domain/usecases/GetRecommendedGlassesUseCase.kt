package com.example.facefit.domain.usecases

import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.repository.GlassesRepository
import com.example.facefit.domain.utils.Resource
import javax.inject.Inject

class GetRecommendedGlassesUseCase @Inject constructor(
    private val repository: GlassesRepository
) {
    suspend operator fun invoke(
        currentProductId: String,
        gender: String,
        type: String,
        material: String
    ): Resource<List<Glasses>> {
        return repository.getRecommendedGlasses(
            currentProductId = currentProductId,
            gender = gender,
            type = type,
            material = material
        )
    }
}