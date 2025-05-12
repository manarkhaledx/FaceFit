package com.example.facefit.domain.usecases

import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.repository.GlassesRepository
import com.example.facefit.domain.utils.Resource
import javax.inject.Inject

class GetGlassesByIdUseCase @Inject constructor(
    private val repository: GlassesRepository
) {
    suspend operator fun invoke(id: String): Resource<Glasses> {
        return repository.getGlassesById(id)
    }
}