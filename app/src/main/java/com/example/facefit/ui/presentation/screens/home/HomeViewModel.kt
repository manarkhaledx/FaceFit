package com.example.facefit.ui.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.usecases.GetBestSellersUseCase
import com.example.facefit.domain.usecases.GetNewArrivalsUseCase
import com.example.facefit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getBestSellersUseCase: GetBestSellersUseCase,
    private val getNewArrivalsUseCase: GetNewArrivalsUseCase
) : ViewModel() {
    private val _bestSellers = MutableStateFlow<Resource<List<Glasses>>>(Resource.Loading())
    val bestSellers: StateFlow<Resource<List<Glasses>>> = _bestSellers

    private val _newArrivals = MutableStateFlow<Resource<List<Glasses>>>(Resource.Loading())
    val newArrivals: StateFlow<Resource<List<Glasses>>> = _newArrivals

    private val _filteredProducts = MutableStateFlow<Resource<List<Glasses>>>(Resource.Loading())
    val filteredProducts: StateFlow<Resource<List<Glasses>>> = _filteredProducts

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    init {
        getBestSellers()
        getNewArrivals()
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

    fun refresh() {
        getBestSellers()
        getNewArrivals()
        _selectedCategory.value?.let { getProductsByCategory(it) }
    }
}