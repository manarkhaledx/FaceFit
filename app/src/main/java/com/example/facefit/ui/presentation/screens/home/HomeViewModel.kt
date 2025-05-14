package com.example.facefit.ui.presentation.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.data.local.TokenManager
import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.usecases.favorites.GetFavoritesUseCase
import com.example.facefit.domain.usecases.favorites.ToggleFavoriteUseCase
import com.example.facefit.domain.usecases.glasses.GetBestSellersUseCase
import com.example.facefit.domain.usecases.glasses.GetNewArrivalsUseCase
import com.example.facefit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getBestSellersUseCase: GetBestSellersUseCase,
    private val getNewArrivalsUseCase: GetNewArrivalsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val authManager: TokenManager
) : ViewModel() {
    private val _bestSellers = MutableStateFlow<Resource<List<Glasses>>>(Resource.Loading())
    val bestSellers: StateFlow<Resource<List<Glasses>>> = _bestSellers

    private val _newArrivals = MutableStateFlow<Resource<List<Glasses>>>(Resource.Loading())
    val newArrivals: StateFlow<Resource<List<Glasses>>> = _newArrivals

    private val _filteredProducts = MutableStateFlow<Resource<List<Glasses>>>(Resource.Loading())
    val filteredProducts: StateFlow<Resource<List<Glasses>>> = _filteredProducts

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    private val _favoriteStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val favoriteStatus: StateFlow<Map<String, Boolean>> = _favoriteStatus.asStateFlow()

    private val _pendingFavorites = MutableStateFlow<Set<String>>(emptySet())
    val pendingFavorites: StateFlow<Set<String>> = _pendingFavorites.asStateFlow()

    init {
        getBestSellers()
        getNewArrivals()
        loadFavorites()
    }

    private fun getBestSellers() {
        viewModelScope.launch {
            _bestSellers.value = getBestSellersUseCase()
        }
    }

    private fun getNewArrivals() {
        viewModelScope.launch {
            _newArrivals.value = getNewArrivalsUseCase()
        }
    }

    fun getProductsByCategory(category: String) {
        _selectedCategory.value = category
        viewModelScope.launch {
            _filteredProducts.value = Resource.Loading()
            try {
                val allProducts = mutableListOf<Glasses>()
                (bestSellers.value as? Resource.Success)?.data?.let { allProducts.addAll(it) }
                (newArrivals.value as? Resource.Success)?.data?.let { allProducts.addAll(it) }

                val filtered = when (category) {
                    "Men" -> allProducts.filter { it.gender == "Men" }
                    "Women" -> allProducts.filter { it.gender == "Women" }
                    "Eye Glasses" -> allProducts.filter { it.type == "eyeglasses" }
                    "Sun Glasses" -> allProducts.filter { it.type == "sunglasses" }
                    else -> allProducts
                }

                _filteredProducts.value = Resource.Success(filtered)
            } catch (e: Exception) {
                _filteredProducts.value = Resource.Error(e.message ?: "Unknown error")
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
                is Resource.Loading -> {
                    // Handle loading state
                }
            }
        }
    }

    fun toggleFavorite(productId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = authManager.getToken() ?: return@launch

            // Optimistic update
            val currentStatus = _favoriteStatus.value[productId] ?: false
            _favoriteStatus.update { it + (productId to !currentStatus) }
            _pendingFavorites.update { it + productId }

            when (val result = toggleFavoriteUseCase(token, productId)) {
                is Resource.Success -> {
                    getBestSellers()
                    getNewArrivals()
                    loadFavorites()
                }
                is Resource.Error -> {
                    _favoriteStatus.update { it + (productId to currentStatus) }
                }

                is Resource.Loading -> TODO()
            }

            _pendingFavorites.update { it - productId }
        }
    }

    fun refresh() {
        getBestSellers()
        getNewArrivals()
        _selectedCategory.value?.let { getProductsByCategory(it) }
    }
}