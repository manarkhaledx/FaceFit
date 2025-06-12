package com.example.facefit.ui.presentation.screens.cart

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.example.facefit.domain.models.CartData
import com.example.facefit.domain.models.CartItem
import com.example.facefit.domain.models.CartItemWithGlasses
import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.utils.Resource
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.FaceFitTheme
import com.example.facefit.ui.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.max

@AndroidEntryPoint
class ShoppingCartActivity : ComponentActivity() {
    private val cartViewModel: CartViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        cartViewModel.loadCart()
        setContent {
            FaceFitTheme {
                ShoppingCartScreen(
                    viewModel = cartViewModel,
                    onBackClick = { finish() }
                )
            }
        }
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
    var isDeleteMode by remember { mutableStateOf(false) }
    var showDeleteAllConfirmation by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top AppBar
        TopAppBar(
            title = { Text("Shopping Cart", fontSize = 18.sp) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                TextButton(onClick = { isDeleteMode = !isDeleteMode }) {
                    Text(if (isDeleteMode) "Done" else "Edit", color = Color.Black)
                }
            }
        )

        // Cart Items List
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (cartState) {
                is Resource.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                is Resource.Error -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (cartState as Resource.Error).message ?: "Error loading cart",
                                color = Color.Red
                            )
                        }
                    }
                }

                is Resource.Success<*> -> {
                    val items = (cartState as Resource.Success<List<CartItem>>).data ?: emptyList()
                    items(cartItems) { cartItem ->
                        val imageModel = if (cartItem.glasses.images.isNotEmpty()) {
                            "${Constants.EMULATOR_URL}/${cartItem.glasses.images.first()}"
                        } else {
                            R.drawable.placeholder
                        }
                        val totalPrice = String.format("%.2f", (cartItem.glasses.price + cartItem.lensPrice) * cartItem.quantity).toDouble()

                        CartItem(
                            item = CartItemUI(
                                id = cartItem.id,
                                name = cartItem.glasses.name,
                                color = cartItem.color,
                                visionType = cartItem.lensSpecification,
                                price = totalPrice,
                                quantity = cartItem.quantity,
                                imageModel = imageModel
                            ),
                            isDeleteMode = isDeleteMode,
                            onQuantityChange = { itemId, newQuantity ->
                                viewModel.updateCartItem(
                                    itemId = itemId,
                                    quantity = newQuantity
                                )
                            },
                            onDeleteItem = { itemId ->
                                viewModel.removeCartItem(itemId)
                            }
                        )
                    }
                }
            }
        }

        // Bottom Bar
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
                if (!isDeleteMode) {
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
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
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
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text("Checkout", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItem(
    item: CartItemUI,
    isDeleteMode: Boolean,
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

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
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

                // Color display (without dropdown)
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

                // Vision type display (without dropdown)
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
                    text = "EGP ${item.price}",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight(700),
                        color = Color(0xFFEB0404),
                        letterSpacing = 0.9.sp,
                    )
                )
            }

            if (isDeleteMode) {
                IconButton(onClick = { onDeleteItem(item.id) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.trash),
                        tint = Color.Unspecified,
                        contentDescription = "Delete",
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp)
                            .background(
                                color = Color(0x1AD20000),
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
                    Text(
                        text = "−",
                        modifier = Modifier
                            .clickable {
                                val newQuantity = max(1, item.quantity - 1) // Ensure quantity doesn't go below 1
                                onQuantityChange(item.id, newQuantity)
                            }
                            .padding(horizontal = 4.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "${item.quantity}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "+",
                        modifier = Modifier
                            .clickable {
                                onQuantityChange(item.id, item.quantity + 1)
                            }
                            .padding(horizontal = 4.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

data class CartItemUI(
    val id: String,
    val name: String = "Unknown Glasses",
    val color: String = "Unknown Color",
    val visionType: String = "Unknown Type",
    val price: Double,
    val quantity: Int,
    val imageModel: Any?
)