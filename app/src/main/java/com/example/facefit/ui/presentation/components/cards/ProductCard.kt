package com.example.facefit.ui.presentation.components.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.facefit.ui.presentation.components.ProductItem
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.utils.Constants

@Composable
fun ProductCard(
    productItem: ProductItem,
    favoriteStatus: Map<String, Boolean>,
    pendingFavorites: Set<String>,
    modifier: Modifier = Modifier,
    showFavorite: Boolean = true,
    onClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {}
) {

    val isFavorite by remember(productItem.id, favoriteStatus, pendingFavorites) {
        derivedStateOf {
            if (productItem.isPlaceholder) false
            else {
                val baseStatus = favoriteStatus[productItem.id] ?: productItem.isFavorite
                if (pendingFavorites.contains(productItem.id)) !baseStatus else baseStatus
            }
        }
    }

    Box(modifier = modifier.clickable(
        enabled = !productItem.isPlaceholder,
        onClick = onClick)) {
        Card(
            modifier = Modifier.width(160.dp),
            elevation = if (productItem.isPlaceholder)
                CardDefaults.cardElevation(defaultElevation = 0.dp)
            else
                CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (productItem.isPlaceholder)
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else
                    Color.White
            )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth()
                        .background(
                            if (productItem.isPlaceholder)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            else
                                Color.Transparent
                        )
                ) {
                    if (productItem.isPlaceholder) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(R.drawable.placeholder),
                                contentDescription = "Loading",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    } else {
                        AsyncImage(
                            model = productItem.imageUrl?.let {
                                if (it.isNotEmpty()) "${Constants.EMULATOR_URL}/$it"
                                else R.drawable.placeholder
                            } ?: R.drawable.placeholder,
                            contentDescription = productItem.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.placeholder),
                            error = painterResource(R.drawable.placeholder)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    productItem.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (productItem.isPlaceholder)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else
                        Color.Black
                )
                Text(
                    productItem.price,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (productItem.isPlaceholder)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else
                        Blue1,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showFavorite && !productItem.isPlaceholder) {
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.align(Alignment.TopEnd),
                enabled = !pendingFavorites.contains(productItem.id)
            ) {
                Icon(
                    painter = painterResource(
                        if (isFavorite) R.drawable.heart_filled else R.drawable.heart
                    ),
                    contentDescription = if (isFavorite)  "Unmark Favorite"
                    else  "Mark Favorite"
                )
            }
        }
    }
}

