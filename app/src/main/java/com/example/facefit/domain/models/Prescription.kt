package com.example.facefit.domain.models

import com.google.gson.annotations.SerializedName

data class PrescriptionData(
    val _id: String,
    @SerializedName("OS")
    val rightEye: EyePrescription,
    @SerializedName("OD")
    val leftEye: EyePrescription,
    @SerializedName("PD")
    val pdDistance: PdMeasurement,
)
data class EyePrescription(
    @SerializedName("SPH")
    val sphere: Double,
    @SerializedName("CYL")
    val cylinder: Double,
    @SerializedName("AXIS")
    val axis: Int
)

data class PdMeasurement(
    @SerializedName("singlePD")
    val singlePd: Double? = null,
    @SerializedName("dualPD")
    val dualPd: DualPd? = null
)

data class DualPd(
    val left: Double,
    val right: Double
)