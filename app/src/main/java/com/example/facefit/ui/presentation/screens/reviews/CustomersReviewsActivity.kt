package com.example.facefit.ui.presentation.screens.reviews

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.facefit.R
import com.example.facefit.ui.presentation.components.buttons.LongButton
import com.example.facefit.ui.presentation.screens.products.ReviewItem
import com.example.facefit.ui.theme.FaceFitTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomersReviewsActivity : ComponentActivity() {
    private val viewModel: CustomerReviewsViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceFitTheme {
                val productId = intent.getStringExtra("productId") ?: ""
                val viewModel: CustomerReviewsViewModel = hiltViewModel()

                LaunchedEffect(productId) {
                    viewModel.loadReviews(productId)
                }

                val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

                CustomersReviewsScreen(
                    uiState = uiState,
                    onBackClick = { finish() },
                    onWriteReviewClick = {
                        val intent = Intent(this, WriteReviewActivity::class.java)
                        intent.putExtra("productId", productId)
                        startActivity(intent)
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersReviewsScreen(
    uiState: CustomerReviewsUiState,
    onBackClick: () -> Unit,
    onWriteReviewClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Customer Reviews",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                LongButton("Write A Review", onClick = onWriteReviewClick)
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.error, color = MaterialTheme.colorScheme.error)
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    ReviewSummary(
                      averageRating = String.format("%.1f", uiState.averageRating).toDouble(),
                        reviewCount = uiState.reviews.size
                    )

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.reviews.size) { index ->
                            val review = uiState.reviews[index]
                            ReviewItem(review = review)
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun StaticStarRating(
    rating: Double,
    modifier: Modifier = Modifier,
    starSize: Int = 24
) {
    val fullStars = rating.toInt().coerceIn(0, 5)
    val emptyStars = 5 - fullStars

    Row(modifier = modifier) {
        repeat(fullStars) {
            Icon(
                painter = painterResource(id = R.drawable.rate_star_filled),
                contentDescription = "Filled Star",
                modifier = Modifier.size(starSize.dp),
                tint = Color.Unspecified
            )
        }
        repeat(emptyStars) {
            Icon(
                painter = painterResource(id = R.drawable.rate_star),
                contentDescription = "Empty Star",
                modifier = Modifier.size(starSize.dp),
                tint = Color.Unspecified
            )
        }
    }
}

@Composable
fun ReviewSummary(averageRating: Double, reviewCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$averageRating",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp)
            )
            Column {
                StaticStarRating(rating = averageRating)

                Text(
                    text = "$reviewCount customer ratings",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
