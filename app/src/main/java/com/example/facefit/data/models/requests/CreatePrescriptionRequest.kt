package com.example.facefit.data.models.requests

import com.example.facefit.domain.models.EyePrescription
import com.example.facefit.domain.models.PdMeasurement
import com.google.gson.annotations.SerializedName

data class CreatePrescriptionRequest(
    @SerializedName("OS")
    val rightEye: EyePrescription,
    @SerializedName("OD")
    val leftEye: EyePrescription,
    @SerializedName("PD")
    val pd: PdMeasurement
)