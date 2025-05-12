package com.example.facefit.ui.presentation.screens.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.usecases.GetGlassesByIdUseCase
import com.example.facefit.domain.usecases.GetRecommendedGlassesUseCase
import com.example.facefit.domain.utils.Resource
import com.example.facefit.ui.presentation.components.ProductItem
import com.example.facefit.ui.presentation.components.toProductItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val getGlassesByIdUseCase: GetGlassesByIdUseCase,
    private val getRecommendedGlassesUseCase: GetRecommendedGlassesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailsUiState())
    val uiState: StateFlow<ProductDetailsUiState> = _uiState.asStateFlow()

    private val _recommendations = MutableStateFlow<List<ProductItem>>(emptyList())
    val recommendations: StateFlow<List<ProductItem>> = _recommendations.asStateFlow()

    fun loadProductDetails(productId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = getGlassesByIdUseCase(productId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            glasses = result.data,
                            isLoading = false,
                            error = null
                        )
                    }
                    // Load recommendations after product is loaded
                    result.data?.let { glasses ->
                        loadRecommendations(productId, glasses.gender, glasses.type, glasses.material)
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

    private fun loadRecommendations(productId: String, gender: String, type: String, material: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = getRecommendedGlassesUseCase(productId, gender, type, material)) {
                is Resource.Success -> {
                    _recommendations.value = result.data?.map { it.toProductItem() } ?: emptyList()
                }
                is Resource.Error -> {
                    // Handle error if needed
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