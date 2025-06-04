package com.example.facefit.ui.presentation.screens.products

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
import com.example.facefit.domain.utils.Resource
import com.example.facefit.ui.presentation.base.RefreshableViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllProductsViewModel @Inject constructor(
    private val getAllGlassesUseCase: GetAllGlassesUseCase,
    private val getBestSellersUseCase: GetBestSellersUseCase,
    private val getNewArrivalsUseCase: GetNewArrivalsUseCase,
    private val filterGlassesUseCase: FilterGlassesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val authManager: TokenManager
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

    init {
        loadAllProducts()
        loadFavorites()
    }


    fun loadAllProducts() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    selectedTab = 0,
                    selectedSort = SORT_DEFAULT
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
                    val friendlyMessage = if (result.message?.contains("Unable to resolve host") == true) {
                        "Please check your internet connection."
                    } else {
                        "Failed to load products. Please try again."
                    }
                    _uiState.update {
                        it.copy(
                            error = friendlyMessage,
                            isLoading = false,
                            products = result.data ?: emptyList()
                        )
                    }
                }
                is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
            }
        }
    }


    fun filterByCategory(category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(isLoading = true)
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
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    selectedSort = sortOption,
                    selectedTab = 0
                )
            }

            val result = when (sortOption) {
                SORT_BEST_SELLERS -> getBestSellersUseCase()
                SORT_NEW_ARRIVALS -> getNewArrivalsUseCase()
                else -> getAllGlassesUseCase()
            }

            _uiState.update {
                when (result) {
                    is Resource.Success -> it.copy(
                        products = result.data ?: emptyList(),
                        isLoading = false,
                        error = null
                    )

                    is Resource.Error -> {
                        val friendlyMessage = if (result.message?.contains("Unable to resolve host") == true) {
                            "Please check your internet connection."
                        } else {
                            "Failed to sort products. Please try again."
                        }
                        it.copy(
                            error = friendlyMessage,
                            isLoading = false,
                            products = result.data ?: emptyList()
                        )
                    }

                    is Resource.Loading -> it.copy(isLoading = true)
                }
            }
        }
    }


    fun filterByType(tabIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { 
                it.copy(
                    isLoading = true,
                    selectedTab = tabIndex,
                    selectedSort = SORT_DEFAULT
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
        type: String? = null,
        gender: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        shape: String? = null,
        material: String? = null,
        sort: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    priceRangeMin = minPrice,
                    priceRangeMax = maxPrice,
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
                            error = null,
                            selectedType = type,
                            selectedGender = gender,
                            priceRangeMin = minPrice,
                            priceRangeMax = maxPrice,
                        )
                    }
                }

                is Resource.Error -> {
                    val friendlyMessage = if (result.message?.contains("Unable to resolve host") == true) {
                        "Please check your internet connection."
                    } else {
                        "Failed to filter products. Please try again."
                    }

                    _uiState.update {
                        it.copy(
                            error = friendlyMessage,
                            isLoading = false,
                            products = result.data ?: emptyList()
                        )
                    }
                }

                is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
            }
        }
    }


    fun loadFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            val token = authManager.getToken() ?: return@launch
            when (val result = getFavoritesUseCase(token)) {
                is Resource.Success -> {
                    val newStatus = result.data?.associate { it.id to true } ?: emptyMap()
                    _favoriteStatus.update { newStatus }
                }

                is Resource.Error -> {
                    Log.e("AllProductsVM", "Error loading favorites: ${result.message}")

                }

                is Resource.Loading -> {  }
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
                is Resource.Success -> {
                    loadFavorites()
                }
                is Resource.Error -> {
                    _favoriteStatus.update { it + (productId to currentStatus) }
                    Log.e("ViewModel", "Failed to toggle favorite: ${result.message}")
                }
                is Resource.Loading -> {

                }
            }

            _pendingFavorites.update { it - productId }
        }
    }
    private val _toastTrigger = MutableStateFlow(0)
    val toastTrigger: StateFlow<Int> = _toastTrigger.asStateFlow()

    override fun refresh() {
        loadAllProducts()
        loadFavorites()
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
    val priceRangeMax: Double? = null
)