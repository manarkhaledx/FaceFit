package com.example.facefit.data.models.responses

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("errors")
    val errors: List<FieldError>? = null
)

data class FieldError(
    @SerializedName("msg")
    val message: String? = null,

    @SerializedName("path")
    val field: String? = null
)


