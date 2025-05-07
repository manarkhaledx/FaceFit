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

    fun refresh() {
        getBestSellers()
        getNewArrivals()
    }
}