package com.example.facefit.data.models.responses

import com.example.facefit.domain.models.PrescriptionData

data class PrescriptionResponse(
    val status: String,
    val data: PrescriptionData
)