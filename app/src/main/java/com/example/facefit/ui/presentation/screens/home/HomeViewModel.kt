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
import com.example.facefit.ui.presentation.base.RefreshableViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
) : ViewModel(), RefreshableViewModel {
    private val _toastTrigger = MutableStateFlow(0)
    val toastTrigger: StateFlow<Int> = _toastTrigger.asStateFlow()
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

    private val _searchResults = MutableStateFlow<Resource<List<Glasses>>>(Resource.Success(emptyList()))
    val searchResults: StateFlow<Resource<List<Glasses>>> = _searchResults.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchPage = MutableStateFlow(1)
    val searchPage: StateFlow<Int> = _searchPage.asStateFlow()

    private val _hasMoreSearchResults = MutableStateFlow(true)
    val hasMoreSearchResults: StateFlow<Boolean> = _hasMoreSearchResults.asStateFlow()

    init {
        getBestSellers()
        getNewArrivals()
        loadFavorites()
    }


    private fun getBestSellers() {
        viewModelScope.launch {
            val result = getBestSellersUseCase()
            _bestSellers.value = result
            if (result is Resource.Error) {
                _toastTrigger.update { it + 1 }
            }
        }
    }

    private fun getNewArrivals() {
        viewModelScope.launch {
            val result = getNewArrivalsUseCase()
            _newArrivals.value = result
            if (result is Resource.Error) {
                _toastTrigger.update { it + 1 }
            }
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

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = Resource.Success(emptyList())
        } else {
            performSearch(query)
        }
    }

    fun loadMoreSearchResults() {
        if (_hasMoreSearchResults.value) {
            _searchPage.value += 1
            performSearch(_searchQuery.value)
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            if (_searchPage.value == 1) {
                _searchResults.value = Resource.Loading()
            }

            try {
                val allProducts = mutableListOf<Glasses>()
                (bestSellers.value as? Resource.Success)?.data?.let { allProducts.addAll(it) }
                (newArrivals.value as? Resource.Success)?.data?.let { allProducts.addAll(it) }

                val filtered = allProducts.filter {
                    it.name.contains(query, ignoreCase = true)
                }

                val pageSize = 10
                val startIndex = (_searchPage.value - 1) * pageSize
                val endIndex = minOf(startIndex + pageSize, filtered.size)
                val pagedResults = filtered.subList(startIndex, endIndex)

                _hasMoreSearchResults.value = endIndex < filtered.size

                if (_searchPage.value == 1) {
                    _searchResults.value = Resource.Success(pagedResults)
                } else {
                    val currentResults = (_searchResults.value as? Resource.Success)?.data ?: emptyList()
                    _searchResults.value = Resource.Success(currentResults + pagedResults)
                }
            } catch (e: Exception) {
                _searchResults.value = Resource.Error(e.message ?: "Error during search")
            }
        }
    }

    fun resetSearch() {
        _searchPage.value = 1
        _hasMoreSearchResults.value = true
        _searchQuery.value = ""
        _searchResults.value = Resource.Success(emptyList())
    }

     override fun refresh() {
        viewModelScope.launch {
            _bestSellers.value = Resource.Loading()
            _newArrivals.value = Resource.Loading()

            delay(800)

            getBestSellers()
            getNewArrivals()
            _selectedCategory.value?.let { getProductsByCategory(it) }
        }
    }

}