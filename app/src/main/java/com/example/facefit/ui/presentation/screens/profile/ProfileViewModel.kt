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
import com.example.facefit.domain.utils.NetworkUtils
import com.example.facefit.domain.utils.Resource
import com.example.facefit.domain.utils.validators.ProfileValidator
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val getUserProfile: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val uploadProfilePictureUseCase: UploadProfilePictureUseCase,
    private val getUserOrders: GetUserOrdersUseCase,
    private val gson: Gson,
    @ApplicationContext private val context: Context
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
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _ordersState.value = OrdersState.Error("Please check your internet connection.")
            return
        }

        viewModelScope.launch {
            _ordersState.value = OrdersState.Loading
            try {
                when (val result = getUserOrders()) {
                    is Resource.Success -> {
                        result.data?.let { response ->
                            val sortedOrders = response.data.sortedByDescending { it.date }
                            _ordersState.value = OrdersState.Success(
                                if (limit != null) sortedOrders.take(limit) else sortedOrders
                            )
                        } ?: run {
                            Log.e("ProfileViewModel", "loadUserOrders: No orders data found.")
                            _ordersState.value = OrdersState.Error("Something went wrong.")
                        }
                    }
                    is Resource.Error -> {
                        handleGenericError(result.message, OrdersState.Error(""), _ordersState)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                handleGenericError(e.message, OrdersState.Error(""), _ordersState)
            }
        }
    }

    fun loadUserProfile() {
        val token = tokenManager.getToken()
        if (token.isNullOrEmpty()) {
            _userState.value = ProfileState.Error("User not authenticated")
            Log.e("ProfileViewModel", "User not authenticated for profile load.")
            return
        }

        if (!NetworkUtils.isNetworkAvailable(context)) {
            _userState.value = ProfileState.Error("Please check your internet connection.")
            return
        }

        viewModelScope.launch {
            _userState.value = ProfileState.Loading
            try {
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
                            Log.e("ProfileViewModel", "loadUserProfile: User data is empty for successful response.")
                            _userState.value = ProfileState.Error("Something went wrong.")
                        }
                    }
                    is Resource.Error -> {
                        handleGenericError(result.message, ProfileState.Error(""), _userState)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                handleGenericError(e.message, ProfileState.Error(""), _userState)
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
            _updateState.value = UpdateState.Error("User profile not loaded. Please try again.")
            Log.e("ProfileViewModel", "updateUserProfile: Current user is null.")
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

        if (!NetworkUtils.isNetworkAvailable(context)) {
            _updateState.value = UpdateState.Error("Please check your internet connection.")
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
                        handleUpdateError(result.message) // This handles specific backend errors
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                handleGenericError(e.message, UpdateState.Error(""), _updateState) // Generic exception handling
            }
        }
    }

    fun uploadProfileImage(uri: Uri, context: Context) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _imageUploadState.value = ImageUploadState.Error("Please check your internet connection.")
            return
        }

        _imageUploadState.value = ImageUploadState.Loading
        viewModelScope.launch {
            try {
                val contentResolver = context.contentResolver
                val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                Log.d("UploadDebug", "Image bytes size = ${bytes?.size}")

                if (bytes == null) {
                    Log.e("ProfileViewModel", "uploadProfileImage: Failed to read image data from URI.")
                    _imageUploadState.value = ImageUploadState.Error("Failed to read image data.")
                    return@launch
                }

                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                Log.d("UploadDebug", "URI: $uri")
                Log.d("UploadDebug", "Bytes size: ${bytes.size}")
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
                            Log.e("ProfileViewModel", "uploadProfileImage: Success, but no user data returned.")
                            _imageUploadState.value = ImageUploadState.Error("Something went wrong.")
                        }
                    }
                    is Resource.Error -> {
                        handleGenericError(result.message, ImageUploadState.Error(""), _imageUploadState)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                handleGenericError(e.message, ImageUploadState.Error(""), _imageUploadState)
            }
        }
    }

    private fun handleUpdateError(errorMessage: String?) {
        clearEditStateAndErrors()

        if (errorMessage.isNullOrEmpty()) {
            Log.e("ProfileViewModel", "handleUpdateError: Empty error message received.")
            _updateState.value = UpdateState.Error("Something went wrong.")
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
                    ?: "Something went wrong" // Default user-friendly message
                Log.e("ProfileViewModel", "Backend general error: $generalMessage (original: $errorMessage)")
                _updateState.value = UpdateState.Error(generalMessage)
            }

        } catch (e: JsonSyntaxException) {
            try {
                val genericErrorResponse = gson.fromJson(errorMessage, GenericBackendError::class.java)
                val generalErrorMessage = genericErrorResponse?.error

                if (generalErrorMessage != null) {
                    val messageToLog = if (generalErrorMessage.contains("E11000 duplicate key error") && generalErrorMessage.contains("email")) {
                        "Duplicate email detected on update."
                    } else {
                        generalErrorMessage
                    }
                    Log.e("ProfileViewModel", "Backend generic error: $messageToLog (original: $errorMessage)")

                    // User-friendly messages
                    val messageToDisplay = when {
                        generalErrorMessage.contains("E11000 duplicate key error") && generalErrorMessage.contains("email") -> "Cannot update profile due to an existing email conflict."
                        generalErrorMessage == "Update failed." -> "Profile update failed due to an unknown reason."
                        else -> "Something went wrong" // Catch-all for unspecific backend messages
                    }
                    _updateState.value = UpdateState.Error(messageToDisplay)
                } else {
                    Log.e("ProfileViewModel", "handleUpdateError: Backend response had no specific error message or a general error field: $errorMessage")
                    _updateState.value = UpdateState.Error("Something went wrong.")
                }
            } catch (e2: JsonSyntaxException) {
                Log.e("ProfileViewModel", "handleUpdateError: Secondary JSON parsing error: ${e2.message}. Original error: $errorMessage", e2)
                _updateState.value = UpdateState.Error("Something went wrong.")
            } catch (e2: Exception) {
                Log.e("ProfileViewModel", "handleUpdateError: Unexpected error during generic error parsing: ${e2.message}. Original error: $errorMessage", e2)
                _updateState.value = UpdateState.Error("Something went wrong.")
            }
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "handleUpdateError: Unexpected error during structured error parsing: ${e.message}. Original error: $errorMessage", e)
            _updateState.value = UpdateState.Error("Something went wrong.")
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

    // Generic error handler for ProfileViewModel
    private fun <T> handleGenericError(errorMessage: String?, initialState: T, stateFlow: MutableStateFlow<T>) {
        val userFriendlyMessage: String
        val logMessage: String = errorMessage ?: "Unknown error"

        when {
            errorMessage?.contains("internet connection", ignoreCase = true) == true ||
                    errorMessage?.contains("network error", ignoreCase = true) == true ||
                    errorMessage?.contains("timeout", ignoreCase = true) == true ||
                    errorMessage?.contains("Unable to resolve host", ignoreCase = true) == true -> {
                userFriendlyMessage = "Please check your internet connection."
            }
            else -> {
                userFriendlyMessage = "Something went wrong"
            }
        }

        Log.e("ProfileViewModel", "API Call Error: $logMessage")

        when (stateFlow.value) {
            is ProfileState -> (stateFlow as MutableStateFlow<ProfileState>).value = ProfileState.Error(userFriendlyMessage)
            is OrdersState -> (stateFlow as MutableStateFlow<OrdersState>).value = OrdersState.Error(userFriendlyMessage)
            is UpdateState -> (stateFlow as MutableStateFlow<UpdateState>).value = UpdateState.Error(userFriendlyMessage)
            is ImageUploadState -> (stateFlow as MutableStateFlow<ImageUploadState>).value = ImageUploadState.Error(userFriendlyMessage)
            else -> Log.e("ProfileViewModel", "Unknown state type for generic error handling.")
        }
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