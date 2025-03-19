package com.example.facefit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.facefit.ui.theme.Black
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.FaceFitTheme
import com.example.facefit.ui.theme.Gray100
import com.example.facefit.ui.theme.Gray200
import com.example.facefit.ui.theme.LavenderBlue

class ProductDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceFitTheme {
                ProductDetailScreen(
                    onBackClick = { finish() },
                    onNavigateToLenses = {
                        val intent = Intent(this,PrescriptionLensActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}
@Preview
@Composable
fun ProductDetailPreview() {
    FaceFitTheme {
        ProductDetailScreen(
            onBackClick = { /* Handle back click */ },
            onNavigateToLenses = { /* Handle navigate to lenses */ }
        )
    }
}

@Composable
fun ProductDetailScreen(
    onBackClick: () -> Unit,
    onNavigateToLenses: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        bottomBar = {
            ProductBottomNavBar(
                onTryOnClick = { /* Handle Try-On */ },
                onSelectLensesClick = onNavigateToLenses
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Gray100)
                .padding(paddingValues) // Ensure content is not overlapped by the bottom bar
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

                Text(
                    text = "Round Glasses",
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

                IconButton(onClick = { /* Handle share */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.share),
                        contentDescription = "Share",
                        tint = Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                var isFavorite by remember { mutableStateOf(false) }
                IconButton(onClick = { isFavorite = !isFavorite }) {
                    Icon(
                        painter = painterResource(
                            id = if (isFavorite) R.drawable.heart_filled else R.drawable.heart
                        ),
                        contentDescription = "Favorite",
                        tint = Blue1,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }


            // Main Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
Image(
    painter = painterResource(id = R.drawable.eye_glasses),
    contentDescription = "Product Image",
    modifier = Modifier
        .fillMaxSize()
        .clip(RoundedCornerShape(8.dp)),
    contentScale = ContentScale.Crop
)
                        Button(
                            onClick = { /* Handle AR try-on */ },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 8.dp, bottom = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LavenderBlue,
                                contentColor = Blue1
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.camera),
                                contentDescription = "Camera",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("AR Try-On")
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Round Glasses",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "EGP 150",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight(600),
                                color = Color(0xFF111928),
                                letterSpacing = 0.8.sp,
                            )
                        )
                    }

                    Text("#54321")
                }

                val colors = listOf(Color.Yellow, Color.Blue, Color.Green, Color.Black)
                val labels = listOf("Yellow", "Blue", "Green", "Black")

                item {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            ColorOptionsSection(colors = colors, labels = labels)
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
                            SpecificationRow("Shape", "Rounded")
                            SpecificationRow("Size", "Medium 11-11-11")
                            SpecificationRow("Weight", "20.3 gm")
                            SpecificationRow("Material", "Plastic")
                        }
                    }
                }

                item {
                    ReviewsSection(11)
                }

                item {
                    Text(
                        text = "Recommendation",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = Black,
                        )
                    )
                }

                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(4) {
                            GlassesItem(onClick = { /* Handle item click */ })
                        }
                    }
                }
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
fun ReviewsSection(reviewsCount: Int) {
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
                "Reviews(${reviewsCount})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(
                onClick = { /* Handle see all */ },
                colors = ButtonDefaults.textButtonColors(contentColor = Blue1)
            ) {
                Text(
                    "See all",
                    style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.Underline)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "4.0",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight(600),
                    color = Black,

                    )
            )
            Spacer(Modifier.width(8.dp))
            Row {
                repeat(4) {
                    Icon(
                        painter = painterResource(id = R.drawable.rate_star_filled),
                        contentDescription = null,
                        tint = Blue1,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Icon(
                    painter = painterResource(id = R.drawable.rate_star),
                    contentDescription = null,
                    tint = Blue1,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        repeat(2) { // Repeat the review item twice
            ReviewItem()
        }
    }
}
@Composable
fun ReviewItem() {
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
                    "User Name",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "12/2/2024",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(4) {
                    Icon(
                        painter = painterResource(id = R.drawable.rate_star_filled),
                        contentDescription = null,
                        tint = Blue1,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Icon(
                    painter = painterResource(id = R.drawable.rate_star),
                    contentDescription = null,
                    tint = Blue1,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Lorem ipsum dolor sit amet consectetur...",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ProductBottomNavBar(onTryOnClick: () -> Unit, onSelectLensesClick: () -> Unit) {
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
                    contentColor = Blue1
                ),
                border = BorderStroke(1.dp, Blue1),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.camera), // Replace with your icon
                    contentDescription = "Try-On Icon",
                    modifier = Modifier.size(20.dp),
                    tint = Blue1
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Try-On", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.width(16.dp)) // Space between buttons

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

