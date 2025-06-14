package com.example.facefit.ui.presentation.screens.products

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.data.local.TokenManager
import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.models.Review
import com.example.facefit.domain.models.User
import com.example.facefit.domain.usecases.auth.GetUserProfileUseCase
import com.example.facefit.domain.usecases.favorites.GetFavoritesUseCase
import com.example.facefit.domain.usecases.favorites.ToggleFavoriteUseCase
import com.example.facefit.domain.usecases.glasses.GetGlassesByIdUseCase
import com.example.facefit.domain.usecases.glasses.GetRecommendedGlassesUseCase
import com.example.facefit.domain.usecases.reviews.GetReviewsUseCase
import com.example.facefit.domain.utils.NetworkUtils
import com.example.facefit.domain.utils.Resource
import com.example.facefit.ui.presentation.base.RefreshableViewModel
import com.example.facefit.ui.presentation.components.ProductItem
import com.example.facefit.ui.presentation.components.toProductItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val getGlassesByIdUseCase: GetGlassesByIdUseCase,
    private val getRecommendedGlassesUseCase: GetRecommendedGlassesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val authManager: TokenManager,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val getReviewsUseCase: GetReviewsUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    @ApplicationContext private val context: Context // Inject Context
) : ViewModel(), RefreshableViewModel {

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
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Please check your internet connection.",
                    glasses = null // Clear product on network error
                )
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) } // Clear previous error
            loadFavorites()
            loadUserProfile()

            try {
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
                        handleGenericError(result.message, _uiState)
                    }

                    is Resource.Loading -> Unit
                }
            } catch (e: Exception) {
                handleGenericError(e.message, _uiState)
            }
        }
    }

    private fun loadRecommendations(productId: String, gender: String, type: String, material: String) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _recommendations.value = List(3) { // Still show placeholders
                ProductItem(
                    id = "",
                    name = "Couldn't load",
                    price = "---",
                    imageUrl = null,
                    isFavorite = false,
                    isPlaceholder = true
                )
            }
            Log.e("ProductDetailsVM", "No network for recommendations. Displaying placeholders.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                when (val result = getRecommendedGlassesUseCase(productId, gender, type, material)) {
                    is Resource.Success -> {
                        _recommendations.value = result.data?.map { it.toProductItem() } ?: emptyList()
                    }
                    is Resource.Error -> {
                        Log.e("ProductDetailsVM", "Error loading recommendations: ${result.message}")
                        _recommendations.value = List(3) {
                            ProductItem(
                                id = "",
                                name = "Couldn't load",
                                price = "---",
                                imageUrl = null,
                                isFavorite = false,
                                isPlaceholder = true
                            )
                        }
                    }
                    is Resource.Loading -> Unit
                }
            } catch (e: Exception) {
                Log.e("ProductDetailsVM", "Exception loading recommendations: ${e.message}", e)
                _recommendations.value = List(3) {
                    ProductItem(
                        id = "",
                        name = "Couldn't load",
                        price = "---",
                        imageUrl = null,
                        isFavorite = false,
                        isPlaceholder = true
                    )
                }
            }
        }
    }


    private fun loadFavorites() {
        val token = authManager.getToken()
        if (token == null) {
            Log.d("ProductDetailsVM", "No token available to load favorites.")
            return
        }
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.e("ProductDetailsVM", "No network for loadFavorites.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                when (val result = getFavoritesUseCase(token)) {
                    is Resource.Success -> {
                        _favoriteStatus.update {
                            result.data?.associate { it.id to true } ?: emptyMap()
                        }
                    }
                    is Resource.Error -> {
                        Log.e("ProductDetailsVM", "Error loading favorites: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Optional: show loading state if needed
                    }
                }
            } catch (e: Exception) {
                Log.e("ProductDetailsVM", "Exception loading favorites: ${e.message}", e)
            }
        }
    }

    fun toggleFavorite(productId: String) {
        val token = authManager.getToken()
        if (token == null) {
            _uiState.update { it.copy(error = "Authentication required to toggle favorite.") }
            Log.e("ProductDetailsVM", "Authentication token missing for favorite toggle.")
            return
        }
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _uiState.update { it.copy(error = "Please check your internet connection.") }
            Log.e("ProductDetailsVM", "No network to toggle favorite.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val currentStatus = _favoriteStatus.value[productId] ?: false

            _favoriteStatus.update { it + (productId to !currentStatus) }
            _pendingFavorites.update { it + productId }

            try {
                when (val result = toggleFavoriteUseCase(token, productId)) {
                    is Resource.Success -> loadFavorites()
                    is Resource.Error -> {
                        _favoriteStatus.update { it + (productId to currentStatus) } // Revert
                        handleGenericError(result.message, _uiState)
                    }
                    is Resource.Loading -> Unit // No action on loading
                }
            } catch (e: Exception) {
                _favoriteStatus.update { it + (productId to currentStatus) } // Revert
                handleGenericError(e.message, _uiState)
            } finally {
                _pendingFavorites.update { it - productId }
            }
        }
    }

    fun toggleRecommendedFavorite(productId: String) = toggleFavorite(productId)

    fun loadReviews(glassesId: String) {
        val token = authManager.getToken()
        if (token == null) {
            _uiState.update {
                it.copy(error = "Authentication required to view reviews")
            }
            Log.e("ProductDetailsVM", "No token to load reviews.")
            return
        }
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _uiState.update {
                it.copy(error = "Please check your internet connection.")
            }
            Log.e("ProductDetailsVM", "No network to load reviews.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
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
                        handleGenericError(result.message, _uiState)
                    }
                    is Resource.Loading -> {
                        // Handle loading state if needed
                    }
                }
            } catch (e: Exception) {
                handleGenericError(e.message, _uiState)
            }
        }
    }

    private fun loadUserProfile() {
        val token = authManager.getToken()
        if (token == null) {
            Log.d("ProductDetailsVM", "No token available for user profile.")
            return
        }
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.e("ProductDetailsVM", "No network for user profile.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                when (val result = getUserProfileUseCase(token)) {
                    is Resource.Success -> {
                        _userProfile.value = result.data
                    }
                    is Resource.Error -> {
                        Log.e("ProductDetailsVM", "Error loading user profile: ${result.message}")
                    }
                    is Resource.Loading -> Unit
                }
            } catch (e: Exception) {
                Log.e("ProductDetailsVM", "Exception loading user profile: ${e.message}", e)
            }
        }
    }

    fun retryLoadingReviews(glassesId: String) {
        loadReviews(glassesId)
    }

    override fun refresh() {
        val productId = uiState.value.glasses?.id
        if (productId != null) {
            loadProductDetails(productId)
        } else {
            Log.w("ProductDetailsVM", "Cannot refresh: product ID is null.")
            _uiState.update { it.copy(error = "Cannot refresh: product ID not found.") }
        }
    }

    // Generic error handler for ProductDetailsViewModel
    private fun handleGenericError(errorMessage: String?, stateFlow: MutableStateFlow<ProductDetailsUiState>) {
        val userFriendlyMessage: String
        val logMessage: String = errorMessage ?: "Unknown error"

        when {
            errorMessage?.contains("internet connection", ignoreCase = true) == true ||
                    errorMessage?.contains("network error", ignoreCase = true) == true ||
                    errorMessage?.contains("timeout", ignoreCase = true) == true ||
                    errorMessage?.contains("Unable to resolve host", ignoreCase = true) == true -> {
                userFriendlyMessage = "Please check your internet connection."
            }
            else -> {
                userFriendlyMessage = "Something went wrong"
            }
        }

        Log.e("ProductDetailsVM", "API Call Error: $logMessage")
        stateFlow.update {
            it.copy(
                error = userFriendlyMessage,
                isLoading = false,
                glasses = null // Clear glasses data on error
            )
        }
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