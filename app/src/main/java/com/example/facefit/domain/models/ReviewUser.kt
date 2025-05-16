package com.example.facefit.domain.models

import com.google.gson.annotations.SerializedName

data class ReviewUser(
    @SerializedName("_id") val id: String,
    val firstName: String,
    val lastName: String,
    @SerializedName("email") val email: String,
    @SerializedName("profilePictureUrl") val profilePicture: String?
) {
    val displayName: String
        get() = "$firstName $lastName"
}