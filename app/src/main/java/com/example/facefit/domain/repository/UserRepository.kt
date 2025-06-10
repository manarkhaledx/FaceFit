package com.example.facefit.domain.repository

import com.example.facefit.domain.models.User
import com.example.facefit.domain.utils.Resource
import okhttp3.MultipartBody // Make sure this import is present!

interface UserRepository {
    suspend fun getUserProfile(token: String): Resource<User>
    suspend fun updateUserProfile(user: User): Resource<Unit>
    // Make sure this new function is here
    suspend fun uploadProfilePicture(image: MultipartBody.Part): Resource<User>
}