package com.example.facefit

import android.content.Intent
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.FaceFitTheme
import com.example.facefit.ui.theme.Gray600
import com.example.facefit.ui.theme.LavenderBlue

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

@Composable
fun EyewearScreen() {
    val products = getProducts()
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
            CategorySection(title = "Categories", categories = categories)
            Spacer(modifier = Modifier.height(24.dp))
            ProductSection(title = "Best Seller", products = products)
            Spacer(modifier = Modifier.height(24.dp))
            ProductSection(title = "New Arrivals", products = products)
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
            placeholder = { Text("Search", fontSize = 14.sp, color = Color.Gray) },
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
            contentDescription = "Camera Icon",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun FeaturedImagesSection() {
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

@Composable
fun ProductCard(product: Product) {
    var isFavorite by remember { mutableStateOf(false) }

    Box {
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
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    product.price,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        IconButton(
            onClick = { isFavorite = !isFavorite },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                painter = painterResource(
                    if (isFavorite) R.drawable.heart_filled else R.drawable.heart
                ),
                contentDescription = if (isFavorite) "Unmark Favorite" else "Mark Favorite",
                tint = Blue1
            )
        }
    }
}

@Composable
fun AppBottomNavigation() {
    val context = LocalContext.current
    val defaultSelectedItem = when (context::class.java) {
        HomePageActivity::class.java -> 0
        AllProductsActivity::class.java -> 2
        FavouritesActivity::class.java -> 1
        else -> 0
    }

    var selectedItem by remember { mutableIntStateOf(defaultSelectedItem) }

    val items = listOf(
        NavigationItem("Home", if (selectedItem == 0)  R.drawable.home else R.drawable.home_empty),
        NavigationItem("Favourites", if (selectedItem == 1) R.drawable.heart_filled else R.drawable.heart),
        NavigationItem("Products", if (selectedItem == 2) R.drawable.fill_glasses else R.drawable.glass_icon),
        NavigationItem("Cart", if (selectedItem == 3) R.drawable.cart_fill else R.drawable.cart),
        NavigationItem("Profile", if (selectedItem == 4) R.drawable.profile_fill else R.drawable.profile)
    )

    BottomNavigation(
        backgroundColor = Color.White,
        modifier = Modifier.height(80.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                BottomNavigationItem(
                    icon = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (selectedItem == index) LavenderBlue else Color.Transparent
                                    )
                                    .padding(vertical = 4.dp, horizontal = 12.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = item.iconResId),
                                    contentDescription = item.title,
                                    tint = if (selectedItem == index) Blue1 else Gray600,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Text(
                                text = item.title,
                                fontSize = 10.sp,
                                color = if (selectedItem == index) Blue1 else Gray600
                            )
                        }
                    },
                    selected = selectedItem == index,
                    onClick = {
                        selectedItem = index
                        when (item.title) {
                            "Home" -> context.startActivity(Intent(context, HomePageActivity::class.java))
                            "Products" -> context.startActivity(Intent(context, AllProductsActivity::class.java))
                            "Favourites" -> context.startActivity(Intent(context, FavouritesActivity::class.java))
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private data class NavigationItem(val title: String, val iconResId: Int)

data class Product(val name: String, val price: String)

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

@Preview(showBackground = true)
@Composable
fun HomePagePreview() {
    FaceFitTheme {
        EyewearScreen()
    }
}
