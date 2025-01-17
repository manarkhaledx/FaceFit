package com.example.facefit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.facefit.ui.theme.FaceFitTheme

class FavouritesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceFitTheme {
                FavouritesScreen()
            }
        }
    }
}

@Composable
fun FavouriteItem() {
    var isFavorite by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier
        .fillMaxWidth() // Make the card take full width
        .padding(horizontal = 16.dp, vertical = 8.dp), // Add 16dp padding on sides, 8dp vertical padding
    shape = RoundedCornerShape(8.dp),
    elevation = 4.dp // Set elevation to create shadow
) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color(0xFFFAFBFC),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp), // Inner padding for content
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.eyeglasses_wear), // Replace with your actual image resource
                contentDescription = "Glasses",
                modifier = Modifier
                    .size(width = 166.dp, height = 59.dp)
                    .padding(end = 16.dp),
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(), // Ensure column aligns vertically
                verticalArrangement = Arrangement.Center // Center content vertically
            ) {
Text(
    text = "Browline Glasses",
    fontSize = 14.sp,
    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
    color = Color.Black,
    modifier = Modifier.padding(bottom = 4.dp)
)
Text(
    text = "EGP 120",
    fontSize = 14.sp,
    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
    color = Color.Black,
    modifier = Modifier.padding(top = 4.dp)
)
            }

            IconButton(
                onClick = { isFavorite = !isFavorite },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isFavorite) R.drawable.heart_filled else R.drawable.heart
                    ),
                    contentDescription = if (isFavorite) "Unmark Favorite" else "Mark Favorite",
                    tint = Color.Blue
                )
            }
        }
    }
}

@Composable
fun FavouritesScreen() {
    Scaffold(
        bottomBar = { AppBottomNavigation() }
    ) { innerPadding ->
        // Ensure the LazyColumn uses the full available space
        LazyColumn(
            modifier = Modifier
                .fillMaxSize() // Fill the entire screen
                .padding(innerPadding) // Respect Scaffold's padding values
        ) {
            items(10) { // Replace `10` with the size of your data list
                FavouriteItem()
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun PreviewFavouritesScreen() {
    FaceFitTheme {
        FavouritesScreen()
    }
}
