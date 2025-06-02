package com.example.facefit.ui.presentation.screens.auth.signUp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.data.models.requests.SignUpRequest
import com.example.facefit.data.models.responses.SignUpResponse
import com.example.facefit.domain.usecases.auth.SignUpUseCase
import com.example.facefit.domain.utils.Resource
import com.example.facefit.domain.utils.validators.SignUpValidator
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val gson: Gson
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState = _uiState.asStateFlow()

    private val _signUpState = MutableStateFlow<Resource<Unit>?>(null)
    val signUpState = _signUpState.asStateFlow()

    fun updateFirstName(firstName: String) {
        val trimmed = firstName.trim()
        _uiState.update { currentState ->
            currentState.copy(
                firstName = trimmed,
                firstNameError = SignUpValidator.validateFirstName(trimmed)
            )
        }
    }

    fun updateLastName(lastName: String) {
        val trimmed = lastName.trim()
        _uiState.update { currentState ->
            currentState.copy(
                lastName = trimmed,
                lastNameError = SignUpValidator.validateLastName(trimmed)
            )
        }
    }


    fun updatePhone(phone: String) {
        _uiState.update { currentState ->
            currentState.copy(
                phone = phone,
                phoneError = SignUpValidator.validatePhone(phone)
            )
        }
    }

    fun updateEmail(email: String) {
        val trimmed = email.trim()
        _uiState.update { currentState ->
            currentState.copy(
                email = trimmed,
                emailError = SignUpValidator.validateEmail(trimmed)
            )
        }
    }


    fun updatePassword(password: String) {
        _uiState.update { currentState ->
            currentState.copy(
                password = password,
                passwordError = SignUpValidator.validatePassword(password),
                confirmPasswordError = currentState.confirmPassword.takeIf { it.isNotBlank() }
                    ?.let { SignUpValidator.validateConfirmPassword(password, it) }
            )
        }
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.update { currentState ->
            currentState.copy(
                confirmPassword = confirmPassword,
                confirmPasswordError = SignUpValidator.validateConfirmPassword(
                    currentState.password,
                    confirmPassword
                )
            )
        }
    }

    fun signUp() {
        // Revalidate all fields
        val currentState = _uiState.value
        val errors = mapOf(
            "firstName" to SignUpValidator.validateFirstName(currentState.firstName),
            "lastName" to SignUpValidator.validateLastName(currentState.lastName),
            "phone" to SignUpValidator.validatePhone(currentState.phone),
            "email" to SignUpValidator.validateEmail(currentState.email),
            "password" to SignUpValidator.validatePassword(currentState.password),
            "confirmPassword" to SignUpValidator.validateConfirmPassword(
                currentState.password,
                currentState.confirmPassword
            )
        )

        _uiState.update {
            it.copy(
                firstNameError = errors["firstName"],
                lastNameError = errors["lastName"],
                phoneError = errors["phone"],
                emailError = errors["email"],
                passwordError = errors["password"],
                confirmPasswordError = errors["confirmPassword"]
            )
        }

        if (errors.any { it.value != null }) {
            _signUpState.value = Resource.Error("Please fix the form errors")
            return
        }

        _signUpState.value = Resource.Loading()

        viewModelScope.launch {
            try {
                val request = SignUpRequest(
                    firstName = currentState.firstName,
                    lastName = currentState.lastName,
                    phone = currentState.phone,
                    email = currentState.email,
                    password = currentState.password,
                    confirmPassword = currentState.confirmPassword
                )

                val response = signUpUseCase(request)
                handleSignUpResponse(response)
            } catch (e: Exception) {
                _signUpState.value = Resource.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    private fun handleSignUpResponse(response: Response<SignUpResponse>) {
        if (response.isSuccessful) {
            _signUpState.value = Resource.Success(Unit)
        } else {
            try {
                val errorBody = response.errorBody()?.string()
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)

                errorResponse?.errors?.forEach { error ->
                    when (error.field) {
                        "firstName" -> _uiState.update { it.copy(firstNameError = error.message) }
                        "lastName" -> _uiState.update { it.copy(lastNameError = error.message) }
                        "phone" -> _uiState.update { it.copy(phoneError = error.message) }
                        "email" -> _uiState.update { it.copy(emailError = error.message) }
                        "password" -> _uiState.update { it.copy(passwordError = error.message) }
                        "confirmPassword" -> _uiState.update {
                            it.copy(confirmPasswordError = error.message)
                        }
                    }
                }

                _signUpState.value = Resource.Error(
                    errorResponse?.message ?: "Sign up failed"
                )
            } catch (e: Exception) {
                _signUpState.value = Resource.Error("Failed to parse error response")
            }
        }
    }
}

data class SignUpUiState(
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val phoneError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

data class ErrorResponse(
    val message: String?,
    val errors: List<FieldError> = emptyList()
) {
    data class FieldError(
        val field: String,
        val message: String
    )
}