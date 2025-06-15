package com.example.facefit.ui.presentation.screens.cart

import android.content.Context
import android.util.Log
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
import com.example.facefit.domain.utils.NetworkUtils
import com.example.facefit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _userProfile = MutableStateFlow<Resource<User>>(Resource.Loading())
    val userProfile: StateFlow<Resource<User>> = _userProfile.asStateFlow()

    private val _cartTotal = MutableStateFlow<Resource<Double>>(Resource.Loading())
    val cartTotal: StateFlow<Resource<Double>> = _cartTotal.asStateFlow()

    private val _orderStatus = MutableStateFlow<Resource<String>>(Resource.Success(""))
    val orderStatus: StateFlow<Resource<String>> = _orderStatus.asStateFlow()

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

    fun loadCartData() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _cartItems.value = Resource.Error("Please check your internet connection.", emptyList())
            _cartTotal.value = Resource.Error("Please check your internet connection.", 0.0)
            return
        }

        viewModelScope.launch {
            _cartItems.value = Resource.Loading()
            _cartTotal.value = Resource.Loading()
            try {
                when (val result = cartRepository.getCart()) {
                    is Resource.Success -> {
                        result.data?.let { cartData ->
                            _cartItems.value = Resource.Success(cartData.items)
                            _cartTotal.value = Resource.Success(cartData.totalAmount)
                        } ?: run {
                            Log.e("CheckoutViewModel", "loadCartData: Cart data is null for successful response.")
                            _cartItems.value = Resource.Error("Something went wrong.", emptyList())
                            _cartTotal.value = Resource.Error("Something went wrong.", 0.0)
                        }
                    }
                    is Resource.Error -> {
                        // Pass appropriate default data for the type
                        handleGenericError(result.message, emptyList(), _cartItems)
                        handleGenericError(result.message, 0.0, _cartTotal)
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                // Pass appropriate default data for the type
                handleGenericError(e.message, emptyList(), _cartItems)
                handleGenericError(e.message, 0.0, _cartTotal)
            }
        }
    }

    fun loadUserProfile() {
        val token = tokenManager.getToken()
        if (token.isNullOrEmpty()) {
            _userProfile.value = Resource.Error("User not authenticated", null)
            Log.e("CheckoutViewModel", "loadUserProfile: User not authenticated.")
            return
        }

        if (!NetworkUtils.isNetworkAvailable(context)) {
            _userProfile.value = Resource.Error("Please check your internet connection.", null)
            return
        }

        viewModelScope.launch {
            _userProfile.value = Resource.Loading()
            try {
                when (val result = userRepository.getUserProfile(token)) {
                    is Resource.Success -> {
                        result.data?.let { user ->
                            _userProfile.value = Resource.Success(user)
                            user.address?.let { address ->
                                val parts = address.split(",").map { it.trim() }
                                when (parts.size) {
                                    4 -> {
                                        streetName = parts[0]
                                        buildingNameNo = parts[1]
                                        floorApartmentVillaNo = parts[2]
                                        cityArea = parts[3]
                                    }
                                    else -> cityArea = address
                                }
                            }
                            phoneNumber = user.phone ?: ""
                        } ?: run {
                            Log.e("CheckoutViewModel", "loadUserProfile: User data is null for successful response.")
                            _userProfile.value = Resource.Error("Something went wrong.", null)
                        }
                    }
                    is Resource.Error -> {
                        // Pass appropriate default data for the type
                        handleGenericError(result.message, null, _userProfile)
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                // Pass appropriate default data for the type
                handleGenericError(e.message, null, _userProfile)
            }
        }
    }

    fun calculateCartTotal() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _cartTotal.value = Resource.Error("Please check your internet connection.", 0.0)
            return
        }

        viewModelScope.launch {
            _cartTotal.value = Resource.Loading()
            try {
                when (val result = cartRepository.getCart()) {
                    is Resource.Success -> {
                        result.data?.let { cartData ->
                            _cartTotal.value = Resource.Success(cartData.totalAmount)
                        } ?: run {
                            Log.e("CheckoutViewModel", "calculateCartTotal: Cart data is null for successful response.")
                            _cartTotal.value = Resource.Error("Something went wrong.", 0.0)
                        }
                    }
                    is Resource.Error -> {
                        // Pass appropriate default data for the type
                        handleGenericError(result.message, 0.0, _cartTotal)
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                // Pass appropriate default data for the type
                handleGenericError(e.message, 0.0, _cartTotal)
            }
        }
    }

    fun loadCartItems() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _cartItems.value = Resource.Error("Please check your internet connection.", emptyList())
            return
        }

        viewModelScope.launch {
            _cartItems.value = Resource.Loading()
            try {
                when (val result = cartRepository.getCart()) {
                    is Resource.Success -> {
                        result.data?.let { cartData ->
                            _cartItems.value = Resource.Success(cartData.items)
                        } ?: run {
                            Log.e("CheckoutViewModel", "loadCartItems: Cart data is null for successful response.")
                            _cartItems.value = Resource.Error("Something went wrong.", emptyList())
                        }
                    }
                    is Resource.Error -> {
                        // Pass appropriate default data for the type
                        handleGenericError(result.message, emptyList(), _cartItems)
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                // Pass appropriate default data for the type
                handleGenericError(e.message, emptyList(), _cartItems)
            }
        }
    }

    fun createOrder() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _orderStatus.value = Resource.Error("Please check your internet connection.")
            return
        }

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

            try {
                when (val result = orderRepository.createOrder(
                    address = address,
                    phone = phoneNumber,
                    paymentMethod = "cash"
                )) {
                    is Resource.Success -> {
                        _orderStatus.value = Resource.Success("Order placed successfully")
                    }
                    is Resource.Error -> {
                        // Pass appropriate default data for the type
                        handleGenericError(result.message, "", _orderStatus)
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                // Pass appropriate default data for the type
                handleGenericError(e.message, "", _orderStatus)
            }
        }
    }

    fun calculateTotal(): Double {
        val subtotal = (_cartTotal.value as? Resource.Success)?.data ?: 0.0
        val tax = subtotal * 0.14
        val shipping = 50.0
        return subtotal + tax + shipping
    }

    fun resetOrderStatus() {
        _orderStatus.value = Resource.Success("")
    }

    // Generic error handler for ViewModel
    // Note: The `initialData` parameter is now explicitly typed to `T` (nullable)
    // to match the generic `Resource<T>`
    private fun <T> handleGenericError(errorMessage: String?, initialData: T?, stateFlow: MutableStateFlow<Resource<T>>) {
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

        Log.e("CheckoutViewModel", "API Call Error: $logMessage")
        stateFlow.value = Resource.Error(userFriendlyMessage, initialData)
    }
}