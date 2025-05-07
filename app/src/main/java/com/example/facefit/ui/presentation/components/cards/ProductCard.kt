package com.example.facefit.ui.presentation.components.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.facefit.R
import com.example.facefit.ui.presentation.screens.home.Product
import com.example.facefit.ui.theme.Blue1

@Composable
fun ProductCard(
    product: Product,
    modifier: Modifier = Modifier,
    showFavorite: Boolean = true
) {
    var isFavorite by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Card(
            modifier = Modifier.width(160.dp),
            elevation = if (product.isPlaceholder) CardDefaults.cardElevation(defaultElevation = 0.dp) else CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (product.isPlaceholder)
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else Color.White
            )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth()
                        .background(
                            if (product.isPlaceholder)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            else Color.Transparent
                        )
                ) {
                    if (product.isPlaceholder) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                //.shimmerEffect() // Implement or use a library
                        ) {
                            Image(
                                painter = painterResource(R.drawable.placeholder),
                                contentDescription = "Loading",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentScale = ContentScale.Fit,
//                                colorFilter = ColorFilter.tint(
//                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
//                                )
                            )
                        }
                    } else {
                        AsyncImage(
                            model = product.imageUrl ?: R.drawable.placeholder,
                            contentDescription = product.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.placeholder),
                            error = painterResource(R.drawable.placeholder)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (product.isPlaceholder)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else Color.Black
                )
                Text(
                    product.price,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (product.isPlaceholder)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else Blue1,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showFavorite && !product.isPlaceholder) {
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
}

