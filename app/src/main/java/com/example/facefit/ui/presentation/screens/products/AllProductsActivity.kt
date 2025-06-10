package com.example.facefit.ui.presentation.screens.products

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.example.facefit.AR.augmentedfaces.AugmentedFacesActivity
import com.example.facefit.R
import com.example.facefit.domain.models.Glasses
import com.example.facefit.ui.presentation.components.GlobalErrorToast
import com.example.facefit.ui.presentation.components.PullToRefreshContainer
import com.example.facefit.ui.presentation.components.navigation.AppBottomNavigation
import com.example.facefit.ui.presentation.screens.filter.FilterScreenOverlay
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.FaceFitTheme
import com.example.facefit.ui.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllProductsActivity : ComponentActivity() {
    private val viewModel: AllProductsViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var showFilter by remember { mutableStateOf(false) }
            val viewModel: AllProductsViewModel = hiltViewModel()
            val categoryFilter = intent.getStringExtra("CATEGORY_FILTER")
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                            },
                            currentFilters = uiState.activeFilters
                        )
                    }
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        viewModel.loadFavorites()
    }
}

@Composable
fun AllProducts(
    onClick: (String) -> Unit = {},
    onFilterClick: () -> Unit = {},
    viewModel: AllProductsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val favoriteStatus by viewModel.favoriteStatus.collectAsStateWithLifecycle()
    val pendingFavorites by viewModel.pendingFavorites.collectAsStateWithLifecycle()
    val toastTrigger by viewModel.toastTrigger.collectAsStateWithLifecycle()

    GlobalErrorToast(errorMessage = uiState.error, trigger = toastTrigger)

    LaunchedEffect(Unit) {
        viewModel.loadAllProducts()
    }

    Scaffold(bottomBar = { AppBottomNavigation() }) { paddingValues ->
        PullToRefreshContainer(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refresh() }
        ) {
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
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { viewModel.filterByType(it) }
                )

                when {
                    uiState.isLoading -> {
                        // Show shimmer placeholders
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(6) {
                                ShimmerGlassesItem()
                            }
                        }
                    }

                    uiState.error != null -> {
                        // Error state with placeholders
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(6) {
                                GlassesItem(
                                    glasses = Glasses(
                                        id = "error_placeholder",
                                        name = stringResource(R.string.could_not_load),
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
                                    ),
                                    favoriteStatus = favoriteStatus,
                                    pendingFavorites = pendingFavorites,
                                    onClick = { },
                                    onFavoriteClick = {},
                                    onTryOnClick = {},
                                    isPlaceholder = true
                                )
                            }
                        }
                    }

                    uiState.products.isEmpty() -> {
                        // Empty state
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
                                onClick = { viewModel.clearFilters() },
                                colors = ButtonDefaults.buttonColors(containerColor = Blue1)
                            ) {
                                Text(stringResource(R.string.clear_filters))
                            }
                        }
                    }

                    else -> {
                        // Success state
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.products) { glasses ->
                                GlassesItem(
                                    glasses = glasses,
                                    favoriteStatus = favoriteStatus,
                                    pendingFavorites = pendingFavorites,
                                    onClick = { id -> onClick(id) },
                                    onFavoriteClick = {
                                        viewModel.toggleFavorite(glasses.id)
                                    },
                                    onTryOnClick = { context ->
                                        if (glasses.tryOn && glasses.arModels != null) {
                                            val intent = Intent(
                                                context,
                                                AugmentedFacesActivity::class.java
                                            ).apply {
                                                putExtra(
                                                    "FRAME_PATH",
                                                    "${Constants.EMULATOR_URL}${glasses.arModels.frameObj}"
                                                )
                                                putExtra(
                                                    "FRAME_MTL_PATH",
                                                    "${Constants.EMULATOR_URL}${glasses.arModels.frameMtl}"
                                                )
                                                putExtra(
                                                    "LENSES_PATH",
                                                    "${Constants.EMULATOR_URL}${glasses.arModels.lensesObj}"
                                                )
                                                putExtra(
                                                    "LENSES_MTL_PATH",
                                                    "${Constants.EMULATOR_URL}${glasses.arModels.lensesMtl}"
                                                )
                                                putExtra(
                                                    "ARMS_PATH",
                                                    "${Constants.EMULATOR_URL}${glasses.arModels.armsObj}"
                                                )
                                                putExtra(
                                                    "ARMS_MTL_PATH",
                                                    "${Constants.EMULATOR_URL}${glasses.arModels.armsMtl}"
                                                )
                                                putExtra(
                                                    "FRAME_MATERIALS",
                                                    glasses.arModels.frameMaterials?.map { "${Constants.EMULATOR_URL}$it" }
                                                        ?.toTypedArray()
                                                )
                                                putExtra(
                                                    "ARMS_MATERIALS",
                                                    glasses.arModels.armsMaterials?.map { "${Constants.EMULATOR_URL}$it" }
                                                        ?.toTypedArray()
                                                )
                                            }
                                            context.startActivity(intent)
                                        }
                                    },
                                    isPlaceholder = false
                                )
                            }
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
    favoriteStatus: Map<String, Boolean>,
    pendingFavorites: Set<String>,
    onClick: (String) -> Unit,
    onFavoriteClick: () -> Unit,
    onTryOnClick: (Context) -> Unit,
    isPlaceholder: Boolean = false
) {
    val context = LocalContext.current
    val isFavorite by remember(glasses.id, favoriteStatus, pendingFavorites) {
        derivedStateOf {
            val baseStatus = favoriteStatus[glasses.id] ?: false
            if (pendingFavorites.contains(glasses.id)) !baseStatus else baseStatus
        }
    }

    Box(modifier = Modifier.clickable {
        if (!isPlaceholder && glasses.id != null) {
            onClick(glasses.id)
        }
    }) {
        Card(
            modifier = Modifier.width(180.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()) {
                // Image section
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)) {
                    if (isPlaceholder) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .shimmerEffect()
                        )
                    } else {
                        val imageModel = if (glasses.images.isNotEmpty()) {
                            "${Constants.EMULATOR_URL}/${glasses.images.first()}"
                        } else {
                            R.drawable.placeholder
                        }
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = imageModel,
                                error = painterResource(R.drawable.placeholder),
                                placeholder = painterResource(R.drawable.placeholder)
                            ),
                            contentDescription = glasses.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Try on button
                if (isPlaceholder) {
                    Box(
                        modifier = Modifier
                            .width(72.dp)
                            .height(30.dp)
                            .align(Alignment.CenterHorizontally)
                            .shimmerEffect()
                    )
                } else {
                    Button(
                        onClick = {
                            if (glasses.tryOn && glasses.arModels != null) {
                                onTryOnClick(context)
                            }
                        },
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
                        enabled = glasses.tryOn && glasses.arModels != null
                    ) {
                        Text(
                            stringResource(R.string.try_on),
                            fontSize = 12.sp,
                            color = Blue1,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Product name
                if (isPlaceholder) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(16.dp)
                            .shimmerEffect()
                    )
                } else {
                    Text(
                        text = glasses.name ?: "",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Price
                if (isPlaceholder) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(14.dp)
                            .shimmerEffect()
                    )
                } else {
                    Text(
                        text = stringResource(R.string.currency_format).format(glasses.price),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Color options
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
                } else if (isPlaceholder) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .shimmerEffect()
                                    .clip(CircleShape)
                            )
                        }
                    }
                }
            }
        }

        if (!isPlaceholder) {
            IconButton(
                onClick = {
                    onFavoriteClick()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (isFavorite) R.drawable.heart_filled else R.drawable.heart
                    ),
                    contentDescription = if (isFavorite)
                        stringResource(R.string.desc_unmark_favorite) else
                        stringResource(R.string.desc_mark_favorite)
                )
            }
        }
    }
}

@Composable
fun ShimmerGlassesItem() {
    Box(modifier = Modifier) {
        Card(
            modifier = Modifier.width(180.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()) {
                // Image placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Try on button placeholder
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(30.dp)
                        .align(Alignment.CenterHorizontally)
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Name placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(16.dp)
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Price placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(14.dp)
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Color options placeholder
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .shimmerEffect()
                                .clip(CircleShape)
                        )
                    }
                }
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

@Composable
fun Modifier.shimmerEffect(): Modifier = composed {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 500, translateAnim - 500),
        end = Offset(translateAnim, translateAnim)
    )

    this.then(
        Modifier.drawWithContent {
            drawContent()
            drawRect(
                brush = brush,
                blendMode = BlendMode.SrcAtop
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FaceFitTheme { AllProducts() }
}