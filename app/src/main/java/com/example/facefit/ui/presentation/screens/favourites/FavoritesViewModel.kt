package com.example.facefit.ui.presentation.screens.favourites

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.data.local.TokenManager
import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.usecases.favorites.GetFavoritesUseCase
import com.example.facefit.domain.usecases.favorites.ToggleFavoriteUseCase
import com.example.facefit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val authManager: TokenManager,
) : ViewModel() {
    private val _favoritesState = MutableStateFlow<Resource<List<Glasses>>>(Resource.Loading())
    val favoritesState: StateFlow<Resource<List<Glasses>>> = _favoritesState

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            _favoritesState.value = Resource.Loading()
            try {
                val token = authManager.getToken()
                if (token != null) {
                    _favoritesState.value = getFavoritesUseCase(token)
                } else {
                    _favoritesState.value = Resource.Error("User not authenticated")
                }
            } catch (e: Exception) {
                Log.e("FavoritesVM", "Error loading favorites", e)
                _favoritesState.value = Resource.Error(e.message ?: "Error loading favorites")
            }
        }
    }

    fun toggleFavorite(glassesId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = authManager.getToken() ?: return@launch
            when (val result = toggleFavoriteUseCase(token, glassesId)) {
                is Resource.Success -> loadFavorites()
                is Resource.Error -> {
                    _favoritesState.value = Resource.Error(result.message ?: "Failed to toggle favorite")
                }
                else -> {}
            }
        }
    }
}