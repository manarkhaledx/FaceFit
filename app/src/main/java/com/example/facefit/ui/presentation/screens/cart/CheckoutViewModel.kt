package com.example.facefit.ui.presentation.screens.cart

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.data.local.TokenManager
import com.example.facefit.domain.models.CartItem
import com.example.facefit.domain.models.User
import com.example.facefit.domain.repository.CartRepository
import com.example.facefit.domain.repository.OrderRepository
import com.example.facefit.domain.repository.UserRepository
import com.example.facefit.domain.utils.Resource
import com.example.facefit.ui.presentation.screens.profile.ProfileState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _userProfile = MutableStateFlow<Resource<User>>(Resource.Loading())
    val userProfile: StateFlow<Resource<User>> = _userProfile.asStateFlow()

    private val _cartTotal = MutableStateFlow<Resource<Double>>(Resource.Loading())
    val cartTotal: StateFlow<Resource<Double>> = _cartTotal.asStateFlow()

    private val _orderStatus = MutableStateFlow<Resource<String>>(Resource.Success(""))
    val orderStatus: StateFlow<Resource<String>> = _orderStatus.asStateFlow()

    private val _userState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val userState: StateFlow<ProfileState> = _userState.asStateFlow()

    private val _cartItems = MutableStateFlow<Resource<List<CartItem>>>(Resource.Loading())
    val cartItems: StateFlow<Resource<List<CartItem>>> = _cartItems.asStateFlow()

    var streetName by mutableStateOf("")
    var buildingNameNo by mutableStateOf("")
    var floorApartmentVillaNo by mutableStateOf("")
    var cityArea by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var selectedPaymentOption by mutableStateOf("Cash on Delivery")

    init {
        loadUserProfile()
        loadCartData()
    }

    private fun loadCartData() {
        viewModelScope.launch {
            _cartItems.value = Resource.Loading()
            _cartTotal.value = Resource.Loading()

            when (val result = cartRepository.getCart()) {
                is Resource.Success -> {
                    result.data?.let { cartData ->
                        _cartItems.value = Resource.Success(cartData.items)
                        _cartTotal.value = Resource.Success(cartData.totalAmount)
                    } ?: run {
                        _cartItems.value = Resource.Error("Cart data is null")
                        _cartTotal.value = Resource.Error("Cart data is null")
                    }
                }
                is Resource.Error -> {
                    _cartItems.value = Resource.Error(result.message ?: "Error loading cart")
                    _cartTotal.value = Resource.Error(result.message ?: "Error loading cart")
                }
                else -> Unit
            }
        }
    }

    private fun loadUserProfile() {
        val token = tokenManager.getToken()
        if (token.isNullOrEmpty()) {
            _userState.value = ProfileState.Error("User not authenticated")
            return
        }
        viewModelScope.launch {
            _userProfile.value = Resource.Loading()
            when (val result = userRepository.getUserProfile(token)) {
                is Resource.Success -> {
                    result.data?.let { user ->
                        _userProfile.value = Resource.Success(user)
                        // Parse address if it exists
                        user.address?.let { address ->
                            // Simple parsing - you might need more sophisticated logic
                            val parts = address.split(",").map { it.trim() }
                            when (parts.size) {
                                4 -> {
                                    streetName = parts[0]
                                    buildingNameNo = parts[1]
                                    floorApartmentVillaNo = parts[2]
                                    cityArea = parts[3]
                                }
                                else -> cityArea = address // Fallback
                            }
                        }
                        phoneNumber = user.phone ?: ""
                    } ?: run {
                        _userProfile.value = Resource.Error("User data is null")
                    }
                }
                is Resource.Error -> {
                    _userProfile.value = Resource.Error(result.message ?: "Error loading profile")
                }
                else -> Unit
            }
        }
    }

    private fun loadCartTotal() {
        viewModelScope.launch {
            _cartTotal.value = Resource.Loading()
            when (val result = cartRepository.getCart()) {
                is Resource.Success -> {
                    result.data?.let { cartData ->
                        _cartTotal.value = Resource.Success(cartData.totalAmount)
                    } ?: run {
                        _cartTotal.value = Resource.Error("Cart data is null")
                    }
                }
                is Resource.Error -> {
                    _cartTotal.value = Resource.Error(result.message ?: "Error loading cart")
                }
                else -> Unit
            }
        }
    }

    fun createOrder() {
        viewModelScope.launch {
            _orderStatus.value = Resource.Loading()
            val address = listOf(streetName, buildingNameNo, floorApartmentVillaNo, cityArea)
                .filter { it.isNotBlank() }
                .joinToString(separator = ", ")

            if (address.isBlank()) {
                _orderStatus.value = Resource.Error("Address cannot be empty")
                return@launch
            }

            if (phoneNumber.isBlank()) {
                _orderStatus.value = Resource.Error("Phone number cannot be empty")
                return@launch
            }

            when (val result = orderRepository.createOrder(
                address = address,
                phone = phoneNumber,
                paymentMethod = "cash"
            )) {
                is Resource.Success -> {
                    _orderStatus.value = Resource.Success("Order placed successfully")
                }
                is Resource.Error -> {
                    _orderStatus.value = Resource.Error(result.message ?: "Error creating order")
                }
                else -> Unit
            }
        }
    }

    fun calculateTotal(): Double {
        val subtotal = (_cartTotal.value as? Resource.Success)?.data ?: 0.0
        val tax = subtotal * 0.14
        val shipping = 50.0
        return subtotal + tax + shipping
    }
}