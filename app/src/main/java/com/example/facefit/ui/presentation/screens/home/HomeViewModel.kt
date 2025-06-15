package com.example.facefit.ui.presentation.screens.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.data.local.TokenManager
import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.usecases.favorites.GetFavoritesUseCase
import com.example.facefit.domain.usecases.favorites.ToggleFavoriteUseCase
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
class HomeViewModel @Inject constructor(
    private val getBestSellersUseCase: GetBestSellersUseCase,
    private val getNewArrivalsUseCase: GetNewArrivalsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val authManager: TokenManager,
    @ApplicationContext private val context: Context
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

    private fun handleNetworkCall(call: suspend () -> Resource<List<Glasses>>, stateFlow: MutableStateFlow<Resource<List<Glasses>>>) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            stateFlow.value = Resource.Error("Please check your internet connection.", null) // Pass null data
            _toastTrigger.update { it + 1 }
            return
        }
        viewModelScope.launch {
            val result = call()
            stateFlow.value = result
            if (result is Resource.Error) {
                Log.e("HomeViewModel", "Error fetching data: ${result.message}")
                _toastTrigger.update { it + 1 }
            }
        }
    }

    private fun getBestSellers() {
        handleNetworkCall({ getBestSellersUseCase() }, _bestSellers)
    }

    private fun getNewArrivals() {
        handleNetworkCall({ getNewArrivalsUseCase() }, _newArrivals)
    }

    fun getProductsByCategory(category: String) {
        _selectedCategory.value = category
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _filteredProducts.value = Resource.Error("Please check your internet connection.", null) // Pass null data
            _toastTrigger.update { it + 1 }
            return
        }
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
                handleGenericError(e, _filteredProducts)
            }
        }
    }

    fun loadFavorites() {
        val token = authManager.getToken()
        if (token == null) {
            Log.d("HomeViewModel", "No token available to load favorites.")
            return
        }
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.e("HomeViewModel", "No network to load favorites.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            when (val result = getFavoritesUseCase(token)) {
                is Resource.Success -> {
                    val newStatus = result.data?.associate { it.id to true } ?: emptyMap()
                    _favoriteStatus.update { newStatus }
                }
                is Resource.Error -> {
                    Log.e("HomeViewModel", "Error loading favorites: ${result.message}")
                }
                is Resource.Loading -> {
                    // Nothing to do for loading state here, as it's a background operation
                }
            }
        }
    }

    fun toggleFavorite(productId: String) {
        val token = authManager.getToken()
        if (token == null) {
            _toastTrigger.update { it + 1 }
            Log.e("HomeViewModel", "Authentication token missing for favorite toggle.")
            return
        }
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _toastTrigger.update { it + 1 }
            Log.e("HomeViewModel", "No network to toggle favorite.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val currentStatus = _favoriteStatus.value[productId] ?: false
            _favoriteStatus.update { it + (productId to !currentStatus) }
            _pendingFavorites.update { it + productId }

            try {
                when (val result = toggleFavoriteUseCase(token, productId)) {
                    is Resource.Success -> {
                        getBestSellers()
                        getNewArrivals()
                        loadFavorites()
                    }
                    is Resource.Error -> {
                        _favoriteStatus.update { it + (productId to currentStatus) }
                        Log.e("HomeViewModel", "Error toggling favorite for $productId: ${result.message}")
                        _toastTrigger.update { it + 1 }
                    }
                    is Resource.Loading -> {
                        // Not applicable for this synchronous-like action
                    }
                }
            } catch (e: Exception) {
                _favoriteStatus.update { it + (productId to currentStatus) }
                val userFriendlyMessage: String
                val logMessage: String

                when (e) {
                    is SocketTimeoutException -> {
                        userFriendlyMessage = "Please check your internet connection."
                        logMessage = "Timeout error: ${e.message}"
                    }
                    is IOException -> {
                        userFriendlyMessage = "Please check your internet connection."
                        logMessage = "Network error: ${e.message}"
                    }
                    is HttpException -> {
                        userFriendlyMessage = "Something went wrong"
                        logMessage = "HTTP error: ${e.code()} - ${e.message()}"
                    }
                    else -> {
                        userFriendlyMessage = "Something went wrong"
                        logMessage = "An unexpected error occurred: ${e.message}"
                    }
                }
                Log.e("HomeViewModel", logMessage, e)
                _toastTrigger.update { it + 1 }
            } finally {
                _pendingFavorites.update { it - productId }
            }
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
        if (_hasMoreSearchResults.value && _searchResults.value !is Resource.Loading) {
            _searchPage.value += 1
            performSearch(_searchQuery.value)
        }
    }

    private fun performSearch(query: String) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _searchResults.value = Resource.Error("Please check your internet connection.", null) // Pass null data
            _toastTrigger.update { it + 1 }
            return
        }
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
                handleGenericError(e, _searchResults)
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
            loadFavorites()
        }
    }

    private fun <T> handleGenericError(e: Exception, stateFlow: MutableStateFlow<Resource<T>>) {
        val userFriendlyMessage: String
        val logMessage: String

        when (e) {
            is SocketTimeoutException -> {
                userFriendlyMessage = "Please check your internet connection."
                logMessage = "Timeout error: ${e.message}"
            }
            is IOException -> {
                userFriendlyMessage = "Please check your internet connection."
                logMessage = "Network error: ${e.message}"
            }
            is HttpException -> {
                userFriendlyMessage = "Something went wrong"
                logMessage = "HTTP error: ${e.code()} - ${e.message()}"
            }
            else -> {
                userFriendlyMessage = "Something went wrong"
                logMessage = "An unexpected error occurred: ${e.message}"
            }
        }
        Log.e("HomeViewModel", logMessage, e)
        stateFlow.value = Resource.Error(userFriendlyMessage, null) // Pass null data here
        _toastTrigger.update { it + 1 }
    }
}