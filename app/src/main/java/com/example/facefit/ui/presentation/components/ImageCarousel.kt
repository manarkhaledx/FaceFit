package com.example.facefit.ui.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.facefit.R
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.LavenderBlue
import com.example.facefit.ui.utils.Constants
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageCarousel(
    images: List<String>,
    onTryOnClick: () -> Unit,
    modifier: Modifier = Modifier,
    isTryOnEnabled: Boolean = false
) {
    val effectiveImages = if (images.isNotEmpty()) images else listOf("placeholder")
    val pagerState = rememberPagerState(pageCount = { effectiveImages.size })
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Pre-compute image models for better performance
    val imageModels = remember(effectiveImages) {
        effectiveImages.map { imageUrl ->
            if (imageUrl != "placeholder") {
                "${Constants.EMULATOR_URL}/${imageUrl}"
            } else {
                R.drawable.placeholder
            }
        }
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Box(modifier = Modifier.fillMaxSize()) {
                    val imageModel = imageModels[page]

                    Image(
                        painter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(context)
                                .data(imageModel)
                                .crossfade(true)
                                .build(),
                            error = painterResource(R.drawable.placeholder),
                            placeholder = painterResource(R.drawable.placeholder)
                        ),
                        contentDescription = "Product Image ${page + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Try-on button
            if (isTryOnEnabled) {
                Button(
                    onClick = onTryOnClick,
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

            // Only show navigation buttons if there's more than one image
            if (effectiveImages.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .padding(start = 8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.7f))
                            .clickable {
                                val targetPage = (pagerState.currentPage - 1)
                                    .coerceAtLeast(0)
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(targetPage)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Previous",
                            tint = Blue1,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .padding(end = 8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.7f))
                            .clickable {
                                val targetPage = (pagerState.currentPage + 1) %
                                        effectiveImages.size
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(targetPage)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Next",
                            tint = Blue1,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        }

        // Page indicators - only show if there are multiple images
        if (effectiveImages.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                for (i in effectiveImages.indices) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (i == pagerState.currentPage) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (i == pagerState.currentPage) Blue1
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(i)
                                }
                            }
                    )
                }
            }
        }
    }
}