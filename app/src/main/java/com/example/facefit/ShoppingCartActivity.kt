package com.example.facefit

    import android.os.Bundle
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.activity.enableEdgeToEdge
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.background
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.PaddingValues
    import androidx.compose.foundation.layout.Row
    import androidx.compose.foundation.layout.Spacer
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.size
    import androidx.compose.foundation.layout.width
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.items
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.automirrored.filled.ArrowBack
    import androidx.compose.material.icons.filled.KeyboardArrowDown
    import androidx.compose.material3.Button
    import androidx.compose.material3.ButtonDefaults
    import androidx.compose.material3.Card
    import androidx.compose.material3.CardDefaults
    import androidx.compose.material3.Checkbox
    import androidx.compose.material3.DropdownMenu
    import androidx.compose.material3.DropdownMenuItem
    import androidx.compose.material3.ExperimentalMaterial3Api
    import androidx.compose.material3.Icon
    import androidx.compose.material3.IconButton
    import androidx.compose.material3.Surface
    import androidx.compose.material3.Text
    import androidx.compose.material3.TextButton
    import androidx.compose.material3.TopAppBar
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.tooling.preview.Preview
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import com.example.facefit.ui.theme.FaceFitTheme

class ShoppingCartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceFitTheme {
                ShoppingCartScreen(
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
    onBackClick: () -> Unit
) {
    var isDeleteMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf(setOf<String>()) }
    var cartItems by remember { mutableStateOf(sampleCartItems.toMutableList()) }

    Column(modifier = modifier.fillMaxSize().background(Color.White)) {
        TopAppBar(
            title = { Text("Shopping Cart", fontSize = 18.sp) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                TextButton(onClick = { isDeleteMode = !isDeleteMode }) {
                    Text("Edit", color = Color.Black)
                }
            }
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cartItems) { item ->
                CartItem(
                    item = item,
                    isSelected = selectedItems.contains(item.id),
                    onSelectedChange = { selected ->
                        selectedItems = if (selected) {
                            selectedItems + item.id
                        } else {
                            selectedItems - item.id
                        }
                    },
                    onQuantityChange = { cartItem, newQuantity ->
                        cartItems = cartItems.map {
                            if (it.id == cartItem.id) it.copy(quantity = newQuantity.coerceAtLeast(1)) else it
                        }.toMutableList()
                    }
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = selectedItems.isNotEmpty(),
                        onCheckedChange = { checked ->
                            selectedItems = if (checked) {
                                cartItems.map { it.id }.toSet()
                            } else {
                                emptySet()
                            }
                        }
                    )
                    Text("All", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "EGP ${selectedItems.size * 150}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = { /* Checkout logic */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0000FF)
                    ),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text("Checkout", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun CartItem(
    item: CartItem,
    isSelected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    onQuantityChange: (CartItem, Int) -> Unit
) {
    var showColorDropdown by remember { mutableStateOf(false) }
    var showVisionDropdown by remember { mutableStateOf(false) }

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
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelectedChange(it) },
                modifier = Modifier.padding(end = 8.dp)
            )

            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(70.dp)
                    .padding(end = 12.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )

                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { showColorDropdown = true }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            "Color: ${item.color}",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(
                        expanded = showColorDropdown,
                        onDismissRequest = { showColorDropdown = false }
                    ) {
                        listOf("Black", "Brown", "Blue").forEach { color ->
                            DropdownMenuItem(
                                text = { Text(color) },
                                onClick = {
                                    // Handle color selection
                                    showColorDropdown = false
                                }
                            )
                        }
                    }
                }

                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { showVisionDropdown = true }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            item.visionType,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(
                        expanded = showVisionDropdown,
                        onDismissRequest = { showVisionDropdown = false }
                    ) {
                        listOf("Single Vision", "Progressive", "Bifocal").forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    // Handle vision type selection
                                    showVisionDropdown = false
                                }
                            )
                        }
                    }
                }

                Text(
                    text = "EGP ${item.price}",
                    fontSize = 16.sp,
                    color = Color.Red,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .background(
                        color = Color(0xFFF5F6F7),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "−",
                    modifier = Modifier
                        .clickable { onQuantityChange(item, item.quantity - 1) }
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
                        .clickable { onQuantityChange(item, item.quantity + 1) }
                        .padding(horizontal = 4.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}



data class CartItem(
    val id: String,
    val name: String,
    val color: String,
    val visionType: String,
    val price: Int,
    val quantity: Int,
    val imageRes: Int
)

// Sample data
val sampleCartItems = listOf(
    CartItem("1", "Browline glasses", "Black", "Single Vision", 150, 1, R.drawable.eye_glasses),
    CartItem("2", "Browline glasses", "Black", "Single Vision", 150, 1, R.drawable.eye_glasses),
    CartItem("3", "Browline glasses", "Black", "Single Vision", 150, 1, R.drawable.eye_glasses)
)













