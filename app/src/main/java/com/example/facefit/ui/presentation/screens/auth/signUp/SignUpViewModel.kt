package com.example.facefit.ui.presentation.screens.auth.signUp

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.data.local.TokenManager
import com.example.facefit.data.models.requests.LoginRequest
import com.example.facefit.data.models.requests.SignUpRequest
import com.example.facefit.data.models.responses.ErrorResponse
import com.example.facefit.data.models.responses.SignUpResponse
import com.example.facefit.domain.usecases.auth.LoginUseCase
import com.example.facefit.domain.usecases.auth.SignUpUseCase
import com.example.facefit.domain.utils.NetworkUtils
import com.example.facefit.domain.utils.Resource
import com.example.facefit.domain.utils.validators.SignUpValidator
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val loginUseCase: LoginUseCase,
    private val tokenManager: TokenManager,
    private val gson: Gson,
    @ApplicationContext private val context: Context
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

        if (!NetworkUtils.isNetworkAvailable(context)) {
            _signUpState.value = Resource.Error("Please check your internet connection.")
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
                handleGenericError(e)
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
                Log.e("SignUpViewModel", "Login after sign up: body is null for successful response")
                _signUpState.value = Resource.Error("Something went wrong")
            }
        } else {
            Log.e("SignUpViewModel", "Login after sign up failed. Code: ${loginResponse.code()}, Error: ${loginResponse.errorBody()?.string()}")
            _signUpState.value = Resource.Error("Something went wrong")
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
                    _signUpState.value = Resource.Error("Please fix the form errors")
                } else {
                    // For unhandled server errors or general messages not tied to a specific field
                    val fallbackMessage = errorResponse?.errors?.firstOrNull()?.message ?: "Something went wrong"
                    Log.e("SignUpViewModel", "Server error (code: ${response.code()}): $errorBody")
                    _signUpState.value = Resource.Error(fallbackMessage)
                }

            } catch (e: Exception) {
                Log.e("SignUpViewModel", "Error parsing sign up error response: ${e.message}", e)
                _signUpState.value = Resource.Error("Something went wrong")
            }
        }
    }

    private fun handleGenericError(e: Exception) {
        val userFriendlyMessage: String
        val logMessage: String

        when (e) {
            is SocketTimeoutException -> {
                userFriendlyMessage = "Please check your internet connection."
                logMessage = "Timeout error: ${e.message}"
            }
            is IOException -> {
                userFriendlyMessage = "Please check your internet connection."
                logMessage = "Network error: ${e.message}"
            }
            is HttpException -> {
                userFriendlyMessage = "Something went wrong"
                logMessage = "HTTP error: ${e.code()} - ${e.message()}"
            }
            else -> {
                userFriendlyMessage = "Something went wrong"
                logMessage = "An unexpected error occurred: ${e.message}"
            }
        }

        Log.e("SignUpViewModel", logMessage, e)
        _signUpState.value = Resource.Error(userFriendlyMessage)
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