package com.example.facefit.ui.presentation.screens.products

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.facefit.AR.augmentedfaces.AugmentedFacesActivity
import com.example.facefit.R
import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.models.Review
import com.example.facefit.ui.presentation.components.ImageCarousel
import com.example.facefit.ui.presentation.components.ProductItem
import com.example.facefit.ui.presentation.components.PullToRefreshContainer
import com.example.facefit.ui.presentation.components.cards.ProductCard
import com.example.facefit.ui.presentation.screens.prescription.PrescriptionLensActivity
import com.example.facefit.ui.presentation.screens.reviews.CustomersReviewsActivity
import com.example.facefit.ui.theme.Black
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.FaceFitTheme
import com.example.facefit.ui.theme.Gray100
import com.example.facefit.ui.theme.Gray200
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductDetailsActivity : ComponentActivity() {
    private val viewModel: ProductDetailsViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val productId = intent.getStringExtra("productId") ?: ""
            val viewModel: ProductDetailsViewModel = hiltViewModel()

            LaunchedEffect(productId) {
                viewModel.loadProductDetails(productId)
            }

            FaceFitTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                ProductDetailScreen(
                    viewModel = viewModel,
                    glasses = uiState.glasses,
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    onBackClick = { finish() },
                    onNavigateToLenses = {
                        val intent = Intent(this, PrescriptionLensActivity::class.java)
                        startActivity(intent)
                    },
                    onNavigateToReviews = { productId ->
                        val intent = Intent(this, CustomersReviewsActivity::class.java).apply {
                            putExtra("productId", productId)
                        }
                        startActivity(intent)
                    },
                    activity = this
                )
            }
        }
    }
    override fun onResume() {
        super.onResume()
        val productId = intent.getStringExtra("productId") ?: ""
        viewModel.loadReviews(productId)
    }
}

@Preview
@Composable
fun ProductDetailPreview() {
    FaceFitTheme {
//        ProductDetailScreen(
//            onBackClick = { /* Handle back click */ },
//            onNavigateToLenses = { /* Handle navigate to lenses */ }
//        )
    }
}

@Composable
fun ProductDetailScreen(
    glasses: Glasses?,
    isLoading: Boolean,
    error: String?,
    onBackClick: () -> Unit,
    onNavigateToLenses: () -> Unit,
    onNavigateToReviews: (String) -> Unit,
    viewModel: ProductDetailsViewModel = hiltViewModel(),
    activity: ComponentActivity? = null,
    modifier: Modifier = Modifier
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val favoriteStatus by viewModel.favoriteStatus.collectAsStateWithLifecycle()
    val pendingFavorites by viewModel.pendingFavorites.collectAsStateWithLifecycle()
    val reviews by viewModel.reviews.collectAsStateWithLifecycle()
    val recommendations by viewModel.recommendations.collectAsStateWithLifecycle()
    val context = LocalContext.current
    uiState.error?.let { errorMsg ->
        LaunchedEffect(errorMsg) {
            Toast.makeText(
                context,
                errorMsg,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    val glasses = uiState.glasses
    val isFavorite = glasses?.id?.let { id ->
        val baseStatus = favoriteStatus[id] ?: false
        if (pendingFavorites.contains(id)) !baseStatus else baseStatus
    } ?: false



    if (error != null) {
        LaunchedEffect(error) {
            Toast.makeText(
                context,
                error,
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    if (isLoading && glasses == null) {
        ProductDetailPlaceholderUI() // 👈 shimmer or placeholder composable
        return
    }

    if (!isLoading && glasses == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Product not found")
        }
        return
    }


    Scaffold(
        bottomBar = {
            ProductBottomNavBar(
                onTryOnClick = {
                    glasses?.let { product ->
                        if (product.tryOn && product.arModels != null) {
                            activity?.let {
                                val intent = Intent(it, AugmentedFacesActivity::class.java).apply {
                                    putExtra("FRAME_PATH", product.arModels.frameObj)
                                    putExtra("FRAME_MTL_PATH", product.arModels.frameMtl)
                                    putExtra("LENSES_PATH", product.arModels.lensesObj)
                                    putExtra("LENSES_MTL_PATH", product.arModels.lensesMtl)
                                    putExtra("ARMS_PATH", product.arModels.armsObj)
                                    putExtra("ARMS_MTL_PATH", product.arModels.armsMtl)
                                    putExtra("FRAME_MATERIALS", product.arModels.frameMaterials?.toTypedArray())
                                    putExtra("ARMS_MATERIALS", product.arModels.armsMaterials?.toTypedArray())
                                }
                                it.startActivity(intent)
                            }
                        }
                    }
                },
                onSelectLensesClick = onNavigateToLenses,
                isTryOnEnabled = glasses?.tryOn == true && glasses.arModels != null
            )
        }
    ) {  paddingValues ->
        PullToRefreshContainer(
            isRefreshing = isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = modifier
                .fillMaxSize()
                .background(Gray100)
                .padding(paddingValues)
        ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Gray100)
                .padding(paddingValues)
        ) {
            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                glasses?.let {
                    Text(
                        text = it.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .weight(1f),
                        textAlign = TextAlign.Center
                    )
                }

                IconButton(onClick = { /* Handle share */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.share),
                        contentDescription = "Share",
                        tint = Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = {
                        if (!isConnected(context)) {
                            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
                        } else {
                            glasses?.id?.let { viewModel.toggleFavorite(it) }
                        }
                    },
                            enabled = !pendingFavorites.contains(glasses?.id)
                ) {
                    if (pendingFavorites.contains(glasses?.id)) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            painter = painterResource(
                                id = if (isFavorite) R.drawable.heart_filled else R.drawable.heart
                            ),
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer {
                                    if (glasses != null) {
                                        scaleX = if (pendingFavorites.contains(glasses.id)) 0.8f else 1f
                                    }
                                    if (glasses != null) {
                                        scaleY = if (pendingFavorites.contains(glasses.id)) 0.8f else 1f
                                    }
                                }
                                .animateContentSize(),
                            contentDescription = if (isFavorite) "Unmark Favorite" else "Mark Favorite"
                        )
                    }
                }
            }
            val isConnected = remember { isConnected(context) }

            // Main Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    if (glasses != null) {
                        ImageCarousel(
                            images = glasses.images,
                            onTryOnClick = {
                                if (!isConnected(context)) {
                                    Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
                                    return@ImageCarousel
                                }
                                glasses?.let { product ->
                                    if (product.tryOn && product.arModels != null) {
                                        activity?.let {
                                            val intent = Intent(it, AugmentedFacesActivity::class.java).apply {
                                                putExtra("FRAME_PATH", product.arModels.frameObj)
                                                putExtra("FRAME_MTL_PATH", product.arModels.frameMtl)
                                                putExtra("LENSES_PATH", product.arModels.lensesObj)
                                                putExtra("LENSES_MTL_PATH", product.arModels.lensesMtl)
                                                putExtra("ARMS_PATH", product.arModels.armsObj)
                                                putExtra("ARMS_MTL_PATH", product.arModels.armsMtl)
                                                putExtra("FRAME_MATERIALS", product.arModels.frameMaterials?.toTypedArray())
                                                putExtra("ARMS_MATERIALS", product.arModels.armsMaterials?.toTypedArray())
                                            }
                                            it.startActivity(intent)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            isTryOnEnabled = glasses?.tryOn == true && glasses.arModels != null && isConnected
                        )

                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (glasses != null) {
                            Text(
                                glasses.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (glasses != null) {
                            Text(
                                text = stringResource(R.string.currency_format).format(glasses.price),
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight(600),
                                    color = Color(0xFF111928),
                                    letterSpacing = 0.8.sp,
                                )
                            )
                        }
                    }

                    //Text("#${glasses.id}")
                }

                item {
                    var selectedColorIndex by remember { mutableIntStateOf(0) }
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        if (glasses != null) {
                            if (glasses.colors.isNotEmpty()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                                ) {
                                    glasses.colors.take(4).forEachIndexed { index, colorString ->
                                        val color =
                                            Color(android.graphics.Color.parseColor(colorString))
                                        ColorOptionWithLabel(
                                            color = color,
                                            label = colorString,
                                            isSelected = index == selectedColorIndex,
                                            onClick = {
                                                selectedColorIndex = index
                                            }
                                        )
                                    }
                                }

                            }
                        }
                    }
                }

                item {
                    Text(
                        "Product specifications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Spacer(Modifier.height(8.dp))
                            if (glasses != null) {
                                SpecificationRow("Shape", glasses.shape)
                            }
                            if (glasses != null) {
                                SpecificationRow("Size", glasses.size)
                            }
                            if (glasses != null) {
                                SpecificationRow("Weight", "${glasses.weight} gm")
                            }
                            if (glasses != null) {
                                SpecificationRow("Material", glasses.material)
                            }
                        }
                    }
                }

                item {
                    ReviewsSection(
                        viewModel = viewModel,
onSeeAllClick = {
    if (!isConnected(context)) {
        Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
        return@ReviewsSection
    }

                            if (glasses != null) {
                                onNavigateToReviews(glasses.id)
                            }
                        }

                    )
                }

                item {
                    Text(
                        text = "Recommendation",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = Black,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    if (recommendations.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            items(recommendations) { recommendedProduct ->
                                val isRecFavorite by remember(
                                    recommendedProduct.id,
                                    favoriteStatus,
                                    pendingFavorites
                                ) {
                                    derivedStateOf {
                                        val baseStatus =
                                            favoriteStatus[recommendedProduct.id] ?: false
                                        if (pendingFavorites.contains(recommendedProduct.id)) !baseStatus else baseStatus
                                    }
                                }

                                ProductCard(
                                    productItem = recommendedProduct.copy(isFavorite = isRecFavorite),
                                    favoriteStatus = favoriteStatus,
                                    pendingFavorites = pendingFavorites,
                                    modifier = Modifier.width(160.dp),
                                    showFavorite = !recommendedProduct.isPlaceholder,
onClick = {
    if (!isConnected(context)) {
        Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
        return@ProductCard
    }

                                        if (error == null && !recommendedProduct.isPlaceholder) {
                                            activity?.let {
                                                val intent = Intent(it, ProductDetailsActivity::class.java).apply {
                                                    putExtra("productId", recommendedProduct.id)
                                                }
                                                it.startActivity(intent)
                                            }
                                        }
                                    },
                                            onFavoriteClick = {
                                        if (error == null && !recommendedProduct.isPlaceholder) {
                                            viewModel.toggleRecommendedFavorite(recommendedProduct.id)
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        Text(
                            "No recommendations available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                }
            }
        }
    }
}

@Composable
fun ProductDetailPlaceholderUI() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray100)
            .padding(16.dp)
    ) {
        // Image Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title & Price Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(20.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            )
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(20.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Colors
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Product Specs title
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(20.dp)
                .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Specs Card Placeholder
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .padding(vertical = 6.dp)
                            .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reviews placeholder
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(20.dp)
                .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        repeat(2) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Gray200),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}


fun isConnected(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

@Composable
fun RecommendedProductItem(
    product: ProductItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = product.imageUrl ?: R.drawable.placeholder,
                        error = painterResource(R.drawable.placeholder),
                        placeholder = painterResource(R.drawable.placeholder)
                    ),
                    contentDescription = "Recommended Product",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Favorite icon
                if (product.isFavorite) {
                    Icon(
                        painter = painterResource(id = R.drawable.heart_filled),
                        contentDescription = "Favorite",
                        tint = Blue1,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(16.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.price,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
fun ColorOptionsSection(colors: List<Color>, labels: List<String>) {
    var selectedColorIndex by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            colors.forEachIndexed { index, color ->
                ColorOptionWithLabel(
                    color = color,
                    label = labels[index],
                    isSelected = index == selectedColorIndex,
                    onClick = { selectedColorIndex = index }
                )
            }
        }
    }
}

@Composable
fun ColorOptionWithLabel(
    color: Color,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color, CircleShape)
                .clickable { onClick() }
        )
        if (isSelected) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SpecificationRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun ReviewsSection(
    viewModel: ProductDetailsViewModel = hiltViewModel(),
    onSeeAllClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Reviews(${uiState.reviews.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
TextButton(
    onClick = { onSeeAllClick() },
    enabled = uiState.error == null,
    colors = ButtonDefaults.textButtonColors(contentColor = Blue1)
) {
    Text("See all", style = MaterialTheme.typography.bodyMedium.copy(
        textDecoration = TextDecoration.Underline
    ))
}
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "%.1f".format(uiState.averageRating),
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight(600),
                    color = Black,
                )
            )
            Spacer(Modifier.width(8.dp))
            StarRating(rating = uiState.averageRating, starSize = 20.dp)
        }

        if (uiState.reviews.isNotEmpty()) {
            Column {
                uiState.reviews.take(2).forEach { review ->
                    ReviewItem(review = review)
                }
            }
        } else {
            Text(
                "No reviews yet",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
fun StarRating(
    rating: Double,
    individualRating: Int? = null,
    starSize: Dp
) {
    val starsToShow = individualRating?.toDouble() ?: rating

    val fullStars = starsToShow.toInt()
    val hasHalfStar = (starsToShow - fullStars) >= 0.5 && individualRating == null

    Row {
        repeat(fullStars) {
            Icon(
                painter = painterResource(id = R.drawable.rate_star_filled),
                contentDescription = null,
                tint = Blue1,
                modifier = Modifier.size(starSize)
            )
        }

        if (hasHalfStar) {
            Icon(
                painter = painterResource(id = R.drawable.rate_star_filled),
                contentDescription = null,
                tint = Blue1,
                modifier = Modifier.size(starSize)
            )
        }

        repeat(5 - fullStars - if (hasHalfStar) 1 else 0) {
            Icon(
                painter = painterResource(id = R.drawable.rate_star),
                contentDescription = null,
                tint = Blue1,
                modifier = Modifier.size(starSize)
            )
        }
    }
}

@Composable
fun ReviewItem(
    review: Review
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Gray200),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = review.user.displayName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = review.getFormattedDate(),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            StarRating(
                rating = 0.0,
                individualRating = review.rating,
                starSize = 16.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ProductBottomNavBar(
    onTryOnClick: () -> Unit,
    onSelectLensesClick: () -> Unit,
    isTryOnEnabled: Boolean = false
) {
    Surface(
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Try-On Button
            Button(
                onClick = { onTryOnClick() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = if (isTryOnEnabled) Blue1 else Color.Gray
                ),
                border = BorderStroke(1.dp, if (isTryOnEnabled) Blue1 else Color.Gray),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                enabled = isTryOnEnabled
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.camera),
                    contentDescription = "Try-On Icon",
                    modifier = Modifier.size(20.dp),
                    tint = if (isTryOnEnabled) Blue1 else Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Try-On",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isTryOnEnabled) Blue1 else Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Select Lenses Button
            Button(
                onClick = { onSelectLensesClick() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue1,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(text = "Select Lenses", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}