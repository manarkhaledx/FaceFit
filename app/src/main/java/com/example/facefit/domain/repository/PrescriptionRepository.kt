package com.example.facefit.domain.repository

import com.example.facefit.data.models.requests.CreatePrescriptionRequest
import com.example.facefit.data.models.responses.PrescriptionResponse
import com.example.facefit.domain.utils.Resource

interface PrescriptionRepository {
    suspend fun createPrescription(request: CreatePrescriptionRequest): Resource<PrescriptionResponse>
}