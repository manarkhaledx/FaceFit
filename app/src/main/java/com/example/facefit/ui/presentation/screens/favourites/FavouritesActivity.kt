package com.example.facefit.ui.presentation.screens.favourites

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberImagePainter
import com.example.facefit.R
import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.utils.Resource
import com.example.facefit.ui.presentation.base.RefreshableViewModel
import com.example.facefit.ui.presentation.components.PullToRefreshContainer
import com.example.facefit.ui.presentation.components.navigation.AppBottomNavigation
import com.example.facefit.ui.presentation.screens.products.ProductDetailsActivity
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.FaceFitTheme
import com.example.facefit.ui.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavouritesActivity : ComponentActivity() {
    private val viewModel: FavoritesViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceFitTheme {
                FavouritesScreen(
                    onProductClick = { productId ->
                        startActivity(
                            Intent(this, ProductDetailsActivity::class.java).apply {
                                putExtra("productId", productId)
                            }
                        )
                    }
                )
            }
        }
    }
    override fun onResume() {
        super.onResume()
        viewModel.loadFavorites()
    }
}

@Composable
fun FavouritesScreen(
    onProductClick: (String) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val favoritesState by viewModel.favoritesState.collectAsStateWithLifecycle()
    val isRefreshing = favoritesState is Resource.Loading

    Scaffold(
        bottomBar = { AppBottomNavigation() }
    ) { innerPadding ->
        PullToRefreshContainer(
            isRefreshing = isRefreshing,
            onRefresh = { (viewModel as? RefreshableViewModel)?.refresh() },
            modifier = Modifier.padding(innerPadding)
        ) {
            when (favoritesState) {
                is Resource.Loading -> {
                    // Shimmer loading state
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(5) {
                            ShimmerFavoritesItem()
                        }
                    }
                }

                is Resource.Success -> {
                    val favorites = (favoritesState as Resource.Success<List<Glasses>>).data ?: emptyList()
                    if (favorites.isEmpty()) {
                        EmptyFavoritesScreen(onExploreClick = {})
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(favorites) { glasses ->
                                FavouriteItem(
                                    glasses = glasses,
                                    onToggleFavorite = { viewModel.toggleFavorite(glasses.id) },
                                    onClick = { onProductClick(glasses.id) }
                                )
                            }
                        }
                    }
                }

                is Resource.Error -> {
                    val message = (favoritesState as Resource.Error<List<Glasses>>).message ?: "Unknown error"
                    if (message.contains("network", ignoreCase = true) || message.contains("Unable to resolve host", ignoreCase = true)) {
                        NoInternetScreen()
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(message)
                                Button(onClick = { viewModel.loadFavorites() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShimmerFavoritesItem() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFBFC))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image placeholder with shimmer
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(100.dp)
                    .padding(end = 16.dp)
                    .shimmerEffect()
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                // Name placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Price placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(14.dp)
                        .shimmerEffect()
                )
            }

            // Favorite icon placeholder
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .shimmerEffect()
                    .clip(RoundedCornerShape(12.dp))
            )
        }
    }
}

@Composable
fun FavouriteItem(
    glasses: Glasses,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(true) }
    val imageUrl = remember(glasses.images) {
        glasses.images.firstOrNull()?.let { "${Constants.EMULATOR_URL}/$it" }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color(0xFFFAFBFC),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image container with fixed width and flexible height
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(100.dp)
                    .padding(end = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberImagePainter(
                        data = imageUrl,
                        builder = {
                            placeholder(R.drawable.placeholder)
                            error(R.drawable.placeholder)
                            crossfade(true)
                        }
                    ),
                    contentDescription = "Glasses",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = glasses.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "EGP ${glasses.price}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            IconButton(
                onClick = {
                    isFavorite = !isFavorite
                    onToggleFavorite()
                },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isFavorite) R.drawable.heart_filled else R.drawable.heart
                    ),
                    contentDescription = if (isFavorite) "Unmark Favorite" else "Mark Favorite"
                )
            }
        }
    }
}

@Composable
fun EmptyFavoritesScreen(onExploreClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.favourite_empty),
                contentDescription = "No favorites illustration",
                modifier = Modifier.height(180.dp),
                colorFilter = ColorFilter.tint(Blue1)
            )

            Text(
                text = "No favourites yet",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black
            )

            Text(
                text = "Browse our collection and save your\nfavourite styles. We'll keep them here for you.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun NoInternetScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.no_int),
            contentDescription = "No internet illustration",
            modifier = Modifier.height(180.dp)
        )

        Text(
            text = "No Internet Connection",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black
        )

        Text(
            text = "Please check your connection and try again.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
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
fun PreviewFavouritesScreen() {
    FaceFitTheme {
        // FavouritesScreen preview
    }
}