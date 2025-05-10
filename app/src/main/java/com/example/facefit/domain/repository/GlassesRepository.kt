package com.example.facefit.domain.repository

import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.utils.Resource

interface GlassesRepository {
    suspend fun getBestSellers(): Resource<List<Glasses>>
    suspend fun getNewArrivals(): Resource<List<Glasses>>
    suspend fun getAllGlasses(): Resource<List<Glasses>>
    suspend fun filterGlasses(
        type: String? = null,
        gender: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        shape: String? = null,
        material: String? = null,
        sort: String? = null
    ): Resource<List<Glasses>>

    suspend fun getGlassesById(id: String): Resource<Glasses>
}