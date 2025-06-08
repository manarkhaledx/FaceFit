package com.example.facefit.ui.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.data.local.TokenManager
import com.example.facefit.data.models.responses.ErrorResponse
import com.example.facefit.data.models.responses.FieldError
import com.example.facefit.data.models.responses.GenericBackendError
import com.example.facefit.domain.models.User
import com.example.facefit.domain.usecases.auth.GetUserProfileUseCase
import com.example.facefit.domain.usecases.auth.UpdateUserProfileUseCase
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
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val getUserProfile: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val gson: Gson
) : ViewModel() {

    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut

    private val _userState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val userState: StateFlow<ProfileState> = _userState.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    // Combined UI state for profile editing, including validation errors
    private val _profileEditUiState = MutableStateFlow(ProfileEditUiState())
    val profileEditUiState = _profileEditUiState.asStateFlow()

    init {
        // Load user profile immediately when ViewModel is created
        loadUserProfile()
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
                        // Also update the editable state when user is loaded
                        // Ensure to clear existing errors when a new user is loaded or on successful update
                        _profileEditUiState.update {
                            it.copy(
                                firstName = user.firstName,
                                lastName = user.lastName,
                                email = user.email,
                                phone = user.phone,
                                address = user.address ?: "",
                                firstNameError = null, // Clear errors on successful load
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

    fun updateEmail(email: String) {
        val trimmed = email.trim()
        _profileEditUiState.update { currentState ->
            currentState.copy(
                email = trimmed,
                emailError = ProfileValidator.validateEmail(trimmed)
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
                addressError = null // Address may not have local validation but still clear any old error
            )
        }
    }

    fun updateUserProfile() {
        val currentState = _profileEditUiState.value
        val currentUser = (_userState.value as? ProfileState.Success)?.user

        // If user state is not loaded, or in error, prevent update
        if (currentUser == null) {
            _updateState.value = UpdateState.Error("User profile not loaded.")
            // Also update UI state for immediate feedback by setting emailError
            _profileEditUiState.update { it.copy(emailError = "User profile not loaded. Please try again.") }
            return
        }

        // Perform local validation using ProfileValidator
        val errors = mutableMapOf<String, String>()
        ProfileValidator.validateFirstName(currentState.firstName)?.let { errors["firstName"] = it }
        ProfileValidator.validateLastName(currentState.lastName)?.let { errors["lastName"] = it }
        // Only validate email locally if it's changed from the original
        if (currentState.email != currentUser.email) {
            ProfileValidator.validateEmail(currentState.email)?.let { errors["email"] = it }
        }
        ProfileValidator.validatePhone(currentState.phone)?.let { errors["phone"] = it }

        // Update the UI state with local validation errors before proceeding
        // Ensure all error fields are explicitly cleared if no new error for them
        _profileEditUiState.update {
            it.copy(
                firstNameError = errors["firstName"],
                lastNameError = errors["lastName"],
                emailError = errors["email"],
                phoneError = errors["phone"],
                addressError = errors["address"] // Make sure address error is also handled
            )
        }

        if (errors.isNotEmpty()) {
            _updateState.value = UpdateState.ValidationError(errors)
            return
        }

        _updateState.value = UpdateState.Loading

        viewModelScope.launch {
            try {
                // Create a User object from the current editable state.
                // Keep existing ID and profilePicture as they are not being updated here.
                val userToUpdate = User(
                    id = currentUser.id,
                    firstName = currentState.firstName,
                    lastName = currentState.lastName,
                    email = currentState.email,
                    phone = currentState.phone,
                    address = currentState.address.takeIf { it.isNotBlank() },
                    profilePicture = currentUser.profilePicture
                )

                when (val result = updateUserProfileUseCase(userToUpdate)) {
                    is Resource.Success -> {
                        _updateState.value = UpdateState.Success
                        // Reload user profile to ensure UI reflects latest data, including potential server-side changes
                        // This will also clear all field errors as ProfileEditUiState is reset on load.
                        loadUserProfile()
                    }
                    is Resource.Error -> {
                        handleUpdateError(result.message) // Pass the raw error string
                    }
                    else -> {} // Should not happen for this use case
                }
            } catch (e: Exception) {
                // Set emailError for unexpected exceptions during update
                _profileEditUiState.update { it.copy(emailError = "An unexpected error occurred: ${e.message}") }
                _updateState.value = UpdateState.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }

    private fun handleUpdateError(errorMessage: String?) {
        // Clear previous field errors before attempting to parse new ones
        clearEditStateAndErrors()

        if (errorMessage.isNullOrEmpty()) {
            _profileEditUiState.update { it.copy(emailError = "An unknown error occurred.") }
            _updateState.value = UpdateState.Error("An unknown error occurred.")
            return
        }

        try {
            // Attempt 1: Try to parse as the structured ErrorResponse (for field-specific errors)
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
                            "email" -> current.copy(emailError = message)
                            "phoneNumber" -> current.copy(phoneError = message) // Backend uses "phoneNumber" path
                            "address" -> current.copy(addressError = message)
                            else -> current // Keep current state if field path is not recognized
                        }
                    }
                }
            }

            if (hasSpecificFieldError) {
                // If specific field errors were found, signal ValidationError
                _updateState.value = UpdateState.ValidationError(
                    _profileEditUiState.value.let {
                        mapOfNotNull(
                            "firstName" to it.firstNameError,
                            "lastName" to it.lastNameError,
                            "email" to it.emailError,
                            "phone" to it.phoneError,
                            "address" to it.addressError
                        )
                    }
                )
            } else {
                // If no structured field errors were identified to a specific field,
                // assume it's a general message and display it on the email field.
                val generalMessage = structuredErrorResponse?.errors?.firstOrNull()?.message
                    ?: "The email already exists." // Changed default fallback to "The email already exists."
                _profileEditUiState.update { it.copy(emailError = generalMessage) } // Set on email field
                _updateState.value = UpdateState.ValidationError(mapOf("email" to generalMessage)) // Treat as validation error for UI feedback
            }

        } catch (e: JsonSyntaxException) {
            // Attempt 2: If parsing as ErrorResponse failed (due to different JSON structure),
            // try to parse as GenericBackendError
            try {
                val genericErrorResponse = gson.fromJson(errorMessage, GenericBackendError::class.java)
                val generalErrorMessage = genericErrorResponse?.error

                if (generalErrorMessage != null) {
                    if (generalErrorMessage.contains("E11000 duplicate key error") && generalErrorMessage.contains("email")) {
                        _profileEditUiState.update { it.copy(emailError = "The email already exists.") }
                        _updateState.value = UpdateState.ValidationError(mapOf("email" to "The email already exists."))
                    } else {
                        // It's a generic error, not a specific field error, set it on the email field
                        // If the generic error message is simply "Update failed.", it will be caught here.
                        val messageToDisplay = if (generalErrorMessage == "Update failed.") {
                            "The email already exists." // Change "Update failed." to "The email already exists."
                        } else {
                            generalErrorMessage
                        }
                        _profileEditUiState.update { it.copy(emailError = messageToDisplay) } // Set on email field
                        _updateState.value = UpdateState.ValidationError(mapOf("email" to messageToDisplay)) // Treat as validation error for UI feedback
                    }
                } else {
                    val fallbackMessage = "An unknown error occurred: Response format unexpected (no 'error' field)."
                    _profileEditUiState.update { it.copy(emailError = fallbackMessage) } // Set on email field
                    _updateState.value = UpdateState.ValidationError(mapOf("email" to fallbackMessage))
                }
            } catch (e2: JsonSyntaxException) {
                // If neither parsing worked, it's a completely unparseable error string
                val fallbackMessage = "An unhandled error occurred: ${errorMessage}"
                _profileEditUiState.update { it.copy(emailError = fallbackMessage) } // Set on email field
                _updateState.value = UpdateState.ValidationError(mapOf("email" to fallbackMessage))
            } catch (e2: Exception) {
                val fallbackMessage = "An unexpected error occurred during generic error parsing: ${e2.message}"
                _profileEditUiState.update { it.copy(emailError = fallbackMessage) } // Set on email field
                _updateState.value = UpdateState.ValidationError(mapOf("email" to fallbackMessage))
            }
        } catch (e: Exception) {
            val fallbackMessage = "An unexpected error occurred during structured error parsing: ${e.message}"
            _profileEditUiState.update { it.copy(emailError = fallbackMessage) } // Set on email field
            _updateState.value = UpdateState.ValidationError(mapOf("email" to fallbackMessage))
        }
    }

    // Helper function to create map from nullable values
    private fun mapOfNotNull(vararg pairs: Pair<String, String?>): Map<String, String> =
        pairs.filter { it.second != null }.associate { it.first to it.second!! }


    fun clearUpdateState() {
        _updateState.value = UpdateState.Idle
    }

    // Clears all field errors
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

    fun signOut() {
        tokenManager.clearToken()
        _isLoggedOut.value = true
    }
}

// Data class to hold the mutable state for the profile editing UI
data class ProfileEditUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null, // All general errors will now be routed here if no other specific field error applies
    val phoneError: String? = null,
    val addressError: String? = null
)

// Sealed interface for the overall profile state (loading, success, error in fetching profile)
sealed interface ProfileState {
    data object Loading : ProfileState
    data class Success(val user: User) : ProfileState
    data class Error(val message: String) : ProfileState
}

// Sealed class for the profile update operation state (idle, loading, success, validation error, general error)
sealed class UpdateState {
    data object Idle : UpdateState()
    data object Loading : UpdateState()
    data object Success : UpdateState()
    data class ValidationError(val errors: Map<String, String>) : UpdateState()
    data class Error(val message: String) : UpdateState()
}
