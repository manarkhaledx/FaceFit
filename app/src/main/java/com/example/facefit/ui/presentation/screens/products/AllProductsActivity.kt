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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.facefit.R
import com.example.facefit.domain.models.Glasses
import com.example.facefit.ui.presentation.components.navigation.AppBottomNavigation
import com.example.facefit.ui.presentation.screens.filter.FilterScreenOverlay
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.FaceFitTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme

@AndroidEntryPoint
class AllProductsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var showFilter by remember { mutableStateOf(false) }
            val viewModel: AllProductsViewModel = hiltViewModel()
            val categoryFilter = intent.getStringExtra("CATEGORY_FILTER")

            LaunchedEffect(categoryFilter) {
                if (categoryFilter != null) {
                    viewModel.filterByCategory(categoryFilter)
                }
            }

            FaceFitTheme {
                Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .blur(if (showFilter) 3.dp else 0.dp)) {
                        AllProducts(
                            onClick = { productId ->
                                startActivity(
                                    Intent(
                                        this@AllProductsActivity,
                                        ProductDetailsActivity::class.java
                                    ).apply {
                                        putExtra("productId", productId)
                                    }
                                )
                            },
                            onFilterClick = { showFilter = true }
                        )
                    }
                    if (showFilter) {
                        FilterScreenOverlay(
                            onDismiss = { showFilter = false },
                            onApply = { gender, type, minPrice, maxPrice, shape, material ->
                                viewModel.filterProducts(
                                    gender = gender,
                                    type = type,
                                    minPrice = minPrice,
                                    maxPrice = maxPrice,
                                    shape = shape,
                                    material = material
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AllProducts(
    onClick: (String) -> Unit = {},
    onFilterClick: () -> Unit = {},
    viewModel: AllProductsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTab = uiState.selectedTab
    
    LaunchedEffect(Unit) {
        viewModel.loadAllProducts()
    }

    Scaffold(bottomBar = { AppBottomNavigation() }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            TopBar(
                selectedOption = uiState.selectedSort,
                onOptionSelected = { viewModel.sortProducts(it) },
                onFilterClick = onFilterClick
            )
            Spacer(modifier = Modifier.height(16.dp))
            FilterTabs(
                selectedTab = selectedTab,
                onTabSelected = { viewModel.filterByType(it) }
            )

            when {
                uiState.isLoading -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(createPlaceholderGlasses(6)) { glasses ->
                            GlassesItem(
                                glasses = glasses,
                                onClick = {},
                                onFavoriteClick = {},
                                isError = false
                            )
                        }
                    }
                }
                uiState.error != null -> {
                    Column {
                        Text(
                            text = uiState.error ?: stringResource(R.string.error_loading_products),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(createPlaceholderGlasses(6)) { glasses ->
                                GlassesItem(
                                    glasses = glasses,
                                    onClick = {},
                                    onFavoriteClick = {},
                                    isError = true
                                )
                            }
                        }
                    }
                }
                uiState.products.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.no_products_found),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.loadAllProducts() },
                            colors = ButtonDefaults.buttonColors(containerColor = Blue1)
                        ) {
                            Text(stringResource(R.string.clear_filters))
                        }
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.products) { glasses ->
                            GlassesItem(
                                glasses = glasses,
                                onClick = { onClick(glasses.id) },
                                onFavoriteClick = { viewModel.toggleFavorite(glasses.id) },
                                isError = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar(
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = { expanded = true },
            modifier = Modifier.weight(1f)
        ) {
            Text(selectedOption, color = Blue1)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.ArrowDropDown, stringResource(R.string.desc_dropdown), tint = Blue1)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            listOf(
                stringResource(R.string.default_sort),
                stringResource(R.string.best_sellers),
                stringResource(R.string.new_arrivals)
            ).forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }

        TextButton(
            onClick = { onFilterClick() },
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.filter), color = Color.Black)
            Spacer(modifier = Modifier.width(4.dp))
            Image(
                painter = painterResource(id = R.drawable.filter),
                contentDescription = stringResource(R.string.desc_filter_icon)
            )
        }
    }
}

@Composable
fun FilterTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
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

        listOf(
            stringResource(R.string.all),
            stringResource(R.string.eyeglasses),
            stringResource(R.string.sunglasses)
        ).forEachIndexed { index, text ->
            Button(
                onClick = { onTabSelected(index) },
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
fun GlassesItem(
    glasses: Glasses,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    isError: Boolean = false
) {
    val isPlaceholder = glasses.id?.startsWith("placeholder_") == true
    
    Box(modifier = Modifier.clickable { if (!isPlaceholder) onClick() }) {
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
                    val imageModel = when {
                        isPlaceholder -> if (isError) R.drawable.placeholder else R.drawable.eye_glasses
                        glasses.images.isNotEmpty() -> glasses.images.first()
                        else -> R.drawable.eye_glasses
                    }
                    
                    Image(
                        painter = rememberAsyncImagePainter(model = imageModel),
                        contentDescription = glasses.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { /* Try on functionality */ },
                    modifier = Modifier
                        .width(72.dp)
                        .height(30.dp)
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Blue1
                    ),
                    border = BorderStroke(1.dp, Blue1),
                    contentPadding = PaddingValues(4.dp),
                    enabled = !isPlaceholder
                ) {
                    Text(
                        stringResource(R.string.try_on),
                        fontSize = 12.sp,
                        color = Blue1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = when {
                        isPlaceholder && isError -> stringResource(R.string.could_not_load)
                        isPlaceholder -> stringResource(R.string.loading)
                        else -> glasses.name ?: ""
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isPlaceholder) if (isError) Color.Red else Color.Gray else Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = if (isPlaceholder) stringResource(R.string.price_placeholder) 
                           else stringResource(R.string.currency_format).format(glasses.price),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPlaceholder) Color.Gray else Color.Black
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (!isPlaceholder && glasses.colors.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        glasses.colors.take(4).forEach { colorString ->
                            val color = Color(android.graphics.Color.parseColor(colorString))
                            ColorOption(color)
                        }
                    }
                }
            }
        }
        
        if (!isPlaceholder) {
            IconButton(
                onClick = { onFavoriteClick() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (glasses.isFavorite) R.drawable.heart_filled else R.drawable.heart
                    ),
                    contentDescription = if (glasses.isFavorite) 
                        stringResource(R.string.desc_unmark_favorite) else 
                        stringResource(R.string.desc_mark_favorite),
                    tint = Color.Blue
                )
            }
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

fun createPlaceholderGlasses(count: Int): List<Glasses> {
    return List(count) { index ->
        Glasses(
            id = "placeholder_$index",
            name = "Placeholder Glasses $index",
            price = 0.0,
            images = emptyList(),
            colors = emptyList(),
            isFavorite = false,
            stock = 0,
            shape = "",
            weight = 0.0,
            size = "",
            material = "",
            type = "",
            gender = "",
            createdAt = ""
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FaceFitTheme { AllProducts() }
}