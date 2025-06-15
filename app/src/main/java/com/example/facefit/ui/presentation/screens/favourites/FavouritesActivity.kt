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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import com.example.facefit.ui.presentation.components.ErrorScreen

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
    val listContentPadding = combineSystemAndContentPadding(top = 0.dp)

    Scaffold(
        bottomBar = { AppBottomNavigation() },
    ) { _ ->
        PullToRefreshContainer(
            isRefreshing = isRefreshing,
            onRefresh = { (viewModel as? RefreshableViewModel)?.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            when (favoritesState) {
                is Resource.Loading -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = listContentPadding
                    ) {
                        items(5) {
                            ShimmerFavoritesItem()
                        }
                    }
                }

                is Resource.Success -> {
                    val favorites = (favoritesState as Resource.Success<List<Glasses>>).data ?: emptyList()
                    if (favorites.isEmpty()) {
                        EmptyFavoritesScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(listContentPadding),
                            onExploreClick = { /* TODO: Implement navigation to explore products */ }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = listContentPadding
                        ) {
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
                    val isNetworkError = message.contains("internet connection", ignoreCase = true) || message.contains("network error", ignoreCase = true)

                    ErrorScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(listContentPadding),
                        title = if (isNetworkError) "No Internet Connection" else "Something Went Wrong",
                        message = if (isNetworkError) "Please check your connection and try again." else message,
                        imageResId = if (isNetworkError) R.drawable.no_int else R.drawable.error
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyFavoritesScreen(
    modifier: Modifier = Modifier,
    onExploreClick: () -> Unit
) {
    Box(
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 16.dp),
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

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.height(8.dp))


                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(14.dp)
                        .shimmerEffect()
                )
            }


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
                    contentDescription = if (isFavorite) "Unmark Favorite" else "Mark Favorite",
                    tint = Blue1
                )
            }
        }
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
        FavouritesScreen(onProductClick = {})
    }
}
@Composable
fun combineSystemAndContentPadding(
    top: Dp = 0.dp,
    bottom: Dp = 0.dp,
    start: Dp = 0.dp,
    end: Dp = 0.dp
): PaddingValues {
    val systemStatusBarsPadding = WindowInsets.statusBars.asPaddingValues()
    val systemNavigationBarsPadding = WindowInsets.navigationBars.asPaddingValues()

    return PaddingValues(
        start = systemNavigationBarsPadding.calculateStartPadding(androidx.compose.ui.unit.LayoutDirection.Ltr) + start,
        top = systemStatusBarsPadding.calculateTopPadding() + top,
        end = systemNavigationBarsPadding.calculateEndPadding(androidx.compose.ui.unit.LayoutDirection.Ltr) + end,
        bottom = systemNavigationBarsPadding.calculateBottomPadding() + bottom
    )
}
