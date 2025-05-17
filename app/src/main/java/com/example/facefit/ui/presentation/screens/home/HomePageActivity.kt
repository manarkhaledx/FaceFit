package com.example.facefit.ui.presentation.screens.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.facefit.R
import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.utils.Resource
import com.example.facefit.ui.presentation.components.ProductItem
import com.example.facefit.ui.presentation.components.cards.ProductCard
import com.example.facefit.ui.presentation.components.navigation.AppBottomNavigation
import com.example.facefit.ui.presentation.components.toProductItem
import com.example.facefit.ui.presentation.screens.products.AllProductsActivity
import com.example.facefit.ui.presentation.screens.products.ProductDetailsActivity
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.FaceFitTheme
import com.example.facefit.ui.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomePageActivity : ComponentActivity() {
    private val viewModel: HomeViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceFitTheme {
                val viewModel: HomeViewModel = hiltViewModel()
                EyewearScreen(viewModel, this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadFavorites()
    }
}

@Composable
fun EyewearScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    activity: ComponentActivity? = null
) {
    val bestSellers by viewModel.bestSellers.collectAsStateWithLifecycle()
    val newArrivals by viewModel.newArrivals.collectAsStateWithLifecycle()
    val favoriteStatus by viewModel.favoriteStatus.collectAsStateWithLifecycle()
    val pendingFavorites by viewModel.pendingFavorites.collectAsStateWithLifecycle()
    val categories = getCategories()

    Scaffold(
        bottomBar = { AppBottomNavigation() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            SearchBar(
                viewModel = viewModel,
                onProductClick = { product ->
                    activity?.let {
                        val intent = Intent(it, ProductDetailsActivity::class.java).apply {
                            putExtra("productId", product.id)
                        }
                        it.startActivity(intent)
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))

            FeaturedImagesSection()
            Spacer(modifier = Modifier.height(24.dp))
            CategorySection(
                title = stringResource(R.string.categories),
                categories = categories,
                onCategoryClick = { category ->
                    activity?.let {
                        val intent = Intent(it, AllProductsActivity::class.java).apply {
                            putExtra("CATEGORY_FILTER", category)
                        }
                        it.startActivity(intent)
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))


            when (val result = bestSellers) {
                is Resource.Success -> {
                    ProductSection(
                        title = stringResource(R.string.best_seller),
                        products = result.data?.map { it.toProductItem() } ?: emptyList(),
                        favoriteStatus = favoriteStatus,
                        pendingFavorites = pendingFavorites,
                        onProductClick = { product ->
                            activity?.let {
                                val intent =
                                    Intent(it, ProductDetailsActivity::class.java).apply {
                                        putExtra(
                                            "productId",
                                            product.id
                                        )
                                    }
                                it.startActivity(intent)
                            }
                        },
                        onFavoriteClick = { productId ->
                            viewModel.toggleFavorite(productId)
                        }
                    )
                }

                is Resource.Error -> {
                    ProductSection(
                        title = stringResource(R.string.best_seller),
                        products = (result.data ?: emptyList()).map {
                            it.toProductItem().copy(
                                name = stringResource(R.string.could_not_load),
                                isPlaceholder = true
                            )
                        },
                        favoriteStatus = favoriteStatus,
                        pendingFavorites = pendingFavorites,
                        onProductClick = {},
                        onFavoriteClick = {}
                    )
                }

                is Resource.Loading -> {
                    ProductSection(
                        title = stringResource(R.string.best_seller),
                        products = (result.data ?: emptyList()).map {
                            it.toProductItem().copy(
                                name = stringResource(R.string.loading),
                                price = stringResource(R.string.price_placeholder),
                                isPlaceholder = true
                            )
                        },
                        favoriteStatus = favoriteStatus,
                        pendingFavorites = pendingFavorites,
                        onProductClick = {},
                        onFavoriteClick = {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (val result = newArrivals) {
                is Resource.Success -> {
                    ProductSection(
                        title = stringResource(R.string.new_arrivals),
                        products = result.data?.map { it.toProductItem() } ?: emptyList(),
                        favoriteStatus = favoriteStatus,
                        pendingFavorites = pendingFavorites,
                        onProductClick = { product ->
                            activity?.let {
                                val intent =
                                    Intent(it, ProductDetailsActivity::class.java).apply {
                                        putExtra(
                                            "productId",
                                            product.id
                                        )
                                    }
                                it.startActivity(intent)
                            }
                        },
                        onFavoriteClick = { productId ->
                            viewModel.toggleFavorite(productId)
                        }
                    )
                }

                is Resource.Error -> {
                    ProductSection(
                        title = stringResource(R.string.new_arrivals),
                        products = (result.data ?: emptyList()).map {
                            it.toProductItem().copy(
                                name = stringResource(R.string.could_not_load),
                                isPlaceholder = true
                            )
                        },
                        favoriteStatus = favoriteStatus,
                        pendingFavorites = pendingFavorites,
                        onProductClick = {},
                        onFavoriteClick = {}
                    )
                }

                is Resource.Loading -> {
                    ProductSection(
                        title = stringResource(R.string.new_arrivals),
                        products = (result.data ?: emptyList()).map {
                            it.toProductItem().copy(
                                name = stringResource(R.string.loading),
                                price = stringResource(R.string.price_placeholder),
                                isPlaceholder = true
                            )
                        },
                        favoriteStatus = favoriteStatus,
                        pendingFavorites = pendingFavorites,
                        onProductClick = {},
                        onFavoriteClick = {}
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    viewModel: HomeViewModel = hiltViewModel(),
    onProductClick: (ProductItem) -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val hasMoreResults by viewModel.hasMoreSearchResults.collectAsStateWithLifecycle()
    var isSearchActive by remember { mutableStateOf(false) }

    Box {
        TextField(
            value = searchText,
            onValueChange = {
                searchText = it
                if (it.isNotBlank()) {
                    viewModel.resetSearch()
                    viewModel.onSearchQueryChanged(it)
                    isSearchActive = true
                } else {
                    isSearchActive = false
                }
            },
            placeholder = {
                Text(
                    stringResource(R.string.search),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            },
            shape = RoundedCornerShape(20.dp),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .zIndex(2f),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            ),
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (searchResults is Resource.Loading && searchText.isNotBlank()) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (searchText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                searchText = ""
                                viewModel.resetSearch()
                                isSearchActive = false
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.close),
                                contentDescription = "Clear search",
                                tint = Color.Gray
                            )
                        }
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.search_icon),
                            contentDescription = stringResource(R.string.search_icon),
                            tint = Color.Gray
                        )
                    }
                }
            }
        )

        if (isSearchActive) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp)
                    .zIndex(3f)
            ) {
                SearchResultsDropdown(
                    searchResults = searchResults,
                    hasMoreResults = hasMoreResults,
                    onProductClick = onProductClick,
                    onLoadMore = { viewModel.loadMoreSearchResults() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .zIndex(1f)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            isSearchActive = false
                            searchText = ""
                            viewModel.resetSearch()
                        }
                    }
            )
        }
    }
}

@Composable
fun SearchResultsDropdown(
    searchResults: Resource<List<Glasses>>,
    hasMoreResults: Boolean,
    onProductClick: (ProductItem) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {

    val uniqueItems = remember(searchResults) {
        when (searchResults) {
            is Resource.Success -> searchResults.data?.distinctBy { it.id } ?: emptyList()
            else -> emptyList()
        }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        when (searchResults) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text("Searching...", color = Color.Gray)
                    }
                }
            }

            is Resource.Success -> {
                //val results = searchResults.data ?: emptyList()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    if (uniqueItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No products found",
                                color = Color.Gray
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                        ) {
                            val scrollState = rememberScrollState()

                            Column(
                                modifier = Modifier
                                    .verticalScroll(scrollState)
                                    .fillMaxWidth()
                            ) {
                                uniqueItems.forEach { glasses ->
                                    SearchResultItem(
                                        glasses = glasses,
                                        onClick = { onProductClick(glasses.toProductItem()) }
                                    )
                                    Divider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 0.5.dp,
                                        color = Color.LightGray.copy(alpha = 0.5f)
                                    )
                                }

                                if (hasMoreResults) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onLoadMore() }
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Load More",
                                            color = Blue1,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }

                            if (scrollState.maxValue > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .width(4.dp)
                                        .fillMaxHeight()
                                        .padding(top = 8.dp, bottom = 8.dp, end = 2.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                            .offset(y = (scrollState.value.toFloat() / scrollState.maxValue.toFloat() *
                                                    (scrollState.maxValue - 40.dp.value)).dp)
                                            .background(
                                                color = Color.Gray.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(2.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            is Resource.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${searchResults.message?.take(30)}...",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    glasses: Glasses,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (glasses.images.isNotEmpty()) {
                AsyncImage(
                    model = "${Constants.EMULATOR_URL}/${glasses.images.first()}",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.placeholder),
                    contentDescription = "Placeholder",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = glasses.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$${"%.2f".format(glasses.price)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Icon(
            painter = painterResource(id = R.drawable.arrow_right),
            contentDescription = "Go to product",
            tint = Color.Gray
        )
    }
}

@Composable
fun CameraButton() {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Blue1),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.camera),
            contentDescription = stringResource(R.string.camera_icon_description),
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun FeaturedImagesSection() {
    val images = listOf(R.drawable.img7, R.drawable.img7)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(images) { image ->
            Card(
                modifier = Modifier
                    .fillParentMaxWidth(0.95f)
                    .height(200.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Image(
                    painter = painterResource(id = image),
                    contentDescription = stringResource(R.string.featured_image),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillWidth
                )
            }
        }
    }
}

@Composable
fun CategorySection(
    title: String,
    categories: List<Pair<String, Int>>,
    onCategoryClick: (String) -> Unit = {}
) {
    Column {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(categories) { (category, imageResId) ->
                Column(
                    modifier = Modifier
                        .width(100.dp)
                        .clickable { onCategoryClick(category) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = stringResource(R.string.category_image),
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProductSection(
    title: String,
    products: List<ProductItem>,
    favoriteStatus: Map<String, Boolean>,
    pendingFavorites: Set<String>,
    onProductClick: (ProductItem) -> Unit = {},
    onFavoriteClick: (String) -> Unit = {}
) {
    Column {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(products) { product ->
                ProductCard(
                    productItem = product,
                    favoriteStatus = favoriteStatus,
                    pendingFavorites = pendingFavorites,
                    showFavorite = !product.isPlaceholder,
                    onClick = { if (!product.isPlaceholder) onProductClick(product) },
                    onFavoriteClick = { if (!product.isPlaceholder) onFavoriteClick(product.id) }
                )
            }
        }
    }
}


fun getCategories() = listOf(
    "Men" to R.drawable.men_glasses,
    "Women" to R.drawable.women_glasses,
    "Eye Glasses" to R.drawable.eye_glasses,
    "Sun Glasses" to R.drawable.sun_glasses
)

@Preview(showBackground = true)
@Composable
fun HomePagePreview() {
    FaceFitTheme {
        EyewearScreen()
    }
}