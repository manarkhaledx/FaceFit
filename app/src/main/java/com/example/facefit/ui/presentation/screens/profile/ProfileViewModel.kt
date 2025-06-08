package com.example.facefit.ui.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.data.local.TokenManager
import com.example.facefit.domain.models.User
import com.example.facefit.domain.repository.UserRepository
import com.example.facefit.domain.usecases.auth.GetUserProfileUseCase
import com.example.facefit.domain.usecases.auth.UpdateUserProfileUseCase
import com.example.facefit.domain.utils.Resource
import com.example.facefit.domain.utils.validators.ProfileValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val getUserProfile: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    val checkEmailExistsUseCase: CheckEmailExistsUseCase
) : ViewModel() {
    private val _validationErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val validationErrors: StateFlow<Map<String, String>> = _validationErrors

    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut

    private val _userState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val userState: StateFlow<ProfileState> = _userState.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

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

    fun updateUserProfile(updatedUser: User) {
        viewModelScope.launch {
            val validationErrors = ProfileValidator.validateUser(updatedUser).toMutableMap()

            val currentEmail = (userState.value as? ProfileState.Success)?.user?.email
            val isEmailChanged = currentEmail != null && currentEmail != updatedUser.email

            if (isEmailChanged) {
                val result = checkEmailExistsUseCase(updatedUser.email)
                if (result is Resource.Success && result.data == true) {
                    validationErrors["email"] = "This email is already in use"
                }
            }

            if (validationErrors.isNotEmpty()) {
                _validationErrors.value = validationErrors
                return@launch
            }

            _updateState.value = UpdateState.Loading
            when (val result = updateUserProfileUseCase(updatedUser)) {
                is Resource.Success -> {
                    _validationErrors.value = emptyMap()
                    loadUserProfile()
                    _updateState.value = UpdateState.Success
                }

                is Resource.Error -> {
                    _updateState.value = UpdateState.Error(result.message ?: "Update failed")
                }

                else -> {}
            }
        }
    }
    fun setValidationErrors(errors: Map<String, String>) {
        _validationErrors.value = errors
        _updateState.value = UpdateState.ValidationError(errors)
    }




    fun clearUpdateState() {
        _updateState.value = UpdateState.Idle
    }

    fun signOut() {
        tokenManager.clearToken()
        _isLoggedOut.value = true
    }
}

sealed interface ProfileState {
    data object Loading : ProfileState
    data class Success(val user: User) : ProfileState
    data class Error(val message: String) : ProfileState
}

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Loading : UpdateState()
    data object Success : UpdateState()
    data class ValidationError(val errors: Map<String, String>) : UpdateState()
    data class Error(val message: String) : UpdateState()
}
class CheckEmailExistsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String): Resource<Boolean> {
        return userRepository.checkEmailExists(email)
    }
}

