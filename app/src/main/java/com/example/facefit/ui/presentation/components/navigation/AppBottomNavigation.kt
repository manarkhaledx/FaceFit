
package com.example.facefit.ui.presentation.components.navigation

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.facefit.R
import com.example.facefit.ui.presentation.screens.cart.ShoppingCartActivity
import com.example.facefit.ui.presentation.screens.favourites.FavouritesActivity
import com.example.facefit.ui.presentation.screens.home.HomePageActivity
import com.example.facefit.ui.presentation.screens.products.AllProductsActivity
import com.example.facefit.ui.presentation.screens.profile.ProfileActivity
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.Gray600
import com.example.facefit.ui.theme.LavenderBlue

@Composable
fun AppBottomNavigation() {
    val context = LocalContext.current
    val selectedItem = when (context::class.java) {
        HomePageActivity::class.java -> 0
        AllProductsActivity::class.java -> 2
        FavouritesActivity::class.java -> 1
        ShoppingCartActivity::class.java-> 3
        ProfileActivity::class.java -> 4
        else -> 0
    }


    val items = listOf(
        NavigationItem("Home", if (selectedItem == 0)  R.drawable.home else R.drawable.home_empty),
        NavigationItem("Favourites", if (selectedItem == 1) R.drawable.heart_filled else R.drawable.heart),
        NavigationItem("Products", if (selectedItem == 2) R.drawable.fill_glasses else R.drawable.glass_icon),
        NavigationItem("Cart", if (selectedItem == 3) R.drawable.cart_fill else R.drawable.cart),
        NavigationItem("Profile", if (selectedItem == 4) R.drawable.profile_fill else R.drawable.profile)
    )

    BottomNavigation(
        backgroundColor = Color.White,
        modifier = Modifier.height(80.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                BottomNavigationItem(
                    icon = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (selectedItem == index) LavenderBlue else Color.Transparent
                                    )
                                    .padding(vertical = 4.dp, horizontal = 12.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = item.iconResId),
                                    contentDescription = item.title,
                                    tint = if (selectedItem == index) Blue1 else Gray600,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Text(
                                text = item.title,
                                fontSize = 10.sp,
                                color = if (selectedItem == index) Blue1 else Gray600
                            )
                        }
                    },
                    selected = selectedItem == index,
                    onClick = {
                        if (selectedItem != index) {
                            when (item.title) {
                                "Home" -> context.startActivity(
                                    Intent(context, HomePageActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                                "Products" -> context.startActivity(
                                    Intent(context, AllProductsActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                                "Favourites" -> context.startActivity(
                                    Intent(context, FavouritesActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                                "Cart" -> context.startActivity(
                                    Intent(context, ShoppingCartActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                                "Profile" -> context.startActivity(
                                    Intent(context, ProfileActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

