package com.example.facefit.ui.presentation.screens.cart

import android.content.Intent
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.facefit.R
import com.example.facefit.ui.theme.Blue1
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
    val context = LocalContext.current // Get the current context
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
            items(cartItems) { item ->
                CartItem(
                    item = item,
                    isDeleteMode = isDeleteMode,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
Checkbox(
    checked = selectedItems.isNotEmpty(),
    onCheckedChange = { checked ->
        selectedItems = if (checked) {
            cartItems.map { it.id }.toSet()
        } else {
            emptySet()
        }
    },
    colors = CheckboxDefaults.colors(checkedColor = Blue1)
)
                    Text("All", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    if (!isDeleteMode) {
                        Text(
                            "EGP ${selectedItems.size * 150}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }


                }
                Row(verticalAlignment = Alignment.CenterVertically) { // Added this Row for better alignment
                    if (isDeleteMode) {
                        OutlinedButton(
                            onClick = { /* Add to favourites logic */ },
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = SolidColor(Color(0xFF0000FF))
                            ),
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text(
                                "Add to Favourites",
                                color = Color(0xFF0000FF)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp)) // Add space between buttons
                        OutlinedButton(
                            onClick = { /* Delete logic */ },
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = SolidColor(Color.Red)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text("Delete", color = Color.Red)
                        }
                    } else {
                        // Inside ShoppingCartScreen
                        Button(
                            onClick = {

                                // Start CheckoutActivity
                                val intent = Intent(context, CheckoutActivity::class.java)
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Blue1
                            ),
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
    item: CartItem,
    isDeleteMode: Boolean,
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
    colors = CheckboxDefaults.colors(checkedColor = Blue1),
    modifier = Modifier.padding(end = 8.dp)
)

            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 12.dp)
            )

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
//spacer
                 Spacer(modifier = Modifier.height(4.dp))
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(110.dp)
                            .height(IntrinsicSize.Min)
                            .background(color = Color(0xFFEDEFF7), shape = RoundedCornerShape(size = 4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clickable { showColorDropdown = true } // Trigger dropdown
                    ) {
                        Text(
                            "Color: ${item.color}",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF4D5159),
                                letterSpacing = 0.6.sp,
                            ),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showColorDropdown,
                        onDismissRequest = { showColorDropdown = false }
                    ) {
                        listOf("Black", "Brown", "Blue").forEach { color ->
                            DropdownMenuItem(
                                text = { Text(color) },
                                onClick = {
                                    // Handle color selection here (e.g., update item color)
                                    showColorDropdown = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(110.dp)
                            .height(IntrinsicSize.Min)
                            .background(color = Color(0xFFEDEFF7), shape = RoundedCornerShape(size = 4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clickable { showVisionDropdown = true } // Trigger dropdown
                    ) {
                        Text(
                            item.visionType,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF4D5159),
                                letterSpacing = 0.6.sp
                            ),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showVisionDropdown,
                        onDismissRequest = { showVisionDropdown = false }
                    ) {
                        listOf("Single Vision", "Progressive", "Bifocal").forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    // Handle vision type selection here (e.g., update item vision type)
                                    showVisionDropdown = false
                                }
                            )
                        }
                    }
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
                IconButton(onClick = { /* Handle delete */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.trash), tint = Color.Unspecified,
                        contentDescription = "Delete",
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp)
                            .background(
                                color = Color(0x1AD20000),
                                shape = RoundedCornerShape(size = 8.dp)
                            )
                            .padding(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 4.dp)
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













