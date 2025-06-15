package com.example.facefit.ui.presentation.screens.favourites

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.data.local.TokenManager
import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.usecases.favorites.GetFavoritesUseCase
import com.example.facefit.domain.usecases.favorites.ToggleFavoriteUseCase
import com.example.facefit.domain.utils.NetworkUtils
import com.example.facefit.domain.utils.Resource
import com.example.facefit.ui.presentation.base.RefreshableViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val authManager: TokenManager,
    @ApplicationContext private val context: Context
) : ViewModel(), RefreshableViewModel {
    private val _favoritesState = MutableStateFlow<Resource<List<Glasses>>>(Resource.Loading())
    val favoritesState: StateFlow<Resource<List<Glasses>>> = _favoritesState

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _favoritesState.value = Resource.Error("Please check your internet connection.", null)
            Log.e("FavoritesVM", "No network available for loadFavorites.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _favoritesState.value = Resource.Loading()
            try {
                val token = authManager.getToken()
                if (token != null) {
                    val result = getFavoritesUseCase(token)
                    _favoritesState.value = result
                    if (result is Resource.Error) {
                        Log.e("FavoritesVM", "Error loading favorites: ${result.message}")
                    }
                } else {
                    _favoritesState.value = Resource.Error("User not authenticated", null)
                    Log.e("FavoritesVM", "User not authenticated when trying to load favorites.")
                }
            } catch (e: Exception) {
                handleGenericError(e, _favoritesState)
            }
        }
    }

    fun toggleFavorite(glassesId: String) {
        val token = authManager.getToken()
        if (token == null) {
            _favoritesState.value = Resource.Error("Authentication required.", null)
            Log.e("FavoritesVM", "Authentication token missing for favorite toggle.")
            return
        }
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _favoritesState.value = Resource.Error("Please check your internet connection.", null)
            Log.e("FavoritesVM", "No network to toggle favorite.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                when (val result = toggleFavoriteUseCase(token, glassesId)) {
                    is Resource.Success -> {
                        loadFavorites()
                    }
                    is Resource.Error -> {
                        Log.e("FavoritesVM", "Failed to toggle favorite for $glassesId: ${result.message}")
                        _favoritesState.value = Resource.Error(result.message ?: "Something went wrong.", null)
                    }
                    is Resource.Loading -> {
                    }
                }
            } catch (e: Exception) {
                handleGenericError(e, _favoritesState)
            }
        }
    }

    override fun refresh() {
        loadFavorites()
    }

    private fun <T> handleGenericError(e: Exception, stateFlow: MutableStateFlow<Resource<T>>) {
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
        Log.e("FavoritesVM", logMessage, e)
        stateFlow.value = Resource.Error(userFriendlyMessage, null)
    }
}