package com.example.facefit.ui.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.data.local.TokenManager
import com.example.facefit.domain.models.User
import com.example.facefit.domain.usecases.auth.GetUserProfileUseCase
import com.example.facefit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val getUserProfile: GetUserProfileUseCase
) : ViewModel() {
    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut

    private val _userState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val userState: StateFlow<ProfileState> = _userState.asStateFlow()

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
                    val user = result.data
                    if (user != null) {
                        _userState.value = ProfileState.Success(user)
                    } else {
                        _userState.value = ProfileState.Error("User data is empty")
                    }
                }

                is Resource.Error -> {
                    _userState.value = ProfileState.Error(result.message ?: "Unknown error")
                }

                is Resource.Loading -> {

                    _userState.value = ProfileState.Loading
                }
            }
        }

    }

    fun signOut() {
        tokenManager.clearToken()
        _isLoggedOut.value = true
    }

}

sealed interface ProfileState {
    object Loading : ProfileState
    data class Success(val user: User) : ProfileState
    data class Error(val message: String) : ProfileState
}