package com.example.facefit.ui.presentation.screens.favourites

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Button
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Card
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.CircularProgressIndicator
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Icon
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.IconButton
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Scaffold
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is Resource.Success -> {
                    val favorites = (favoritesState as Resource.Success<List<Glasses>>).data ?: emptyList()
                    if (favorites.isEmpty()) {
                        EmptyFavoritesScreen(onExploreClick = {

                        })
                    }
 else {
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
                    val message = (favoritesState as Resource.Error<List<Glasses>>).message
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(message ?: "Unknown error")
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
                modifier = Modifier.height(180.dp)
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


@Preview(showBackground = true)
@Composable
fun PreviewFavouritesScreen() {
//    FaceFitTheme {
//        FavouritesScreen()
//    }
}