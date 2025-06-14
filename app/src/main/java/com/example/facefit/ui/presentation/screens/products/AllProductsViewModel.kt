package com.example.facefit.ui.presentation.screens.products

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.data.local.TokenManager
import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.usecases.favorites.GetFavoritesUseCase
import com.example.facefit.domain.usecases.favorites.ToggleFavoriteUseCase
import com.example.facefit.domain.usecases.glasses.FilterGlassesUseCase
import com.example.facefit.domain.usecases.glasses.GetAllGlassesUseCase
import com.example.facefit.domain.usecases.glasses.GetBestSellersUseCase
import com.example.facefit.domain.usecases.glasses.GetNewArrivalsUseCase
import com.example.facefit.domain.utils.NetworkUtils
import com.example.facefit.domain.utils.Resource
import com.example.facefit.ui.presentation.base.RefreshableViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
class AllProductsViewModel @Inject constructor(
    private val getAllGlassesUseCase: GetAllGlassesUseCase,
    private val getBestSellersUseCase: GetBestSellersUseCase,
    private val getNewArrivalsUseCase: GetNewArrivalsUseCase,
    private val filterGlassesUseCase: FilterGlassesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val authManager: TokenManager,
    @ApplicationContext private val context: Context // Inject Context
) : ViewModel(), RefreshableViewModel {

    companion object {
        const val SORT_DEFAULT = "Default"
        const val SORT_BEST_SELLERS = "Best Sellers"
        const val SORT_NEW_ARRIVALS = "New Arrivals"

        const val EYEGLASSES = "eyeglasses"
        const val SUNGLASSES = "sunglasses"
    }

    private val _uiState = MutableStateFlow(AllProductsUiState())
    val uiState: StateFlow<AllProductsUiState> = _uiState.asStateFlow()

    private val _favoriteStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val favoriteStatus: StateFlow<Map<String, Boolean>> = _favoriteStatus.asStateFlow()

    private val _pendingFavorites = MutableStateFlow<Set<String>>(emptySet())
    val pendingFavorites: StateFlow<Set<String>> = _pendingFavorites.asStateFlow()

    private val _toastTrigger = MutableStateFlow(0)
    val toastTrigger: StateFlow<Int> = _toastTrigger.asStateFlow()

    init {
        loadAllProducts()
        loadFavorites()
    }

    fun loadAllProducts() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Please check your internet connection.",
                    products = emptyList() // Ensure products are empty on network error
                )
            }
            _toastTrigger.update { it + 1 }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    selectedTab = 0,
                    selectedSort = SORT_DEFAULT,
                    activeFilters = emptyMap(),
                    error = null // Clear previous errors
                )
            }

            when (val result = getAllGlassesUseCase()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            products = result.data ?: emptyList(),
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    handleGenericError(result.message, result.data, _uiState)
                }
                is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    fun filterByCategory(category: String) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Please check your internet connection.",
                    products = emptyList()
                )
            }
            _toastTrigger.update { it + 1 }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(isLoading = true, error = null) // Clear previous errors
            }

            val (type, gender) = when (category) {
                "Men" -> Pair(null, "Men")
                "Women" -> Pair(null, "Women")
                "Eye Glasses" -> Pair(EYEGLASSES, null)
                "Sun Glasses" -> Pair(SUNGLASSES, null)
                else -> Pair(null, null)
            }

            filterProducts(type = type, gender = gender)
        }
    }

    fun sortProducts(sortOption: String) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Please check your internet connection.",
                    products = emptyList()
                )
            }
            _toastTrigger.update { it + 1 }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    selectedSort = sortOption,
                    selectedTab = 0,
                    error = null // Clear previous errors
                )
            }

            when (val result = when (sortOption) {
                SORT_BEST_SELLERS -> getBestSellersUseCase()
                SORT_NEW_ARRIVALS -> getNewArrivalsUseCase()
                else -> getAllGlassesUseCase()
            }) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            products = result.data ?: emptyList(),
                            isLoading = false,
                            error = null
                        )
                    }
                }

                is Resource.Error -> {
                    handleGenericError(result.message, result.data, _uiState)
                }

                is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    fun filterByType(tabIndex: Int) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Please check your internet connection.",
                    products = emptyList()
                )
            }
            _toastTrigger.update { it + 1 }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    selectedTab = tabIndex,
                    selectedSort = SORT_DEFAULT,
                    error = null // Clear previous errors
                )
            }

            val type = when (tabIndex) {
                1 -> EYEGLASSES
                2 -> SUNGLASSES
                else -> null
            }

            filterProducts(type = type)
        }
    }

    fun filterProducts(
        gender: String? = null,
        type: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        shape: String? = null,
        material: String? = null,
        sort: String? = null
    ) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Please check your internet connection.",
                    products = emptyList()
                )
            }
            _toastTrigger.update { it + 1 }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val tabIndex = when (type) {
                SUNGLASSES -> 2
                EYEGLASSES -> 1
                else -> 0
            }

            _uiState.update {
                it.copy(
                    isLoading = true,
                    priceRangeMin = minPrice,
                    priceRangeMax = maxPrice,
                    selectedType = type,
                    selectedGender = gender,
                    selectedTab = tabIndex,
                    activeFilters = mapOf(
                        "gender" to gender,
                        "type" to type,
                        "minPrice" to minPrice,
                        "maxPrice" to maxPrice,
                        "shape" to shape,
                        "material" to material
                    ),
                    error = null // Clear previous errors
                )
            }

            when (val result = filterGlassesUseCase(
                type = type,
                gender = gender,
                minPrice = minPrice,
                maxPrice = maxPrice,
                shape = shape,
                material = material,
                sort = sort
            )) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            products = result.data ?: emptyList(),
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    handleGenericError(result.message, result.data, _uiState)
                }
                is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                selectedType = null,
                selectedGender = null,
                priceRangeMin = null,
                priceRangeMax = null,
                selectedTab = 0,
                isLoading = true,
                activeFilters = emptyMap(),
                error = null // Clear previous errors
            )
        }
        loadAllProducts()
    }

    override fun refresh() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Please check your internet connection.",
                    products = emptyList()
                )
            }
            _toastTrigger.update { it + 1 }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) } // Clear previous errors

            // Delay for better UX when pulling to refresh
            delay(800)

            filterProducts(
                type = _uiState.value.selectedType,
                gender = _uiState.value.selectedGender,
                minPrice = _uiState.value.priceRangeMin,
                maxPrice = _uiState.value.priceRangeMax,
            )
            loadFavorites()
        }
    }

    fun loadFavorites() {
        val token = authManager.getToken()
        if (token == null) {
            Log.e("AllProductsVM", "No token available to load favorites.")
            return // No error message to UI, this is a background operation
        }
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.e("AllProductsVM", "No network available for loadFavorites.")
            return // No error message to UI, this is a background operation
        }

        viewModelScope.launch(Dispatchers.IO) {
            when (val result = getFavoritesUseCase(token)) {
                is Resource.Success -> {
                    val newStatus = result.data?.associate { it.id to true } ?: emptyMap()
                    _favoriteStatus.update { newStatus }
                }

                is Resource.Error -> {
                    Log.e("AllProductsVM", "Error loading favorites: ${result.message}")
                }

                is Resource.Loading -> { }
            }
        }
    }

    fun toggleFavorite(productId: String) {
        val token = authManager.getToken()
        if (token == null) {
            _uiState.update { it.copy(error = "Authentication required to toggle favorite.") }
            _toastTrigger.update { it + 1 }
            Log.e("AllProductsVM", "Authentication token missing for favorite toggle.")
            return
        }
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _uiState.update { it.copy(error = "Please check your internet connection.") }
            _toastTrigger.update { it + 1 }
            Log.e("AllProductsVM", "No network available to toggle favorite.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val currentStatus = _favoriteStatus.value[productId] ?: false
            _favoriteStatus.update { it + (productId to !currentStatus) }
            _pendingFavorites.update { it + productId }

            try {
                when (val result = toggleFavoriteUseCase(token, productId)) {
                    is Resource.Success -> {
                        loadFavorites()
                    }
                    is Resource.Error -> {
                        _favoriteStatus.update { it + (productId to currentStatus) }
                        Log.e("AllProductsVM", "Failed to toggle favorite: ${result.message}")
                        _uiState.update { it.copy(error = result.message ?: "Failed to toggle favorite.") }
                        _toastTrigger.update { it + 1 }
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _favoriteStatus.update { it + (productId to currentStatus) }
                handleGenericError(e.message, null, _uiState) // Use handleGenericError for exceptions
            } finally {
                _pendingFavorites.update { it - productId }
            }
        }
    }

    // Generic error handler for ViewModel
    private fun handleGenericError(
        errorMessage: String?,
        currentData: List<Glasses>?,
        stateFlow: MutableStateFlow<AllProductsUiState>
    ) {
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

        Log.e("AllProductsVM", "API Call Error: $logMessage")
        stateFlow.update {
            it.copy(
                error = userFriendlyMessage,
                isLoading = false,
                products = currentData ?: emptyList() // Preserve data if available, else empty
            )
        }
        _toastTrigger.update { it + 1 }
    }
}

data class AllProductsUiState(
    val products: List<Glasses> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedType: String? = null,
    val selectedGender: String? = null,
    val selectedSort: String = AllProductsViewModel.SORT_DEFAULT,
    val selectedTab: Int = 0,
    val priceRangeMin: Double? = null,
    val priceRangeMax: Double? = null,
    val activeFilters: Map<String, Any?> = emptyMap()
)