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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.FaceFitTheme

// Entry Point Activity
class HomePageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceFitTheme {
                EyewearScreen()
            }
        }
    }
}
@Preview
@Composable
fun HomePagePreview() {
    FaceFitTheme {
        SearchBarWithCameraButton()
    }
}

@Composable
fun EyewearScreen() {
    val products = getProducts()
    val categories = getCategories()

    var searchText by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SearchBarWithCameraButton()
        Spacer(modifier = Modifier.height(24.dp))
        FeaturedImagesSection()
        Spacer(modifier = Modifier.height(24.dp))
        CategorySection(title = "Categories", categories = categories)
        Spacer(modifier = Modifier.height(24.dp))
        ProductSection(title = "Best Seller", products = products)
        Spacer(modifier = Modifier.height(24.dp))
        ProductSection(title = "New Arrivals", products = products)
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarWithCameraButton() {
    var searchText by remember { mutableStateOf("") } // Track user input

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically, // Align items vertically
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Search Bar
        TextField(
            value = searchText, // Bind input value
            onValueChange = { searchText = it }, // Update text state
            placeholder = { Text("Search", fontSize = 14.sp, color = Color.Gray) },
            shape = RoundedCornerShape(20.dp),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp), // Adjust text size
            modifier = Modifier
                .weight(1f) // Make the search bar take up available space
                .height(56.dp), // Match the height of the button
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.search_icon), // Replace with your icon
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

        Spacer(modifier = Modifier.width(8.dp)) // Add spacing between search bar and button

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
            contentDescription = "Camera Icon",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}



@Composable
fun FeaturedImagesSection() {
    val images = listOf(R.drawable.img_notfound, R.drawable.img_notfound)
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val itemWidth = screenWidth / (categories.size.coerceAtMost(3) + 1)

    Column {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(categories) { (category, imageResId) ->
                Column(
                    modifier = Modifier.width(itemWidth),
                    horizontalAlignment = Alignment.CenterHorizontally

                ) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = category,
                        modifier = Modifier
                            .size(70.dp)
                            .shadow(4.dp, shape = CircleShape)
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

@Composable
fun ProductCard(product: Product) {
    var isFavorite by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.width(160.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(modifier = Modifier.height(120.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.eye_glasses),
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { isFavorite = !isFavorite },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        painter = painterResource(if (isFavorite) R.drawable.heart_filled else R.drawable.heart),
                        contentDescription = if (isFavorite) "Unmark Favorite" else "Mark Favorite",
                        tint = Blue1
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(product.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(product.price, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}

fun getProducts() = listOf(
    Product("Browline Glasses", "EGP 120"),
    Product("Round Frame Glasses", "EGP 150"),
    Product("Wayfarer Glasses", "EGP 200")
)

fun getCategories() = listOf(
    "Men" to R.drawable.men_glasses,
    "Women" to R.drawable.women_glasses,
    "Eye Glasses" to R.drawable.eye_glasses,
    "Sun Glasses" to R.drawable.sun_glasses
)

data class Product(val name: String, val price: String)
