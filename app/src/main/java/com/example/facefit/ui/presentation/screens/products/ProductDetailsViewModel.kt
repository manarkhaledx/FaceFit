package com.example.facefit.ui.presentation.screens.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.data.local.TokenManager
import com.example.facefit.data.models.User
import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.models.Review
import com.example.facefit.domain.usecases.auth.GetUserProfileUseCase
import com.example.facefit.domain.usecases.favorites.GetFavoritesUseCase
import com.example.facefit.domain.usecases.favorites.ToggleFavoriteUseCase
import com.example.facefit.domain.usecases.glasses.GetGlassesByIdUseCase
import com.example.facefit.domain.usecases.glasses.GetRecommendedGlassesUseCase
import com.example.facefit.domain.usecases.reviews.GetReviewsUseCase
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
    private val getRecommendedGlassesUseCase: GetRecommendedGlassesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val authManager: TokenManager,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val getReviewsUseCase: GetReviewsUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailsUiState())
    val uiState: StateFlow<ProductDetailsUiState> = _uiState.asStateFlow()

    private val _recommendations = MutableStateFlow<List<ProductItem>>(emptyList())
    val recommendations: StateFlow<List<ProductItem>> = _recommendations.asStateFlow()

    private val _favoriteStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val favoriteStatus: StateFlow<Map<String, Boolean>> = _favoriteStatus.asStateFlow()

    private val _pendingFavorites = MutableStateFlow<Set<String>>(emptySet())
    val pendingFavorites: StateFlow<Set<String>> = _pendingFavorites.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()


    fun loadProductDetails(productId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            loadFavorites()
            loadUserProfile()
            when (val result = getGlassesByIdUseCase(productId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            glasses = result.data,
                            isLoading = false,
                            error = null
                        )
                    }
                    result.data?.let {
                        loadRecommendations(it.id, it.gender, it.type, it.material)
                        loadReviews(it.id)
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
                is Resource.Loading -> Unit
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

                }
                is Resource.Loading -> TODO()
            }
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            val token = authManager.getToken() ?: return@launch
            when (val result = getFavoritesUseCase(token)) {
                is Resource.Success -> {
                    _favoriteStatus.update {
                        result.data?.associate { it.id to true } ?: emptyMap()
                    }
                }
                is Resource.Error -> TODO()
                is Resource.Loading -> TODO()
            }
        }
    }

    fun toggleFavorite(productId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = authManager.getToken() ?: return@launch
            val currentStatus = _favoriteStatus.value[productId] ?: false

            _favoriteStatus.update { it + (productId to !currentStatus) }
            _pendingFavorites.update { it + productId }

            when (val result = toggleFavoriteUseCase(token, productId)) {
                is Resource.Success -> loadFavorites() // Confirm with server
                is Resource.Error -> {
                    _favoriteStatus.update { it + (productId to currentStatus) }
                }

                is Resource.Loading -> TODO()
            }

            _pendingFavorites.update { it - productId }
        }
    }

    fun toggleRecommendedFavorite(productId: String) = toggleFavorite(productId)

    fun loadReviews(glassesId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = authManager.getToken()
            if (token == null) {
                _uiState.update {
                    it.copy(error = "Authentication required to view reviews")
                }
                return@launch
            }

            when (val result = getReviewsUseCase(token, glassesId)) {
                is Resource.Success -> {
                    val reviews = result.data ?: emptyList()

                    val averageRating = if (reviews.isNotEmpty()) {
                        reviews.mapNotNull { it.rating?.toDouble() }.average()
                    } else {
                        0.0
                    }

                    _uiState.update {
                        it.copy(
                            reviews = reviews,
                            averageRating = averageRating,
                            numberOfRatings = reviews.size,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(error = result.message ?: "Failed to load reviews")
                    }
                }
                is Resource.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            val token = authManager.getToken() ?: return@launch
            when (val result = getUserProfileUseCase(token)) {
                is Resource.Success -> {
                    _userProfile.value = result.data
                }
                is Resource.Error -> {
                    // Handle error
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun retryLoadingReviews(glassesId: String) {
        loadReviews(glassesId)
    }

}

data class ProductDetailsUiState(
    val glasses: Glasses? = null,
    val reviews: List<Review> = emptyList(),
    val averageRating: Double = 0.0,
    val numberOfRatings: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)