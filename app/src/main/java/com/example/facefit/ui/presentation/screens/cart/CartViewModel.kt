package com.example.facefit.ui.presentation.screens.cart

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.data.models.requests.AddToCartRequest
import com.example.facefit.domain.models.CartItem
import com.example.facefit.domain.usecases.cart.AddToCartUseCase
import com.example.facefit.domain.usecases.cart.ClearCartUseCase
import com.example.facefit.domain.usecases.cart.GetCartItemCountUseCase
import com.example.facefit.domain.usecases.cart.GetCartUseCase
import com.example.facefit.domain.usecases.cart.RemoveCartItemUseCase
import com.example.facefit.domain.usecases.cart.UpdateCartItemUseCase
import com.example.facefit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val getCartUseCase: GetCartUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val updateCartItemUseCase: UpdateCartItemUseCase,
    private val removeCartItemUseCase: RemoveCartItemUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val getCartItemCountUseCase: GetCartItemCountUseCase
) : ViewModel() {

    private val _cartState = MutableStateFlow<Resource<List<CartItem>>>(Resource.Loading())
    val cartState: StateFlow<Resource<List<CartItem>>> = _cartState.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _itemCount = MutableStateFlow<Resource<Int>>(Resource.Loading())
    val itemCount: StateFlow<Resource<Int>> = _itemCount.asStateFlow()

    private var _selectedColor by mutableStateOf("")
    val selectedColor: String get() = _selectedColor

    private var _totalAmount = MutableStateFlow(0.0)
    val totalAmount: StateFlow<Double> = _totalAmount.asStateFlow()

    init {
        loadCart()
        getItemCount()
    }

    fun setSelectedColor(color: String) {
        _selectedColor = color
    }

    fun loadCart() {
        viewModelScope.launch(Dispatchers.IO) {
            _cartState.value = Resource.Loading()
            when (val result = getCartUseCase()) {
                is Resource.Success -> {
                    result.data?.let { cartData ->
                        _totalAmount.value = cartData.totalAmount
                        _cartItems.value = cartData.items // Update local items
                        _cartState.value = Resource.Success(cartData.items)
                    } ?: run {
                        _cartState.value = Resource.Error("Cart data is null")
                    }
                }

                is Resource.Error -> {
                    _cartState.value = Resource.Error(result.message ?: "Error loading cart")
                }

                else -> Unit
            }
        }
    }

    fun addToCart(
        glassesId: String,
        color: String,
        lensType: String,
        size: String = "standard",
        lensSpecification: String? = null,
        prescriptionId: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val request = AddToCartRequest(
                glassesId = glassesId,
                color = color,
                size = size,
                lenseType = lensType,
                lensSpecification = lensSpecification,
                lensPrice = when (lensSpecification) {
                    "Standard Eyeglass Lenses" -> 50.0
                    "Blue Light Blocking" -> 75.0
                    "Driving Lenses" -> 100.0
                    else -> 0.0
                },
                prescriptionId = prescriptionId
            )

            when (val result = addToCartUseCase(request)) {
                is Resource.Success -> loadCart()
                is Resource.Error -> _cartState.value = Resource.Error(result.message ?: "Error")
                else -> Unit
            }
            getItemCount()
        }
    }


    fun updateCartItem(
        itemId: String,
        quantity: Int
    ) {
        val updatedItems = _cartItems.value.map { item ->
            if (item.id == itemId) {
                item.copy(quantity = quantity)
            } else {
                item
            }
        }
        _cartItems.value = updatedItems
        _totalAmount.value = updatedItems.sumOf {
            (it.glasses.price + it.lensPrice) * it.quantity
        }

        viewModelScope.launch(Dispatchers.IO) {
            val cartItem = _cartItems.value.find { it.id == itemId }
            val prescriptionId = cartItem?.prescription?._id ?: "0"

            updateCartItemUseCase(itemId, quantity, prescriptionId).also { result ->
                if (result is Resource.Error) {
                    loadCart()
                }
            }
        }
    }

    fun removeCartItem(itemId: String) {
        val updatedItems = _cartItems.value.filter { it.id != itemId }
        _cartItems.value = updatedItems
        _totalAmount.value = updatedItems.sumOf {
            (it.glasses.price + it.lensPrice) * it.quantity
        }

        viewModelScope.launch(Dispatchers.IO) {
            when (val result = removeCartItemUseCase(itemId)) {
                is Resource.Success -> {
                    loadCart()
                }

                is Resource.Error -> {
                    loadCart()
                    _cartState.value = Resource.Error(result.message ?: "Error")
                }

                else -> Unit
            }
            getItemCount()
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _totalAmount.value = 0.0

        viewModelScope.launch(Dispatchers.IO) {
            when (val result = clearCartUseCase()) {
                is Resource.Success -> {
                    loadCart()
                }

                is Resource.Error -> {
                    loadCart()
                    _cartState.value = Resource.Error(result.message ?: "Error")
                }

                else -> Unit
            }
            getItemCount()
        }
    }

    private fun getItemCount() {
        viewModelScope.launch(Dispatchers.IO) {
            _itemCount.value = when (val result = getCartItemCountUseCase()) {
                is Resource.Success -> Resource.Success(result.data ?: 0)
                is Resource.Error -> Resource.Error(result.message ?: "Error")
                is Resource.Loading -> Resource.Loading()
            }
        }
    }
}