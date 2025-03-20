package com.example.facefit

        import android.os.Bundle
        import androidx.activity.ComponentActivity
        import androidx.activity.compose.setContent
        import androidx.activity.enableEdgeToEdge
        import androidx.compose.foundation.background
        import androidx.compose.foundation.border
        import androidx.compose.foundation.layout.*
        import androidx.compose.foundation.shape.RoundedCornerShape
        import androidx.compose.material.icons.Icons
        import androidx.compose.material.icons.automirrored.filled.ArrowBack
        import androidx.compose.material3.*
        import androidx.compose.runtime.Composable
        import androidx.compose.ui.Alignment
        import androidx.compose.ui.Modifier
        import androidx.compose.ui.graphics.Color
        import androidx.compose.ui.res.painterResource
        import androidx.compose.ui.text.font.FontWeight
        import androidx.compose.ui.tooling.preview.Preview
        import androidx.compose.ui.unit.dp
        import androidx.compose.ui.unit.sp
        import com.example.facefit.ui.theme.FaceFitTheme

        class WriteReviewActivity : ComponentActivity() {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                enableEdgeToEdge()
                setContent {
                    FaceFitTheme {
                        ReviewScreen { finish() }
                    }
                }
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun ReviewScreen(onBackClick: () -> Unit) {
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
                        LongButton("Submit") { /* Handle submit */ }
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
                    ReviewSection()
                }
            }
        }

        @Composable
        fun ReviewSection() {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = RoundedCornerShape(12.dp))
                    .border(1.dp, Color.LightGray, shape = RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text("What’s your fair rate for your glasses?*", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                StarRating()
                Spacer(modifier = Modifier.height(4.dp))
                Text("Click to rate the product", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Let us know your overall product experience*", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Describe your overall product experience to let others know it", fontSize = 14.sp, color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        @Composable
        fun StarRating() {
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(4) {
                    Icon(painter = painterResource(id = R.drawable.rate_star_filled), contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.Unspecified)
                }
                Icon(painter = painterResource(id = R.drawable.rate_star), contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.Unspecified)
            }
        }

        @Preview(showBackground = true)
        @Composable
        fun ReviewScreenPreview() {
            FaceFitTheme {
                ReviewScreen {}
            }
        }