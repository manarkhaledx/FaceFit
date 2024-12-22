package com.example.facefit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.facefit.ui.theme.Black
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.FaceFitTheme
import com.example.facefit.ui.theme.LavenderBlue

class ProductDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceFitTheme {
                ProductDetailScreen(onBackClick = { finish() })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    ProductDetailScreen(onBackClick = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            title = { Text("Round Glasses") },
            actions = {
                IconButton(onClick = { /* Handle share */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.share),
                        contentDescription = "Share",
                        tint = Black
                    )
                }

                val isFavorite = remember { mutableStateOf(false) }
                IconButton(
                    onClick = { isFavorite.value = !isFavorite.value }
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isFavorite.value) R.drawable.heart_filled else R.drawable.heart
                        ),
                        contentDescription = "Favorite",
                        tint = Blue1
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.LightGray, RoundedCornerShape(8.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.eye_glasses),
                        contentDescription = "Product Image",
                        modifier = Modifier.fillMaxSize(),
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
                        "EGP 150",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                        color = Black
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
                    "Recommendation",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            items(4) {
                GlassesItem(onClick = { /* Handle item click */ })
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ColorOptionsSection(colors: List<Color>, labels: List<String>) {
    var selectedColorIndex by remember { mutableStateOf(0) }

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
            Text("4.0", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(8.dp))
            Row {
                repeat(4) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color.Blue
                    )
                }
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color.Blue
                )
            }
        }

        repeat(2) {
            ReviewItem()
        }
    }
}

@Composable
fun ReviewItem() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "User Name",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "12/2/2024",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Row {
            repeat(4) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color.Blue,
                    modifier = Modifier.size(16.dp)
                )
            }
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = Color.Blue,
                modifier = Modifier.size(16.dp)
            )
        }

        Text(
            "Lorem ipsum dolor sit amet consectetur...",
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        TextButton(
            onClick = { /* Handle read more */ },
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("Read More")
        }
    }
}
