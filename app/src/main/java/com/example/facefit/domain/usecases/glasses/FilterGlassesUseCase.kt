package com.example.facefit.domain.usecases.glasses

import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.repository.GlassesRepository
import com.example.facefit.domain.utils.Resource
import javax.inject.Inject

class FilterGlassesUseCase @Inject constructor(
    private val repository: GlassesRepository
) {
    suspend operator fun invoke(
        type: String? = null,
        gender: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        shape: String? = null,
        material: String? = null,
        sort: String? = null
    ): Resource<List<Glasses>> {
        return repository.filterGlasses(
            type = type,
            gender = gender,
            minPrice = minPrice,
            maxPrice = maxPrice,
            shape = shape,
            material = material,
            sort = sort
        )
    }
}