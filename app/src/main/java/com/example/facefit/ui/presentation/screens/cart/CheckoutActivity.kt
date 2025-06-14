package com.example.facefit.ui.presentation.screens.cart

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.facefit.R
import com.example.facefit.domain.models.CartItem
import com.example.facefit.domain.utils.Resource
import com.example.facefit.ui.presentation.components.ErrorScreen
import com.example.facefit.ui.presentation.components.PullToRefreshContainer
import com.example.facefit.ui.presentation.screens.home.HomePageActivity
import com.example.facefit.ui.presentation.screens.profile.EditableInfoItem // Ensure this is correctly imported
import com.example.facefit.ui.theme.Black
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.FaceFitTheme
import com.example.facefit.ui.theme.Gray600
import com.example.facefit.ui.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CheckoutActivity : ComponentActivity() {
    private val viewModel: CheckoutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceFitTheme {
                CheckoutScreen(
                    onBackClick = { finish() },
                    onOrderSuccess = {
                        Toast.makeText(this, "Order Placed Successfully", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, HomePageActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    },
                    viewModel = viewModel
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure data is loaded or refreshed when the activity resumes
        viewModel.loadUserProfile()
        viewModel.loadCartItems()
        viewModel.calculateCartTotal()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onOrderSuccess: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val cartTotal by viewModel.cartTotal.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val orderStatus by viewModel.orderStatus.collectAsState()

    var isEditingAddress by remember { mutableStateOf(false) }

    // Check if any of the main data sources are currently loading
    val isRefreshing = userProfile is Resource.Loading || cartTotal is Resource.Loading || cartItems is Resource.Loading

    // Determine if an overall error state exists for initial data loading
    val initialDataError: String? = (userProfile as? Resource.Error)?.message
        ?: (cartTotal as? Resource.Error)?.message
        ?: (cartItems as? Resource.Error)?.message

    // Trigger order success callback directly
    LaunchedEffect(orderStatus) {
        if (orderStatus is Resource.Success && (orderStatus as Resource.Success).data == "Order placed successfully") {
            onOrderSuccess()
            viewModel.resetOrderStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = { viewModel.createOrder() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue1),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                enabled = userProfile is Resource.Success && cartTotal is Resource.Success && cartItems is Resource.Success && orderStatus !is Resource.Loading
            ) {
                if (orderStatus is Resource.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Place Order", color = Color.White, fontSize = 18.sp)
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshContainer(
            isRefreshing = isRefreshing,
            onRefresh = {
                viewModel.loadUserProfile()
                viewModel.loadCartItems()
                viewModel.calculateCartTotal()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F6F7))
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (initialDataError != null) {
                    val isNetworkError = initialDataError.contains("internet connection", ignoreCase = true) || initialDataError.contains("network error", ignoreCase = true) || initialDataError.contains("timeout", ignoreCase = true)
                    ErrorScreen(
                        modifier = Modifier.fillMaxSize(),
                        title = if (isNetworkError) "No Internet Connection" else "Error Loading Data",
                        message = if (isNetworkError) "Please check your connection and try again." else initialDataError,
                        imageResId = if (isNetworkError) R.drawable.no_int else R.drawable.error
                    )
                } else if (isRefreshing) {
                    LoadingCheckoutScreenContent(
                        isEditingAddress = isEditingAddress, 
                        onEditToggle = { /* No-op in loading state */ }
                    )
                } else {

                    if (userProfile is Resource.Success && cartTotal is Resource.Success && cartItems is Resource.Success) {
                        OrderSummaryCard(viewModel)
                        AddressCard(
                            isEditingAddress = isEditingAddress,
                            onEditToggle = { isEditingAddress = !isEditingAddress },
                            viewModel = viewModel
                        )
                        PhoneNumberCard(viewModel)
                        PaymentMethodCard(viewModel)
                        OrderTotalCard(viewModel)
                    } else {

                        Text("Something went wrong while displaying data.", color = Color.Red, modifier = Modifier.padding(16.dp))
                    }
                }

                // Show order status error if any (separate from initial data loading errors)
                if (orderStatus is Resource.Error) {
                    val orderErrorMessage = (orderStatus as Resource.Error).message ?: "Error placing order"
                    val isNetworkErrorDuringOrder = orderErrorMessage.contains("internet connection", ignoreCase = true) || orderErrorMessage.contains("network error", ignoreCase = true) || orderErrorMessage.contains("timeout", ignoreCase = true)
                    ErrorScreen(
                        modifier = Modifier.fillMaxWidth().height(200.dp).padding(horizontal = 16.dp),
                        title = if (isNetworkErrorDuringOrder) "Order Failed: No Internet" else "Order Failed",
                        message = if (isNetworkErrorDuringOrder) "Could not place order. Check your internet." else orderErrorMessage,
                        imageResId = if (isNetworkErrorDuringOrder) R.drawable.no_int else R.drawable.error
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingShimmerEffect(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(8.dp)) {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ), label = "shimmerAlpha"
    )
    val brush = Brush.linearGradient(
        colors = listOf(Color.LightGray.copy(alpha = 0.2f), Color.LightGray.copy(alpha = alpha), Color.LightGray.copy(alpha = 0.2f)),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )
    Spacer(
        modifier = modifier
            .background(brush)
            .clip(shape)
    )
}

@Composable
fun LoadingCheckoutScreenContent(
    isEditingAddress: Boolean,
    onEditToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            LoadingShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(24.dp),
                shape = RoundedCornerShape(4.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LoadingShimmerEffect(modifier = Modifier.size(80.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            LoadingShimmerEffect(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(20.dp),
                                shape = RoundedCornerShape(4.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LoadingShimmerEffect(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .height(16.dp),
                                shape = RoundedCornerShape(4.dp)
                            )
                        }
                        LoadingShimmerEffect(
                            modifier = Modifier
                                .width(60.dp)
                                .height(20.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                    }
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LoadingShimmerEffect(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(24.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                LoadingShimmerEffect(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LoadingShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(20.dp),
                shape = RoundedCornerShape(4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LoadingShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(20.dp),
                shape = RoundedCornerShape(4.dp)
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            LoadingShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(24.dp),
                shape = RoundedCornerShape(4.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LoadingShimmerEffect(
                    modifier = Modifier.size(20.dp),
                    shape = CircleShape
                )
                Spacer(modifier = Modifier.width(12.dp))
                LoadingShimmerEffect(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            LoadingShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .height(24.dp),
                shape = RoundedCornerShape(4.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LoadingShimmerEffect(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape
                )
                Spacer(modifier = Modifier.width(8.dp))
                LoadingShimmerEffect(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(20.dp),
                    shape = RoundedCornerShape(4.dp)
                )
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            repeat(3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LoadingShimmerEffect(
                        modifier = Modifier
                            .width(80.dp)
                            .height(20.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                    LoadingShimmerEffect(
                        modifier = Modifier
                            .width(70.dp)
                            .height(20.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFFE0E0E0)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LoadingShimmerEffect(
                    modifier = Modifier
                        .width(60.dp)
                        .height(24.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                LoadingShimmerEffect(
                    modifier = Modifier
                        .width(90.dp)
                        .height(24.dp),
                    shape = RoundedCornerShape(4.dp)
                )
            }
        }
    }
}


@Composable
private fun OrderSummaryCard(viewModel: CheckoutViewModel) {
    val cartItems by viewModel.cartItems.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Order Summary",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black,
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            when (cartItems) {
                is Resource.Loading -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        repeat(2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LoadingShimmerEffect(modifier = Modifier.size(80.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    LoadingShimmerEffect(
                                        modifier = Modifier
                                            .fillMaxWidth(0.7f)
                                            .height(20.dp),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LoadingShimmerEffect(
                                        modifier = Modifier
                                            .fillMaxWidth(0.5f)
                                            .height(16.dp),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                }
                                LoadingShimmerEffect(
                                    modifier = Modifier
                                        .width(60.dp)
                                        .height(20.dp),
                                    shape = RoundedCornerShape(4.dp)
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Text(
                        text = (cartItems as Resource.Error).message ?: "Error loading cart items",
                        color = Color.Red,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                is Resource.Success -> {
                    val items = (cartItems as Resource.Success<List<CartItem>>).data
                    if (items.isNullOrEmpty()) {
                        Text(
                            text = "Your cart is empty",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = Color(0xFF4D5159)
                            ),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            items.forEach { item ->
                                CartItemRow(item = item)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(item: CartItem) {
    val imageUrl = if (item.glasses.images.isNotEmpty()) {
        "${Constants.EMULATOR_URL}/${item.glasses.images.first()}"
    } else {
        null
    }
    val totalPrice = (item.glasses.price + item.lensPrice) * item.quantity

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray)
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = item.glasses.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.placeholder),
                    placeholder = painterResource(R.drawable.placeholder)
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.placeholder),
                    contentDescription = "Placeholder",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.glasses.name,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Black,
                )
            )
            Text(
                text = "Color: ${item.color}",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color(0xFF4D5159)
                )
            )
            Text(
                text = "Lens: ${item.lensSpecification}",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color(0xFF4D5159)
                )
            )
            Text(
                text = "Quantity: ${item.quantity}",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color(0xFF4D5159)
                )
            )
        }

        Text(
            text = "EGP ${String.format("%.2f", totalPrice)}",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Black,
            )
        )
    }
}

@Composable
private fun AddressCard(
    isEditingAddress: Boolean,
    onEditToggle: () -> Unit,
    viewModel: CheckoutViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shipping Address",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black,
                    )
                )
                IconButton(onClick = onEditToggle) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Address",
                        tint = if (isEditingAddress) Blue1 else Gray600
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (isEditingAddress) {
                EditableInfoItem(
                    icon = Icons.Default.Place,
                    label = "Street name",
                    value = viewModel.streetName,
                    isEditing = true,
                    onValueChange = { viewModel.streetName = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
                EditableInfoItem(
                    icon = Icons.Default.Home,
                    label = "Building name/no",
                    value = viewModel.buildingNameNo,
                    isEditing = true,
                    onValueChange = { viewModel.buildingNameNo = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
                EditableInfoItem(
                    icon = Icons.Default.Place,
                    label = "Floor, apartment, or villa no.",
                    value = viewModel.floorApartmentVillaNo,
                    isEditing = true,
                    onValueChange = { viewModel.floorApartmentVillaNo = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
                EditableInfoItem(
                    icon = Icons.Default.Place,
                    label = "City/Area",
                    value = viewModel.cityArea,
                    isEditing = true,
                    onValueChange = { viewModel.cityArea = it }
                )
            } else {
                val fullAddress = remember(
                    viewModel.streetName,
                    viewModel.buildingNameNo,
                    viewModel.floorApartmentVillaNo,
                    viewModel.cityArea
                ) {
                    listOf(
                        viewModel.streetName,
                        viewModel.buildingNameNo,
                        viewModel.floorApartmentVillaNo,
                        viewModel.cityArea
                    )
                        .filter { it.isNotBlank() }
                        .joinToString(separator = ", ")
                }

                if (fullAddress.isNotBlank()) {
                    Text(
                        text = fullAddress,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color(0xFF4D5159)
                        )
                    )
                } else {
                    Text(
                        text = "No address provided",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color(0xFF4D5159)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PhoneNumberCard(viewModel: CheckoutViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Contact Information",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black,
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            var localPhone by remember { mutableStateOf(viewModel.phoneNumber.removePrefix("+20")) }

            LaunchedEffect(viewModel.phoneNumber) {
                if (!viewModel.phoneNumber.startsWith("+20")) {
                    localPhone = ""
                } else {
                    localPhone = viewModel.phoneNumber.removePrefix("+20")
                }
            }

            EditableInfoItem(
                icon = Icons.Default.Phone,
                label = "Phone Number",
                value = localPhone,
                isEditing = true,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) {
                        localPhone = it.take(11)
                        val normalized =
                            if (localPhone.startsWith("0")) localPhone.drop(1) else localPhone
                        viewModel.phoneNumber = "+20$normalized"
                    }
                },
                leadingContent = {
                    Text(
                        text = "🇪🇬 +20",
                        style = TextStyle(fontSize = 14.sp),
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun PaymentMethodCard(viewModel: CheckoutViewModel) {
    val paymentOptions = listOf("Cash on Delivery")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Payment Method",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black,
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            paymentOptions.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (option == viewModel.selectedPaymentOption),
                            onClick = { viewModel.selectedPaymentOption = option }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (option == viewModel.selectedPaymentOption),
                        onClick = { viewModel.selectedPaymentOption = option },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Blue1,
                            unselectedColor = Color.LightGray
                        ),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = option,
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = Black
                        ),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderTotalCard(viewModel: CheckoutViewModel) {
    val subtotal = (viewModel.cartTotal.value as? Resource.Success)?.data ?: 0.0
    val tax = subtotal * 0.14
    val shipping = 50.0
    val total = subtotal + tax + shipping

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Subtotal",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color(0xFF4D5159)
                    )
                )
                Text(
                    text = "EGP ${String.format("%.2f", subtotal)}",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Black,
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Shipping",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color(0xFF4D5159)
                    )
                )
                Text(
                    text = "EGP ${String.format("%.2f", shipping)}",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Black,
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tax (14%)",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Black,
                    )
                )
                Text(
                    text = "EGP ${String.format("%.2f", tax)}",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Black,
                    )
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFFE0E0E0)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black,
                    )
                )
                Text(
                    text = "EGP ${String.format("%.2f", total)}",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = Black,
                        fontSize = 18.sp
                    )
                )
            }
        }
    }
}
@Composable
fun EditableInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    leadingContent: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorText: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF4D5159), // Gray600
            modifier = Modifier
                .padding(top = if (isEditing) 20.dp else 8.dp)
                .size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            if (isEditing) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(label) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError,
                    leadingIcon = leadingContent?.let { { it() } },
                    supportingText = {
                        if (isError && errorText != null) {
                            Text(text = errorText, color = Color.Red)
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue1,
                        unfocusedBorderColor = Blue1,
                        errorBorderColor = Color.Red,
                        focusedLabelColor = Blue1,
                        unfocusedLabelColor = Blue1,
                        cursorColor = Blue1
                    )
                )
            } else {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = Color(0xFF4D5159) // Gray600
                )
                Text(
                    text = value,
                    fontSize = 14.sp,
                    color = Color.Black, // Black
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}