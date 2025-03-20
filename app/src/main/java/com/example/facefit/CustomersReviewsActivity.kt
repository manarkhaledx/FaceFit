package com.example.facefit

                import android.os.Bundle
                import androidx.activity.ComponentActivity
                import androidx.activity.compose.setContent
                import androidx.activity.enableEdgeToEdge
                import androidx.compose.foundation.BorderStroke
                import androidx.compose.foundation.layout.*
                import androidx.compose.foundation.lazy.LazyColumn
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
                import com.example.facefit.ui.theme.Blue1
                import com.example.facefit.ui.theme.FaceFitTheme

                class CustomersReviewsActivity : ComponentActivity() {
                    override fun onCreate(savedInstanceState: Bundle?) {
                        super.onCreate(savedInstanceState)
                        enableEdgeToEdge()
                        setContent {
                            FaceFitTheme {
                                CustomersReviewsScreen { finish() }
                            }
                        }
                    }
                }

                @OptIn(ExperimentalMaterial3Api::class)
                @Composable
                fun CustomersReviewsScreen(onBackClick: () -> Unit) {
                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = { Text("Customer Reviews", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
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
                                LongButton("Write A Review") { /* Handle writing review */ }
                            }
                        }
                    ) { paddingValues ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(16.dp)
                        ) {
                            ReviewSummary(averageRating = 4.0, reviewCount = 11)
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(5) { ReviewItem() }
                            }
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
                                Row {
                                    repeat(4) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.rate_star_filled),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                            tint = Color.Unspecified
                                        )
                                    }
                                    Icon(
                                        painter = painterResource(id = R.drawable.rate_star),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = Color.Unspecified
                                    )
                                }
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

                @Composable
                fun LongButton(text: String, onClick: () -> Unit) {
                    Button(
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue1,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(30.dp),
                        border = BorderStroke(1.dp, Blue1)
                    ) {
                        Text(text, fontSize = 16.sp)
                    }
                }

                @Preview(showBackground = true)
                @Composable
                fun CustomersReviewsPreview() {
                    FaceFitTheme {
                        CustomersReviewsScreen {}
                    }
                }