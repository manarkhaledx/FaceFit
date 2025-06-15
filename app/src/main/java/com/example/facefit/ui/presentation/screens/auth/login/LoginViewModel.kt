package com.example.facefit.ui.presentation.screens.auth.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.data.local.TokenManager
import com.example.facefit.data.models.requests.LoginRequest
import com.example.facefit.data.models.responses.ErrorResponse
import com.example.facefit.data.models.responses.LoginResponse
import com.example.facefit.domain.usecases.auth.LoginUseCase
import com.example.facefit.domain.utils.Resource
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import com.example.facefit.domain.utils.NetworkUtils // Import your NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val gson: Gson,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context // Injected ApplicationContext
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<Unit>?>(null)
    val loginState: StateFlow<Resource<Unit>?> = _loginState

    private val _fieldErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val fieldErrors: StateFlow<Map<String, String>> = _fieldErrors

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun login(email: String, password: String) {
        _loginState.value = null
        _errorMessage.value = null

        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)

        _fieldErrors.value = buildMap {
            emailError?.let { put("email", it) }
            passwordError?.let { put("password", it) }
        }

        if (emailError != null || passwordError != null) {
            _loginState.value = Resource.Error("Please fix the form errors")
            return
        }

        if (!NetworkUtils.isNetworkAvailable(context)) {
            _errorMessage.value = "Please check your internet connection."
            _loginState.value = Resource.Error("Please check your internet connection.")
            return
        }


        _loginState.value = Resource.Loading()

        viewModelScope.launch {
            try {
                val request = LoginRequest(
                    email = email,
                    password = password
                )

                val response = loginUseCase(request)
                handleLoginResponse(response)
            } catch (e: Exception) {
                handleGenericError(e)
            }
        }
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Please enter a valid email"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }

    private suspend fun handleLoginResponse(response: Response<LoginResponse>) {
        if (response.isSuccessful) {
            response.body()?.let { loginResponse ->
                tokenManager.saveToken(loginResponse.data.token)
                _loginState.value = Resource.Success(Unit)
            } ?: run {
                Log.e("LoginViewModel", "Invalid successful response: body is null")
                _errorMessage.value = "Something went wrong"
                _loginState.value = Resource.Error("Something went wrong")
            }
        } else {
            handleErrorResponse(response)
        }
    }

    private suspend fun handleErrorResponse(response: Response<LoginResponse>) {
        try {
            val errorBody = response.errorBody()?.string()
            val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)

            if (!errorResponse?.errors.isNullOrEmpty()) {
                val errorsMap = errorResponse?.errors
                    ?.mapNotNull { it.field?.let { field -> it.message?.let { msg -> field to msg } } }
                    ?.toMap() ?: emptyMap()
                _fieldErrors.value = errorsMap
                _loginState.value = Resource.Error("Please fix the form errors")
                return
            }

            val errorMessage = when {
                errorBody?.contains("Customer not found") == true -> "Email not registered"
                errorBody?.contains("Invalid password") == true -> "Invalid password"
                else -> {
                    Log.e("LoginViewModel", "Server error (code: ${response.code()}): $errorBody")
                    "Something went wrong"
                }
            }

            _errorMessage.value = errorMessage
            _loginState.value = Resource.Error(errorMessage)

        } catch (e: Exception) {
            Log.e("LoginViewModel", "Error handling error response: ${e.message}", e)
            _errorMessage.value = "Something went wrong"
            _loginState.value = Resource.Error("Something went wrong")
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

        Log.e("LoginViewModel", logMessage, e)
        _errorMessage.value = userFriendlyMessage
        _loginState.value = Resource.Error(userFriendlyMessage)
    }

    fun clearFieldError(field: String) {
        _fieldErrors.value -= field
    }

    fun clearLoginState() {
        _loginState.value = null
    }
}