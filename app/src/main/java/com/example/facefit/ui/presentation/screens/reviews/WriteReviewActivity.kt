package com.example.facefit.ui.presentation.screens.reviews

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.facefit.R
import com.example.facefit.domain.utils.validators.ReviewValidator
import com.example.facefit.ui.presentation.components.buttons.LongButton
import com.example.facefit.ui.theme.FaceFitTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WriteReviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceFitTheme {
                val productId = intent.getStringExtra("productId") ?: ""
                val viewModel: CustomerReviewsViewModel = hiltViewModel()
                val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

                // Observe submission state
                LaunchedEffect(uiState.isSubmittingReview) {
                    if (!uiState.isSubmittingReview && uiState.error == null) {
                        // Only finish if submission was successful
                        if (uiState.reviewSubmitted) {
                            finish()
                        }
                    }
                }

                ReviewScreen(
                    onBackClick = { finish() },
                    onSubmit = { rating, comment ->
                        viewModel.submitReview(productId, rating, comment)
                    },
                    isSubmitting = uiState.isSubmittingReview,
                    error = uiState.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    onBackClick: () -> Unit,
    onSubmit: (Int, String) -> Unit,
    isSubmitting: Boolean = false,
    error: String? = null
) {

    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var showValidationError by remember { mutableStateOf(false) }
    val isRatingError = showValidationError && rating == 0
    val isCommentError = showValidationError && comment.isBlank()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Review a Product", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
               ReviewSubmitButton(
                    rating = rating,
                    comment = comment,
                    isSubmitting = isSubmitting,
                    onSubmit = onSubmit,
                    setShowValidationError = { showValidationError = it }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Show error if any
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }


            ReviewSection(
                rating = rating,
                onRatingChange = { rating = it },
                comment = comment,
                onCommentChange = { comment = it },
                isRatingError = isRatingError,
                isCommentError = isCommentError
            )


        }
    }
}
@Composable
fun ReviewSubmitButton(
    rating: Int,
    comment: String,
    isSubmitting: Boolean,
    onSubmit: (Int, String) -> Unit,
    setShowValidationError: (Boolean) -> Unit
) {
    LongButton(
        text = if (isSubmitting) "Submitting..." else "Submit",
        onClick = {
            val errors = ReviewValidator.validateReview("temp", rating, comment)
            setShowValidationError(errors.isNotEmpty())
            if (errors.isEmpty()) {
                onSubmit(rating, comment)
            }
        }
    )
}
@Composable
fun FormattedRatingText(averageRating: Double) {
    val averageFormatted = String.format("%.1f", averageRating)
    Text(
        text = "$averageFormatted",
        fontSize = 40.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(end = 8.dp)
    )
}

@Composable
fun ReviewTextField(
    comment: String,
    onCommentChange: (String) -> Unit
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = comment,
            onValueChange = {
                onCommentChange(it)
                errorMessage = ReviewValidator.validateComment(it)
            },
            placeholder = {
                Text(
                    "Describe your overall product experience to let others know it",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(8.dp),
            isError = errorMessage != null,
            supportingText = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (errorMessage != null) {
                        Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                    }
                    Text(
                        "${comment.length} / 500",
                        modifier = Modifier.align(Alignment.End),
                        fontSize = 12.sp,
                        color = if (comment.length > 500) MaterialTheme.colorScheme.error else Color.Gray
                    )
                }
            },
            maxLines = 6
        )
    }
}

@Composable
fun ReviewSection(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    comment: String,
    onCommentChange: (String) -> Unit,
    isRatingError: Boolean = false,
    isCommentError: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .border(1.dp, Color.LightGray, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {

        Text(
            text = "What's your fair rate for your glasses?*",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isRatingError) MaterialTheme.colorScheme.error else Color.Black
        )

        Spacer(modifier = Modifier.height(4.dp))

        StarRating(
            rating = rating,
            onRatingSelected = onRatingChange
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Click to rate the product",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Let us know your overall product experience*",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

      ReviewTextField(
                comment = comment,
                onCommentChange = onCommentChange
            )
    }
}


@Composable
fun StarRating(
    rating: Int,
    onRatingSelected: (Int) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        repeat(5) { index ->
            Icon(
                painter = painterResource(
                    id = if (index < rating) R.drawable.rate_star_filled else R.drawable.rate_star
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onRatingSelected(index + 1) },
                tint = Color.Unspecified
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReviewScreenPreview() {
//    FaceFitTheme {
//        ReviewScreen {}
//    }
}