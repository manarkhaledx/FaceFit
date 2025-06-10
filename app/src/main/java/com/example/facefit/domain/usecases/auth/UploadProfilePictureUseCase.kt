package com.example.facefit.domain.usecases.auth

import com.example.facefit.domain.models.User
import com.example.facefit.domain.repository.UserRepository
import com.example.facefit.domain.utils.Resource
import okhttp3.MultipartBody // Make sure this import is present!
import javax.inject.Inject

// Ensure this file exists and is in this package
class UploadProfilePictureUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(image: MultipartBody.Part): Resource<User> {
        return userRepository.uploadProfilePicture(image)
    }
}