package com.example.facefit.ui.presentation.screens.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.repository.GlassesRepository
import com.example.facefit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val repository: GlassesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailsUiState())
    val uiState: StateFlow<ProductDetailsUiState> = _uiState.asStateFlow()

    fun loadProductDetails(productId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = repository.getGlassesById(productId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            glasses = result.data,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            error = result.message,
                            isLoading = false,
                            glasses = result.data
                        )
                    }
                }

                is Resource.Loading -> TODO()
            }
        }
    }

    fun toggleFavorite() {
        _uiState.update { state ->
            state.glasses?.let { glasses ->
                state.copy(glasses = glasses.copy(isFavorite = !glasses.isFavorite))
            } ?: state
        }
    }
}

data class ProductDetailsUiState(
    val glasses: Glasses? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)