package com.example.facefit.domain.usecases.prescription

import com.example.facefit.data.models.requests.CreatePrescriptionRequest
import com.example.facefit.data.models.responses.PrescriptionResponse
import com.example.facefit.domain.repository.PrescriptionRepository
import com.example.facefit.domain.utils.Resource
import javax.inject.Inject

class CreatePrescriptionUseCase @Inject constructor(
    private val repository: PrescriptionRepository
) {
    suspend operator fun invoke(request: CreatePrescriptionRequest): Resource<PrescriptionResponse> {
        return repository.createPrescription(request)
    }
}