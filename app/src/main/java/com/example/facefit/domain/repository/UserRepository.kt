package com.example.facefit.domain.repository

import com.example.facefit.domain.models.User
import com.example.facefit.domain.utils.Resource

interface UserRepository {
    suspend fun getUserProfile(token: String): Resource<User>
    suspend fun updateUserProfile(user: User): Resource<Unit>
    suspend fun checkEmailExists(email: String): Resource<Boolean>
}
