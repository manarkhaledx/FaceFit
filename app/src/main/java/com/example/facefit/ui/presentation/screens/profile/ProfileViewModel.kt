package com.example.facefit.ui.presentation.screens.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.data.local.TokenManager
import com.example.facefit.data.models.responses.ErrorResponse
import com.example.facefit.data.models.responses.GenericBackendError
import com.example.facefit.domain.models.Order
import com.example.facefit.domain.models.User
import com.example.facefit.domain.usecases.auth.GetUserProfileUseCase
import com.example.facefit.domain.usecases.auth.UpdateUserProfileUseCase
import com.example.facefit.domain.usecases.auth.UploadProfilePictureUseCase
import com.example.facefit.domain.usecases.order.GetUserOrdersUseCase
import com.example.facefit.domain.utils.Resource
import com.example.facefit.domain.utils.validators.ProfileValidator
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val getUserProfile: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val uploadProfilePictureUseCase: UploadProfilePictureUseCase,
    private val getUserOrders: GetUserOrdersUseCase,
    private val gson: Gson
) : ViewModel() {

    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut

    private val _userState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val userState: StateFlow<ProfileState> = _userState.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _profileEditUiState = MutableStateFlow(ProfileEditUiState())
    val profileEditUiState = _profileEditUiState.asStateFlow()

    private val _imageUploadState = MutableStateFlow<ImageUploadState>(ImageUploadState.Idle)
    val imageUploadState: StateFlow<ImageUploadState> = _imageUploadState.asStateFlow()

    private val _ordersState = MutableStateFlow<OrdersState>(OrdersState.Loading)
    val ordersState: StateFlow<OrdersState> = _ordersState.asStateFlow()


    init {
        loadUserProfile()
    }

    fun loadUserOrders(limit: Int? = null) {
        viewModelScope.launch {
            _ordersState.value = OrdersState.Loading
            when (val result = getUserOrders()) {
                is Resource.Success -> {
                    result.data?.let { response ->
                        val sortedOrders = response.data.sortedByDescending { it.date }
                        _ordersState.value = OrdersState.Success(
                            if (limit != null) sortedOrders.take(limit) else sortedOrders
                        )
                    } ?: run {
                        _ordersState.value = OrdersState.Error("No orders found")
                    }
                }
                is Resource.Error -> {
                    _ordersState.value = OrdersState.Error(result.message ?: "Failed to load orders")
                }
                else -> {}
            }
        }
    }

    fun loadUserProfile() {
        val token = tokenManager.getToken()
        if (token.isNullOrEmpty()) {
            _userState.value = ProfileState.Error("User not authenticated")
            return
        }

        viewModelScope.launch {
            _userState.value = ProfileState.Loading
            when (val result = getUserProfile(token)) {
                is Resource.Success -> {
                    result.data?.let { user ->
                        _userState.value = ProfileState.Success(user)
                        _profileEditUiState.update {
                            it.copy(
                                firstName = user.firstName,
                                lastName = user.lastName,
                                email = user.email,
                                phone = user.phone,
                                address = user.address ?: "",
                                firstNameError = null,
                                lastNameError = null,
                                emailError = null,
                                phoneError = null,
                                addressError = null
                            )
                        }
                    } ?: run {
                        _userState.value = ProfileState.Error("User data is empty")
                    }
                }
                is Resource.Error -> {
                    _userState.value = ProfileState.Error(result.message ?: "Unknown error")
                }
                else -> {}
            }
        }
    }

    fun updateFirstName(firstName: String) {
        val trimmed = firstName.trim()
        _profileEditUiState.update { currentState ->
            currentState.copy(
                firstName = trimmed,
                firstNameError = ProfileValidator.validateFirstName(trimmed)
            )
        }
    }

    fun updateLastName(lastName: String) {
        val trimmed = lastName.trim()
        _profileEditUiState.update { currentState ->
            currentState.copy(
                lastName = trimmed,
                lastNameError = ProfileValidator.validateLastName(trimmed)
            )
        }
    }

    fun updatePhone(phone: String) {
        _profileEditUiState.update { currentState ->
            currentState.copy(
                phone = phone,
                phoneError = ProfileValidator.validatePhone(phone)
            )
        }
    }

    fun updateAddress(address: String) {
        _profileEditUiState.update { currentState ->
            currentState.copy(
                address = address,
                addressError = null
            )
        }
    }

    fun updateUserProfile() {
        val currentState = _profileEditUiState.value
        val currentUser = (_userState.value as? ProfileState.Success)?.user

        if (currentUser == null) {
            _updateState.value = UpdateState.Error("User profile not loaded.")
            return
        }

        val errors = mutableMapOf<String, String>()
        ProfileValidator.validateFirstName(currentState.firstName)?.let { errors["firstName"] = it }
        ProfileValidator.validateLastName(currentState.lastName)?.let { errors["lastName"] = it }
        ProfileValidator.validatePhone(currentState.phone)?.let { errors["phone"] = it }

        _profileEditUiState.update {
            it.copy(
                firstNameError = errors["firstName"],
                lastNameError = errors["lastName"],
                emailError = null,
                phoneError = errors["phone"],
                addressError = errors["address"]
            )
        }

        if (errors.isNotEmpty()) {
            _updateState.value = UpdateState.ValidationError(errors)
            return
        }

        _updateState.value = UpdateState.Loading

        viewModelScope.launch {
            try {
                val userToUpdate = User(
                    id = currentUser.id,
                    firstName = currentState.firstName,
                    lastName = currentState.lastName,
                    email = currentUser.email,
                    phone = currentState.phone,
                    address = currentState.address.takeIf { it.isNotBlank() },
                    profilePicture = currentUser.profilePicture
                )

                when (val result = updateUserProfileUseCase(userToUpdate)) {
                    is Resource.Success -> {
                        _updateState.value = UpdateState.Success
                        loadUserProfile()
                    }
                    is Resource.Error -> {
                        handleUpdateError(result.message)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }

    fun uploadProfileImage(uri: Uri, context: Context) {
        _imageUploadState.value = ImageUploadState.Loading
        viewModelScope.launch {
            try {
                val contentResolver = context.contentResolver
                val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                Log.d("UploadDebug", "Image bytes size = ${bytes?.size}")

                if (bytes == null) {
                    _imageUploadState.value = ImageUploadState.Error("Failed to read image data from URI.")
                    return@launch
                }

                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                Log.d("UploadDebug", "URI: $uri")
                Log.d("UploadDebug", "Bytes size: ${bytes?.size}")
                Log.d("UploadDebug", "MimeType: $mimeType")
                val multipartBodyPart = MultipartBody.Part.createFormData(
                    "profilePicture",
                    "profile_picture.jpg",
                    requestBody
                )

                when (val result = uploadProfilePictureUseCase(multipartBodyPart)) {
                    is Resource.Success -> {
                        result.data?.let { updatedUser ->
                            _userState.value = ProfileState.Success(updatedUser)
                            _imageUploadState.value = ImageUploadState.Success("Profile picture uploaded successfully!")
                        } ?: run {
                            _imageUploadState.value = ImageUploadState.Error("Image upload success but no user data returned.")
                        }
                    }
                    is Resource.Error -> {
                        _imageUploadState.value = ImageUploadState.Error(result.message ?: "Failed to upload image.")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _imageUploadState.value = ImageUploadState.Error("Error uploading image: ${e.message}")
            }
        }
    }


    private fun handleUpdateError(errorMessage: String?) {
        clearEditStateAndErrors()

        if (errorMessage.isNullOrEmpty()) {
            _updateState.value = UpdateState.Error("An unknown error occurred.")
            return
        }

        try {
            val structuredErrorResponse = gson.fromJson(errorMessage, ErrorResponse::class.java)

            var hasSpecificFieldError = false
            structuredErrorResponse?.errors?.forEach { error ->
                val field = error.field
                val message = error.message
                if (!field.isNullOrEmpty() && !message.isNullOrEmpty()) {
                    hasSpecificFieldError = true
                    _profileEditUiState.update { current ->
                        when (field) {
                            "firstName" -> current.copy(firstNameError = message)
                            "lastName" -> current.copy(lastNameError = message)
                            "phoneNumber" -> current.copy(phoneError = message)
                            "address" -> current.copy(addressError = message)
                            else -> current
                        }
                    }
                }
            }

            if (hasSpecificFieldError) {
                _updateState.value = UpdateState.ValidationError(
                    _profileEditUiState.value.let {
                        mapOfNotNull(
                            "firstName" to it.firstNameError,
                            "lastName" to it.lastNameError,
                            "phone" to it.phoneError,
                            "address" to it.addressError
                        )
                    }
                )
            } else {
                val generalMessage = structuredErrorResponse?.errors?.firstOrNull()?.message
                    ?: "An unknown validation error occurred."
                _updateState.value = UpdateState.ValidationError(mapOf("general" to generalMessage))
            }

        } catch (e: JsonSyntaxException) {
            try {
                val genericErrorResponse = gson.fromJson(errorMessage, GenericBackendError::class.java)
                val generalErrorMessage = genericErrorResponse?.error

                if (generalErrorMessage != null) {
                    if (generalErrorMessage.contains("E11000 duplicate key error") && generalErrorMessage.contains("email")) {
                        _updateState.value = UpdateState.Error("Cannot update profile due to an existing email conflict.")
                    } else {
                        val messageToDisplay = if (generalErrorMessage == "Update failed.") {
                            "Profile update failed due to an unknown reason."
                        } else {
                            generalErrorMessage
                        }
                        _updateState.value = UpdateState.Error(messageToDisplay)
                    }
                } else {
                    val fallbackMessage = "An unknown error occurred: Response format unexpected (no 'error' field)."
                    _updateState.value = UpdateState.Error(fallbackMessage)
                }
            } catch (e2: JsonSyntaxException) {
                val fallbackMessage = "An unhandled error occurred: ${errorMessage}"
                _updateState.value = UpdateState.Error(fallbackMessage)
            } catch (e2: Exception) {
                val fallbackMessage = "An unexpected error occurred during generic error parsing: ${e2.message}"
                _updateState.value = UpdateState.Error(fallbackMessage)
            }
        } catch (e: Exception) {
            val fallbackMessage = "An unexpected error occurred during structured error parsing: ${e.message}"
            _updateState.value = UpdateState.Error(fallbackMessage)
        }
    }

    private fun mapOfNotNull(vararg pairs: Pair<String, String?>): Map<String, String> =
        pairs.filter { it.second != null }.associate { it.first to it.second!! }


    fun clearUpdateState() {
        _updateState.value = UpdateState.Idle
    }

    fun clearEditStateAndErrors() {
        _profileEditUiState.update {
            it.copy(
                firstNameError = null,
                lastNameError = null,
                emailError = null,
                phoneError = null,
                addressError = null
            )
        }
        _updateState.value = UpdateState.Idle
    }

    fun clearImageUploadState() {
        _imageUploadState.value = ImageUploadState.Idle
    }

    fun signOut() {
        tokenManager.clearToken()
        _isLoggedOut.value = true
    }
}

data class ProfileEditUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val addressError: String? = null
)

sealed interface ProfileState {
    data object Loading : ProfileState
    data class Success(val user: User) : ProfileState
    data class Error(val message: String) : ProfileState
}

sealed interface OrdersState {
    data object Loading : OrdersState
    data class Success(val orders: List<Order>) : OrdersState
    data class Error(val message: String) : OrdersState
}

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Loading : UpdateState()
    data object Success : UpdateState()
    data class ValidationError(val errors: Map<String, String>) : UpdateState()
    data class Error(val message: String) : UpdateState()
}

sealed class ImageUploadState {
    data object Idle : ImageUploadState()
    data object Loading : ImageUploadState()
    data class Success(val message: String) : ImageUploadState()
    data class Error(val message: String) : ImageUploadState()
}