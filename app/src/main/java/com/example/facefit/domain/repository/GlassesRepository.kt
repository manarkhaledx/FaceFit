package com.example.facefit.domain.repository

import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.utils.Resource

interface GlassesRepository {
    suspend fun getBestSellers(): Resource<List<Glasses>>
    suspend fun getNewArrivals(): Resource<List<Glasses>>
}