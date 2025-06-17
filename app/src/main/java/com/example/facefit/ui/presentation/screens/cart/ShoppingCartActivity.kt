package com.example.facefit.ui.presentation.screens.cart

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.facefit.R
import com.example.facefit.domain.models.CartItem
import com.example.facefit.domain.utils.Resource
import com.example.facefit.ui.presentation.components.ErrorScreen
import com.example.facefit.ui.presentation.components.PullToRefreshContainer
import com.example.facefit.ui.presentation.components.navigation.AppBottomNavigation
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.FaceFitTheme
import com.example.facefit.ui.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ShoppingCartActivity : ComponentActivity() {
    private val cartViewModel: CartViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceFitTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    ShoppingCartScreen(
                        viewModel = cartViewModel,
                        onBackClick = { finish() },
                        modifier = Modifier.weight(1f)
                    )
                    AppBottomNavigation()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cartViewModel.loadCart()
    }
}

@Preview
@Composable
fun ShoppingCartScreenPreview() {
    ShoppingCartScreen(
        onBackClick = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingCartScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val cartState by viewModel.cartState.collectAsState()
    val itemCount by viewModel.itemCount.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()
    val deletingItemId by viewModel.deletingItemId.collectAsState()
    val updatingItemId by viewModel.updatingItemId.collectAsState()
    var isDeleteMode by remember { mutableStateOf(false) }
    var showDeleteAllConfirmation by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isRefreshing = cartState is Resource.Loading

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopAppBar(
            title = {
                Text(
                    "Shopping Cart",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            actions = {
                if (cartState is Resource.Success && (cartState as Resource.Success).data?.isNotEmpty() == true) {
                    TextButton(
                        onClick = { isDeleteMode = !isDeleteMode },
                        enabled = deletingItemId == null && updatingItemId == null
                    ) {
                        Text(if (isDeleteMode) "Done" else "Edit", color = Color.Black)
                    }
                }
            }
        )

        PullToRefreshContainer(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadCart() },
            modifier = Modifier.weight(1f)
        ) {
            when (cartState) {
                is Resource.Loading -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(3) {
                            LoadingCartItemShimmer()
                        }
                    }
                }

                is Resource.Error -> {
                    val message = (cartState as Resource.Error).message ?: "Unknown error"
                    val isNetworkError = message.contains(
                        "internet connection",
                        ignoreCase = true
                    ) || message.contains(
                        "network error",
                        ignoreCase = true
                    ) || message.contains("timeout", ignoreCase = true)
                    ErrorScreen(
                        modifier = Modifier.fillMaxSize(),
                        title = if (isNetworkError) "No Internet Connection" else "Error Loading Cart",
                        message = if (isNetworkError) "Please check your connection and try again." else message,
                        imageResId = if (isNetworkError) R.drawable.no_int else R.drawable.error
                    )
                }

                is Resource.Success<*> -> {
                    val items = (cartState as Resource.Success<List<CartItem>>).data ?: emptyList()
                    if (items.isEmpty()) {
                        EmptyCartScreen(
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(items) { cartItem ->
                                val imageModel = if (cartItem.glasses.images.isNotEmpty()) {
                                    "${Constants.EMULATOR_URL}/${cartItem.glasses.images.first()}"
                                } else {
                                    R.drawable.placeholder
                                }

                                val unitPrice = cartItem.glasses.price + cartItem.lensPrice

                                CartItem(
                                    item = CartItemUI(
                                        id = cartItem.id,
                                        name = cartItem.glasses.name,
                                        color = cartItem.color,
                                        visionType = cartItem.lensSpecification,
                                        unitPrice = unitPrice,
                                        quantity = cartItem.quantity,
                                        imageModel = imageModel
                                    ),
                                    isDeleteMode = isDeleteMode,
                                    isDeleting = deletingItemId == cartItem.id,
                                    isUpdating = updatingItemId == cartItem.id,
                                    isAnyItemLoading = deletingItemId != null || updatingItemId != null,
                                    onQuantityChange = { itemId, newQuantity ->
                                        viewModel.updateCartItem(
                                            itemId = itemId,
                                            quantity = newQuantity
                                        )
                                    },
                                    onDeleteItem = { itemId ->
                                        viewModel.removeCartItem(itemId) { success ->
                                            if (success) {
                                                coroutineScope.launch(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        context,
                                                        "Product deleted successfully",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (cartState is Resource.Success && (cartState as Resource.Success).data?.isNotEmpty() == true || cartState is Resource.Loading) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (cartState is Resource.Loading || deletingItemId != null || updatingItemId != null) {
                        LoadingShimmerEffect(
                            modifier = Modifier
                                .width(100.dp)
                                .height(24.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                    } else {
                        Text(
                            "EGP ${String.format("%.2f", totalAmount)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isDeleteMode) {
                            OutlinedButton(
                                onClick = { showDeleteAllConfirmation = true },
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = SolidColor(Color.Red)
                                ),
                                shape = RoundedCornerShape(24.dp),
                                contentPadding = PaddingValues(
                                    horizontal = 24.dp,
                                    vertical = 12.dp
                                ),
                                enabled = deletingItemId == null && updatingItemId == null // Disable "Delete All" if an item is being deleted or updated
                            ) {
                                Text("Delete All", color = Color.Red)
                            }
                            if (showDeleteAllConfirmation) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteAllConfirmation = false },
                                    title = { Text("Delete All Items") },
                                    text = { Text("Are you sure you want to remove all items from your cart?") },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                viewModel.clearCart()
                                                isDeleteMode = false
                                                showDeleteAllConfirmation = false
                                            }
                                        ) {
                                            Text("Delete", color = Color.Red)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = { showDeleteAllConfirmation = false }
                                        ) {
                                            Text("Cancel")
                                        }
                                    }
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    val intent = Intent(context, CheckoutActivity::class.java)
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Blue1),
                                shape = RoundedCornerShape(24.dp),
                                contentPadding = PaddingValues(
                                    horizontal = 24.dp,
                                    vertical = 12.dp
                                ),
                                enabled = cartState is Resource.Success && (cartState as Resource.Success).data?.isNotEmpty() == true && deletingItemId == null && updatingItemId == null // Disable checkout if an item is being deleted or updated
                            ) {
                                if (cartState is Resource.Loading || deletingItemId != null || updatingItemId != null) { // Show loading if overall cart is loading or any item is being deleted/updated
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Checkout", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun Modifier.shimmerEffect(shape: Shape): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                delayMillis = 200
            ),
            repeatMode = RepeatMode.Reverse
        ), label = "shimmerTranslate"
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.LightGray.copy(alpha = 0.6f),
                Color.LightGray.copy(alpha = 0.2f),
                Color.LightGray.copy(alpha = 0.6f),
            ),
            start = Offset.Zero,
            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
        ),
        shape = shape
    )
}


@Composable
fun CartItem(
    item: CartItemUI,
    isDeleteMode: Boolean,
    isDeleting: Boolean,
    isUpdating: Boolean,
    isAnyItemLoading: Boolean,
    onQuantityChange: (String, Int) -> Unit,
    onDeleteItem: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 8.dp),
                contentAlignment = Alignment.Center
            ) {

                if (isDeleting || isUpdating) {
                    LoadingShimmerEffect(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    when (val model = item.imageModel) {
                        is String -> {
                            AsyncImage(
                                model = model,
                                contentDescription = item.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                error = painterResource(R.drawable.placeholder),
                                placeholder = painterResource(R.drawable.placeholder)
                            )
                        }

                        is Int -> {
                            Image(
                                painter = painterResource(id = model),
                                contentDescription = item.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                if (isDeleting || isUpdating) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LoadingShimmerEffect(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(16.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                        LoadingShimmerEffect(
                            modifier = Modifier
                                .width(80.dp)
                                .height(12.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                        LoadingShimmerEffect(
                            modifier = Modifier
                                .width(100.dp)
                                .height(12.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                        LoadingShimmerEffect(
                            modifier = Modifier
                                .width(60.dp)
                                .height(18.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                    }
                } else {
                    Text(
                        text = item.name,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight(500),
                            color = Color(0xFF151616),
                            letterSpacing = 0.8.sp,
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(110.dp)
                            .height(IntrinsicSize.Min)
                            .background(
                                color = Color(0xFFEDEFF7),
                                shape = RoundedCornerShape(size = 4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Color: ${item.color}",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF4D5159),
                                letterSpacing = 0.6.sp,
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(130.dp)
                            .height(IntrinsicSize.Min)
                            .background(
                                color = Color(0xFFEDEFF7),
                                shape = RoundedCornerShape(size = 4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            item.visionType,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF4D5159),
                                letterSpacing = 0.6.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "EGP ${String.format("%.2f", item.unitPrice * item.quantity)}",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight(700),
                            color = Color(0xFFEB0404),
                            letterSpacing = 0.9.sp,
                        )
                    )
                }
            }

            if (isDeleteMode) {
                IconButton(
                    onClick = { onDeleteItem(item.id) },
                    enabled = !isAnyItemLoading // Disable button if any item is deleting or updating
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.trash),
                        tint = if (isAnyItemLoading) Color.Gray else Color.Unspecified, // Gray out if disabled
                        contentDescription = "Delete",
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp)
                            .background(
                                color = if (isAnyItemLoading) Color(0x1A808080) else Color(
                                    0x1AD20000
                                ), // Change background color if disabled
                                shape = RoundedCornerShape(size = 8.dp)
                            )
                            .padding(4.dp)
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .background(
                            color = Color(0xFFF5F6F7),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    val isQuantityChangeDisabled = isAnyItemLoading || isUpdating

                    Text(
                        text = "−",
                        modifier = Modifier
                            .clickable(enabled = !isQuantityChangeDisabled && item.quantity > 1) {
                                onQuantityChange(item.id, item.quantity - 1)
                            }
                            .padding(horizontal = 4.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isQuantityChangeDisabled || item.quantity <= 1) Color.Gray else Color.Black
                    )

                    if (isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Blue1,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "${item.quantity}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isQuantityChangeDisabled) Color.Gray else Color.Black
                        )
                    }

                    Text(
                        text = "+",
                        modifier = Modifier
                            .clickable(enabled = !isQuantityChangeDisabled) {
                                onQuantityChange(item.id, item.quantity + 1)
                            }
                            .padding(horizontal = 4.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isQuantityChangeDisabled) Color.Gray else Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingCartItemShimmer() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LoadingShimmerEffect(
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 8.dp),
                shape = RoundedCornerShape(8.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LoadingShimmerEffect(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(20.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                LoadingShimmerEffect(
                    modifier = Modifier
                        .width(110.dp)
                        .height(20.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                LoadingShimmerEffect(
                    modifier = Modifier
                        .width(130.dp)
                        .height(20.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                LoadingShimmerEffect(
                    modifier = Modifier
                        .width(80.dp)
                        .height(24.dp),
                    shape = RoundedCornerShape(4.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LoadingShimmerEffect(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(horizontal = 4.dp),
                    shape = CircleShape
                )
                LoadingShimmerEffect(
                    modifier = Modifier
                        .width(32.dp)
                        .height(24.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                LoadingShimmerEffect(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(horizontal = 4.dp),
                    shape = CircleShape
                )
            }
        }
    }
}

@Composable
fun EmptyCartScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.empty_shopping_cart),
            contentDescription = "Empty Cart Illustration",
            modifier = Modifier.size(200.dp),
            alpha = 0.6f
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your Cart is Empty!",
            style = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Looks like you haven't added anything to your cart yet. Browse our collection and find your perfect pair.",
            style = TextStyle(
                fontSize = 16.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Normal
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }
}

data class CartItemUI(
    val id: String,
    val name: String = "Unknown Glasses",
    val color: String = "Unknown Color",
    val visionType: String = "Unknown Type",
    val unitPrice: Double, // Changed from 'price' to 'unitPrice' to represent the price of a single item + its lens
    val quantity: Int,
    val imageModel: Any?
)
