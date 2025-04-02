package com.example.facefit.ui.presentation.screens.products

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.facefit.R
import com.example.facefit.ui.presentation.components.navigation.AppBottomNavigation
import com.example.facefit.ui.presentation.screens.filter.FilterScreenOverlay
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.FaceFitTheme

class AllProductsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var showFilter by remember { mutableStateOf(false) }

            FaceFitTheme {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)) {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .blur(if (showFilter) 3.dp else 0.dp)) {
                        AllProducts(
                            onClick = {
                                startActivity(
                                    Intent(
                                        this@AllProductsActivity,
                                        ProductDetailsActivity::class.java
                                    )
                                )
                            },
                            onFilterClick = { showFilter = true }
                        )
                    }
                    if (showFilter) {
                        FilterScreenOverlay(onDismiss = { showFilter = false })
                    }
                }
            }
        }
    }
}

@Composable
fun AllProducts(onClick: () -> Unit = {}, onFilterClick: () -> Unit = {}) {
    Scaffold(bottomBar = { AppBottomNavigation() }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            TopBar(onFilterClick = onFilterClick)
            Spacer(modifier = Modifier.height(16.dp))
            FilterTabs()
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(6) { GlassesItem(onClick = onClick) }
            }
        }
    }
}

@Composable
fun TopBar(onFilterClick: () -> Unit = {}) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("Default") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {


        TextButton(onClick = { expanded = true }, modifier = Modifier.weight(1f)) {
            Text(selectedOption, color = Blue1)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.ArrowDropDown, "dropdown", tint = Blue1)
DropdownMenu(
    expanded = expanded,
    onDismissRequest = { expanded = false },
    modifier = Modifier.background(Color.White)
) {
    listOf("Default", "Best Sellers", "New Arrivals").forEach { option ->
        DropdownMenuItem(
            text = { Text(option) },
            onClick = {
                selectedOption = option
                expanded = false
            }
        )
    }
}
        }
        TextButton(onClick = { onFilterClick() }, modifier = Modifier.weight(1f)) {
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
        val buttonColors =
            @Composable { isSelected: Boolean -> ButtonDefaults.buttonColors(containerColor = if (isSelected) Blue1 else Color.White) }
        val buttonTextColor = { isSelected: Boolean -> if (isSelected) Color.White else Blue1 }

        listOf("All", "Eyeglasses", "Sunglasses").forEachIndexed { index, text ->
            Button(
                onClick = { selectedTab = index },
                modifier = buttonModifier,
                colors = buttonColors(selectedTab == index)
            ) {
                Text(
                    text,
                    modifier = Modifier.fillMaxSize(),
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = buttonTextColor(selectedTab == index)
                )
            }
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
            Column(modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)) {
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
                    "Browline Glasses",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text("EGP 120", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    listOf(
                        Color.Yellow,
                        Color.Blue,
                        Color.Green,
                        Color.Black
                    ).forEach { ColorOption(it) }
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
                painter = painterResource(if (isFavorite) R.drawable.heart_filled else R.drawable.heart),
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
    FaceFitTheme { AllProducts() }
}