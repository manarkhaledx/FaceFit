package com.example.facefit.ui.presentation.screens.auth.signUp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.data.models.SignUpRequest
import com.example.facefit.data.models.SignUpResponse
import com.example.facefit.domain.usecases.SignUpUseCase
import com.example.facefit.domain.utils.Resource
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import com.example.facefit.data.models.ErrorResponse

import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val gson: Gson
) : ViewModel() {

    private val _signUpState = MutableStateFlow<Resource<Unit>?>(null)
    val signUpState: StateFlow<Resource<Unit>?> = _signUpState

    private val _fieldErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val fieldErrors: StateFlow<Map<String, String>> = _fieldErrors

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun signUp(
        firstName: String,
        lastName: String,
        phone: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        _signUpState.value = Resource.Loading()
        _fieldErrors.value = emptyMap()
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val request = SignUpRequest(
                    firstName = firstName,
                    lastName = lastName,
                    phone = phone,
                    email = email,
                    password = password,
                    confirmPassword = confirmPassword
                )

                val response = signUpUseCase(request)
                if (response.isSuccessful) {
                    Log.d("SignUp", "Response: ${response.body()}")
                } else {
                    Log.e("SignUp", "Error: ${response.errorBody()?.string()}")
                }
                if (response.isSuccessful) {
                    _signUpState.value = Resource.Success(Unit)
                } else {
                    handleErrorResponse(response)
                }
            } catch (e: Exception) {
                handleGenericError(e)
            }
        }
    }

    private suspend fun handleErrorResponse(response: Response<SignUpResponse>) {
        try {
            val errorBody = response.errorBody()?.string()
            val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)

            if (!errorResponse?.errors.isNullOrEmpty()) {
                val errorsMap = errorResponse?.errors?.associate { it.field to it.message } ?: emptyMap()
                _fieldErrors.value = errorsMap
                _signUpState.value = Resource.Error("Please fix the form errors")
                return
            }

            val errorMessage = when {
                errorBody?.contains("Email already in use") == true -> "Email already in use"
                else -> "Sign up failed"
            }

            _errorMessage.value = errorMessage
            _signUpState.value = Resource.Error(errorMessage)

        } catch (e: Exception) {
            handleGenericError(e)
        }
    }

    private fun handleGenericError(e: Exception) {
        val message = e.message ?: "Sign up failed"
        _errorMessage.value = message
        _signUpState.value = Resource.Error(message)
    }

    fun clearFieldError(field: String) {
        _fieldErrors.value = _fieldErrors.value - field
    }
}