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
class AllProductsViewModel @Inject constructor(
    private val repository: GlassesRepository
) : ViewModel() {

    companion object {
        const val SORT_DEFAULT = "Default"
        const val SORT_BEST_SELLERS = "Best Sellers"
        const val SORT_NEW_ARRIVALS = "New Arrivals"
        
        const val EYEGLASSES = "eyeglasses"
        const val SUNGLASSES = "sunglasses"
    }

    private val _uiState = MutableStateFlow(AllProductsUiState())
    val uiState: StateFlow<AllProductsUiState> = _uiState.asStateFlow()

    init {
        loadAllProducts()
    }

    fun loadAllProducts() {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = true,
                    selectedTab = 0,
                    selectedSort = SORT_DEFAULT
                )
            }
            
            when (val result = repository.getAllGlasses()) {
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
                    _uiState.update {
                        it.copy(
                            error = result.message,
                            isLoading = false,
                            products = result.data ?: emptyList()
                        )
                    }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun filterByCategory(category: String) {
        viewModelScope.launch {
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
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = true, 
                    selectedSort = sortOption,
                    selectedTab = 0
                )
            }
            
            val result = when (sortOption) {
                SORT_BEST_SELLERS -> repository.getBestSellers()
                SORT_NEW_ARRIVALS -> repository.getNewArrivals()
                else -> repository.getAllGlasses()
            }
            
            _uiState.update {
                when (result) {
                    is Resource.Success -> it.copy(
                        products = result.data ?: emptyList(),
                        isLoading = false,
                        error = null
                    )
                    is Resource.Error -> it.copy(
                        error = result.message,
                        isLoading = false,
                        products = result.data ?: emptyList()
                    )
                    is Resource.Loading -> it.copy(isLoading = true)
                }
            }
        }
    }
    
    fun filterByType(tabIndex: Int) {
        viewModelScope.launch {
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
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    priceRangeMin = minPrice,
                    priceRangeMax = maxPrice
                )
            }

            when (val result = repository.filterGlasses(
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
                            priceRangeMin = minPrice?.toInt(),
                            priceRangeMax = maxPrice?.toInt()
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            error = result.message,
                            isLoading = false,
                            products = result.data ?: emptyList()
                        )
                    }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun toggleFavorite(productId: String) {
        _uiState.update { state ->
            val updatedProducts = state.products.map { glasses ->
                if (glasses.id == productId) {
                    glasses.copy(isFavorite = !glasses.isFavorite)
                } else {
                    glasses
                }
            }
            state.copy(products = updatedProducts)
        }
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