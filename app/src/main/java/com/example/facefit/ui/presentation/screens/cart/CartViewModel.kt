// CartViewModel.kt
package com.example.facefit.ui.presentation.screens.cart

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.facefit.data.models.requests.AddToCartRequest
import com.example.facefit.domain.models.CartData
import com.example.facefit.domain.models.CartItem
import com.example.facefit.domain.usecases.cart.AddToCartUseCase
import com.example.facefit.domain.usecases.cart.ClearCartUseCase
import com.example.facefit.domain.usecases.cart.GetCartItemCountUseCase
import com.example.facefit.domain.usecases.cart.GetCartUseCase
import com.example.facefit.domain.usecases.cart.RemoveCartItemUseCase
import com.example.facefit.domain.usecases.cart.UpdateCartItemUseCase
import com.example.facefit.domain.utils.NetworkUtils
import com.example.facefit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
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
class CartViewModel @Inject constructor(
    private val getCartUseCase: GetCartUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val updateCartItemUseCase: UpdateCartItemUseCase,
    private val removeCartItemUseCase: RemoveCartItemUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val getCartItemCountUseCase: GetCartItemCountUseCase,
    @ApplicationContext private val context: Context // Inject Context
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

    // New state to track which item is being deleted
    private val _deletingItemId = MutableStateFlow<String?>(null)
    val deletingItemId: StateFlow<String?> = _deletingItemId.asStateFlow()

    init {
        loadCart()
        getItemCount()
    }

    fun setSelectedColor(color: String) {
        _selectedColor = color
    }

    fun loadCart() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _cartState.value = Resource.Error("Please check your internet connection.", emptyList())
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _cartState.value = Resource.Loading()
            try {
                when (val result = getCartUseCase()) {
                    is Resource.Success -> {
                        result.data?.let { cartData ->
                            _totalAmount.value = cartData.totalAmount
                            _cartItems.value = cartData.items
                            _cartState.value = Resource.Success(cartData.items)
                        } ?: run {
                            Log.e("CartViewModel", "loadCart: Cart data is null for successful response.")
                            _cartState.value = Resource.Error("Something went wrong.", emptyList())
                        }
                    }

                    is Resource.Error -> {
                        handleGenericError(result.message, Resource.Error("", emptyList()), _cartState)
                    }

                    else -> Unit
                }
            } catch (e: Exception) {
                handleGenericError(e.message, Resource.Error("", emptyList()), _cartState)
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
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _cartState.value = Resource.Error("Please check your internet connection.", emptyList())
            return
        }

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

            try {
                when (val result = addToCartUseCase(request)) {
                    is Resource.Success -> loadCart()
                    is Resource.Error -> {
                        handleGenericError(result.message, Resource.Error("", emptyList()), _cartState)
                    }
                    else -> Unit
                }
                getItemCount()
            } catch (e: Exception) {
                handleGenericError(e.message, Resource.Error("", emptyList()), _cartState)
                getItemCount()
            }
        }
    }

    fun updateCartItem(
        itemId: String,
        quantity: Int
    ) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _cartState.value = Resource.Error("Please check your internet connection.", _cartItems.value)
            return
        }

        // Optimistic update
        val oldQuantity = _cartItems.value.find { it.id == itemId }?.quantity ?: 0
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
            val prescriptionId = cartItem?.prescription?._id

            try {
                when (val result = updateCartItemUseCase(itemId, quantity, prescriptionId)) {
                    is Resource.Error -> {
                        loadCart() // Revert to server state on error
                        handleGenericError(result.message, Resource.Error("", emptyList()), _cartState)
                    }
                    else -> Unit // Success handled by optimistic update for now, or you can reload for consistency
                }
            } catch (e: Exception) {
                loadCart() // Revert to server state on exception
                handleGenericError(e.message, Resource.Error("", emptyList()), _cartState)
            }
        }
    }

    fun removeCartItem(itemId: String, onComplete: (Boolean) -> Unit) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            onComplete(false)
            _cartState.value = Resource.Error("Please check your internet connection.", _cartItems.value)
            return
        }

        _deletingItemId.value = itemId // Set the ID of the item being deleted

        viewModelScope.launch(Dispatchers.IO) {
            try {
                when (val result = removeCartItemUseCase(itemId)) {
                    is Resource.Success -> {
                        loadCart() // Reload cart to reflect deletion and update UI
                        onComplete(true)
                    }
                    is Resource.Error -> {
                        loadCart() // Reload cart to revert if deletion failed
                        handleGenericError(result.message, Resource.Error("", emptyList()), _cartState)
                        onComplete(false)
                    }
                    else -> onComplete(false)
                }
                getItemCount()
            } catch (e: Exception) {
                loadCart() // Reload cart to revert on exception
                handleGenericError(e.message, Resource.Error("", emptyList()), _cartState)
                getItemCount()
                onComplete(false)
            } finally {
                _deletingItemId.value = null // Clear the deleting ID after completion (success or failure)
            }
        }
    }

    fun clearCart() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _cartState.value = Resource.Error("Please check your internet connection.", _cartItems.value)
            return
        }

        // Optimistic update
        _cartItems.value = emptyList()
        _totalAmount.value = 0.0

        viewModelScope.launch(Dispatchers.IO) {
            try {
                when (val result = clearCartUseCase()) {
                    is Resource.Success -> {
                        loadCart() // Reload to confirm
                    }
                    is Resource.Error -> {
                        loadCart() // Revert if failed
                        handleGenericError(result.message, Resource.Error("", emptyList()), _cartState)
                    }
                    else -> Unit
                }
                getItemCount()
            } catch (e: Exception) {
                loadCart() // Revert on exception
                handleGenericError(e.message, Resource.Error("", emptyList()), _cartState)
                getItemCount()
            }
        }
    }

    private fun getItemCount() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _itemCount.value = Resource.Error("No internet.", 0)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _itemCount.value = when (val result = getCartItemCountUseCase()) {
                    is Resource.Success -> Resource.Success(result.data ?: 0)
                    is Resource.Error -> {
                        Log.e("CartViewModel", "Error getting item count: ${result.message}")
                        Resource.Error(result.message ?: "Error getting item count", 0)
                    }
                    is Resource.Loading -> Resource.Loading()
                }
            } catch (e: Exception) {
                Log.e("CartViewModel", "Exception getting item count: ${e.message}", e)
                _itemCount.value = Resource.Error("Something went wrong.", 0)
            }
        }
    }

    // Generic error handler for ViewModel
    private fun <T> handleGenericError(errorMessage: String?, initialResource: Resource<T>, stateFlow: MutableStateFlow<Resource<T>>) {
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

        Log.e("CartViewModel", "API Call Error: $logMessage")
        stateFlow.value = Resource.Error(userFriendlyMessage, initialResource.data) // Preserve data if available, else null
    }
}