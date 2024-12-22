package com.example.facefit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.ui.theme.FaceFitTheme

class AllProductsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceFitTheme {
                AllProducts(onClick = {
                    val intent = Intent(this, ProductDetailsActivity::class.java)
                    startActivity(intent)
                })
            }
        }
    }
}

@Composable
fun AllProducts(onClick: () -> Unit = {}) {
    Scaffold(
        bottomBar = { AppBottomNavigation() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            TopBar()
            Spacer(modifier = Modifier.height(16.dp))
            FilterTabs()
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(6) {
                    GlassesItem(onClick = onClick)
                }
            }
        }
    }
}

@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val buttonModifier = Modifier
            .weight(1f)
            .fillMaxWidth()

        TextButton(
            onClick = {},
            modifier = buttonModifier
        ) {
            Text("Default", color = Blue1)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.ArrowDropDown, "dropdown", tint = Blue1)
        }

        TextButton(
            onClick = {},
            modifier = buttonModifier
        ) {
            Text("Filter", color = Color.Black)
            Spacer(modifier = Modifier.width(4.dp))
            Image(
                painter = painterResource(id = R.drawable.filter),
                contentDescription = "Filter Icon"
            )
        }
    }
}

@Composable
fun FilterTabs() {
    var selectedTab by remember { mutableIntStateOf(0) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val buttonModifier = Modifier
            .weight(1f)
            .height(34.dp)

        Button(
            onClick = { selectedTab = 0 },
            modifier = buttonModifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedTab == 0) Blue1 else Color.White
            )
        ) {
            Text(
                text = "All",
                modifier = Modifier.fillMaxSize(),
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = if (selectedTab == 0) Color.White else Blue1
            )
        }

        Button(
            onClick = { selectedTab = 1 },
            modifier = buttonModifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedTab == 1) Blue1 else Color.White
            )
        ) {
            Text(
                text = "Eyeglasses",
                modifier = Modifier.fillMaxSize(),
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = if (selectedTab == 1) Color.White else Blue1
            )
        }

        Button(
            onClick = { selectedTab = 2 },
            modifier = buttonModifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedTab == 2) Blue1 else Color.White
            )
        ) {
            Text(
                text = "Sunglasses",
                modifier = Modifier.fillMaxSize(),
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = if (selectedTab == 2) Color.White else Blue1
            )
        }
    }
}

@Composable
fun GlassesItem(onClick: () -> Unit) {
    var isFavorite by remember { mutableStateOf(false) }

    Box(modifier = Modifier.clickable { onClick() }) {
        Card(
            modifier = Modifier.width(180.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.eye_glasses),
                        contentDescription = "Glasses",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {},
                    modifier = Modifier
                        .width(72.dp)
                        .height(30.dp)
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Blue1
                    ),
                    border = BorderStroke(1.dp, Blue1),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text(
                        "Try ON",
                        fontSize = 12.sp,
                        color = Blue1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Browline Glasses",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )

                Text(
                    text = "EGP 120",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ColorOption(Color.Yellow)
                    ColorOption(Color.Blue)
                    ColorOption(Color.Green)
                    ColorOption(Color.Black)
                }
            }
        }

        IconButton(
            onClick = { isFavorite = !isFavorite },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ) {
            Icon(
                painter = painterResource(
                    if (isFavorite) R.drawable.heart_filled else R.drawable.heart
                ),
                contentDescription = if (isFavorite) "Unmark Favorite" else "Mark Favorite",
                tint = Color.Blue
            )
        }
    }
}

@Composable
fun ColorOption(color: Color) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .background(color, CircleShape)
            .border(1.dp, Color.LightGray, CircleShape)
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FaceFitTheme {
        AllProducts()
    }
}
