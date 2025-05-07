package com.example.facefit.ui.presentation.screens.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.facefit.R
import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.utils.Resource
import com.example.facefit.ui.presentation.components.cards.ProductCard
import com.example.facefit.ui.presentation.components.navigation.AppBottomNavigation
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.FaceFitTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomePageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceFitTheme {
                val viewModel: HomeViewModel = hiltViewModel()
                EyewearScreen(viewModel)
            }
        }
    }
}

@Composable
fun EyewearScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val bestSellers by viewModel.bestSellers.collectAsStateWithLifecycle()
    val newArrivals by viewModel.newArrivals.collectAsStateWithLifecycle()
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
            SearchBarWithCameraButton()
            Spacer(modifier = Modifier.height(24.dp))
            FeaturedImagesSection()
            Spacer(modifier = Modifier.height(24.dp))
            CategorySection(title = stringResource(R.string.categories), categories = categories)
            Spacer(modifier = Modifier.height(24.dp))

            when (val result = bestSellers) {
                is Resource.Success -> {
                    ProductSection(
                        title = stringResource(R.string.best_seller),
                        products = result.data?.map { it.toProduct() } ?: emptyList()
                    )
                }

                is Resource.Error -> {
                    ProductSection(
                        title = stringResource(R.string.best_seller),
                        products = (result.data ?: emptyList()).map {
                            it.toProduct().copy(name = stringResource(R.string.could_not_load))
                        }
                    )
                }

                is Resource.Loading -> {
                    ProductSection(
                        title = stringResource(R.string.best_seller),
                        products = (result.data ?: emptyList()).map {
                            it.toProduct().copy(name = stringResource(R.string.loading), price = stringResource(R.string.price_placeholder))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (val result = newArrivals) {
                is Resource.Success -> {
                    ProductSection(
                        title = stringResource(R.string.new_arrivals),
                        products = result.data?.map { it.toProduct() } ?: emptyList()
                    )
                }

                is Resource.Error -> {
                    ProductSection(
                        title = stringResource(R.string.new_arrivals),
                        products = (result.data ?: emptyList()).map {
                            it.toProduct().copy(name = stringResource(R.string.could_not_load))
                        }
                    )
                }

                is Resource.Loading -> {
                    ProductSection(
                        title = stringResource(R.string.new_arrivals),
                        products = (result.data ?: emptyList()).map {
                            it.toProduct().copy(name = stringResource(R.string.loading), price = stringResource(R.string.price_placeholder))
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarWithCameraButton() {
    var searchText by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text(stringResource(R.string.search), fontSize = 14.sp, color = Color.Gray) },
            shape = RoundedCornerShape(20.dp),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.search_icon),
                    contentDescription = "Search Icon",
                    tint = Color.Gray
                )
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.width(8.dp))
        CameraButton()
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
    // TODO(): Replace with actual images
    val images = listOf(R.drawable.img_notfound, R.drawable.img_notfound)
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
                    contentDescription = "Featured Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun CategorySection(title: String, categories: List<Pair<String, Int>>) {
    // TODO(): Implement actual logic to fetch categories
    Column {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(categories) { (category, imageResId) ->
                Column(
                    modifier = Modifier.width(100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = category,
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
fun ProductSection(title: String, products: List<Product>) {
    Column {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(products) { product -> ProductCard(product) }
        }
    }
}

data class Product(
    val name: String,
    val price: String,
    val imageUrl: String? = null,
    val isPlaceholder: Boolean = false
)

fun Glasses.toProduct(): Product {
    val isPlaceholder = id?.startsWith("placeholder_") ?: false
    return Product(
        name = if (isPlaceholder) "Loading..." else this.name,
        price = if (isPlaceholder) "---" else "EGP ${this.price}",
        imageUrl = this.images.firstOrNull(),
        isPlaceholder = isPlaceholder
    )
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