package com.example.facefit.ui.presentation.screens.auth.signUp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.data.local.TokenManager
import com.example.facefit.data.models.requests.LoginRequest
import com.example.facefit.data.models.requests.SignUpRequest
import com.example.facefit.data.models.responses.ErrorResponse
import com.example.facefit.data.models.responses.SignUpResponse
import com.example.facefit.domain.usecases.auth.LoginUseCase
import com.example.facefit.domain.usecases.auth.SignUpUseCase
import com.example.facefit.domain.utils.Resource
import com.example.facefit.domain.utils.validators.SignUpValidator
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val loginUseCase: LoginUseCase,
    private val tokenManager: TokenManager,
    private val gson: Gson

) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState = _uiState.asStateFlow()

    private val _signUpState = MutableStateFlow<Resource<Unit>?>(null)
    val signUpState = _signUpState.asStateFlow()
    private var tempEmail = ""
    private var tempPassword = ""
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
        tempEmail = currentState.email
        tempPassword = currentState.password

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
    private suspend fun handleLoginAfterSignUp() {
        val loginRequest = LoginRequest(email = tempEmail, password = tempPassword)
        val loginResponse = loginUseCase(loginRequest)

        if (loginResponse.isSuccessful) {
            loginResponse.body()?.data?.token?.let { token ->
                tokenManager.saveToken(token)
                _signUpState.value = Resource.Success(Unit)
            } ?: run {
                _signUpState.value = Resource.Error("Login failed after sign up")
            }
        } else {
            _signUpState.value = Resource.Error("Login failed after sign up")
        }
    }
    private fun handleSignUpResponse(response: Response<SignUpResponse>) {
        if (response.isSuccessful) {
            viewModelScope.launch {
                handleLoginAfterSignUp()
            }
        } else {
            try {
                val errorBody = response.errorBody()?.string()
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)

                var hasFieldError = false
                errorResponse?.errors?.forEach { error ->
                    val field = error.field
                    val message = error.message
                    if (!field.isNullOrEmpty() && !message.isNullOrEmpty()) {
                        hasFieldError = true
                        _uiState.update { current ->
                            when (field) {
                                "firstName" -> current.copy(firstNameError = message)
                                "lastName" -> current.copy(lastNameError = message)
                                "phone" -> current.copy(phoneError = message)
                                "email" -> current.copy(emailError = message)
                                "password" -> current.copy(passwordError = message)
                                "confirmPassword" -> current.copy(confirmPasswordError = message)
                                else -> current
                            }
                        }
                    }
                }

                if (hasFieldError) {
                    _signUpState.value = Resource.Error("")
                } else {
                    val fallback = errorResponse?.errors?.firstOrNull()?.message ?: "Sign up failed"
                    _signUpState.value = Resource.Error(fallback)
                }

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
