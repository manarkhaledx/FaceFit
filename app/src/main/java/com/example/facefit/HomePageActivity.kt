package com.example.facefit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.FaceFitTheme


class HomePageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceFitTheme {
                EyewearApp()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FaceFitTheme {
        EyewearApp()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EyewearApp() {
    val categories = listOf("Men", "Women", "Eyeglasses", "Sunglasses")
    val products = listOf(
        Product("Browline Glasses", "EGP 120"),
        Product("Round Frame Glasses", "EGP 150"),
        Product("Wayfarer Glasses", "EGP 200")
    )

    var searchText by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(false) } // State to control search bar focus

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Row to contain SearchBar and MaterialButton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Bar
            SearchBar(
                query = searchText,
                onQueryChange = { searchText = it },
                onSearch = { isActive = false },
                active = isActive,
                onActiveChange = { isActive = it },
                placeholder = { Text("Search for eyewear...") },
                trailingIcon = {
                    IconButton(onClick = { /* Handle search action */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.search_icon),
                            contentDescription = "Search"
                        )
                    }
                },
                modifier = Modifier.weight(1f) // Takes available space

                ,
            ) {
                // Search suggestions content when active
                Text("Search results can appear here...", Modifier.padding(16.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Material Button beside SearchBar
            IconButton(
                onClick = { /* Handle camera click */ },
                modifier = Modifier
                    .size(48.dp)
//                    .clip(CircleShape) // Optional styling
                    .clip(RoundedCornerShape(8.dp)) // Optional styling
                    .background(Blue1) // Set background color to blue
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.camera),
                    contentDescription = "Camera Icon",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimary // Use white or theme-based color
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Featured Images RecyclerView
        FeaturedImagesRecyclerView(images = listOf(
            R.drawable.img_notfound,
            R.drawable.img_notfound
        ))

        Spacer(modifier = Modifier.height(24.dp))

        // Categories RecyclerView
        CategoryRecyclerView(title = "Categories", categories = categoriesWithImages)

        Spacer(modifier = Modifier.height(24.dp))

        // Best Seller Section RecyclerView
        ProductRecyclerView(title = "Best Seller", products = products)

        Spacer(modifier = Modifier.height(24.dp))

        // New Arrivals Section RecyclerView
        ProductRecyclerView(title = "New Arrivals", products = products)
    }
}




@Composable
fun FeaturedImagesRecyclerView(images: List<Int>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp), // Padding for both start and end
        horizontalArrangement = Arrangement.spacedBy(8.dp) // Space between items
    ) {
        items(images) { image ->
            Card(
                modifier = Modifier
                    .fillParentMaxWidth(0.95f) // Take up 90% of screen width
                    .height(200.dp),
                shape = RoundedCornerShape(8.dp), // Optional: Rounded corners
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
fun CategoryRecyclerView(title: String, categories: List<Pair<String, Int>>) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val itemWidth = screenWidth / (categories.size.coerceAtMost(3) + 1) // Adjust for fitting behavior

    Column {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories) { (category, imageResId) ->
                Column(
                    modifier = Modifier
                        .width(itemWidth), // Set width dynamically
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
                        maxLines = 1, // Limit to one line
                        overflow = TextOverflow.Ellipsis, // Show ellipsis if text overflows
                        modifier = Modifier.padding(top = 4.dp) // Optional spacing
                    )
                }
            }
        }
    }
}



val categoriesWithImages = listOf(
    "Men" to R.drawable.men_glasses,
    "Women" to R.drawable.women_glasses,
    "Eye Glasses" to R.drawable.eye_glasses,
    "Sun Glasses" to R.drawable.sun_glasses
)




@Composable
fun ProductRecyclerView(title: String, products: List<Product>) {
    Text(title, style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(8.dp))
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(products) { product ->
            ProductCard(product)
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    images: List<Int> = listOf(R.drawable.eye_glasses)
) {
    var isFavorite by remember { mutableStateOf(false) }
    val defaultImage = images.firstOrNull() ?: R.drawable.eye_glasses

    Card(
        modifier = Modifier
            .width(160.dp)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Image(
                    painter = painterResource(id = defaultImage),
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                IconButton(
                    onClick = { isFavorite = !isFavorite },
                    modifier = Modifier.align(Alignment.TopEnd)
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = product.price,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


data class Product(
    val name: String,
    val price: String
)
