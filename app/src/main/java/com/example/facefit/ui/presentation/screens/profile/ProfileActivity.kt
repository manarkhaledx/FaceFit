package com.example.facefit.ui.presentation.screens.profile

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.facefit.R
import com.example.facefit.domain.models.Order
import com.example.facefit.domain.models.User
import com.example.facefit.ui.presentation.components.ErrorScreen
import com.example.facefit.ui.presentation.components.PullToRefreshContainer
import com.example.facefit.ui.presentation.components.navigation.AppBottomNavigation
import com.example.facefit.ui.presentation.screens.auth.login.LoginPage
import com.example.facefit.ui.presentation.screens.auth.signUp.EgyptPhoneNumberField
import com.example.facefit.ui.presentation.screens.cart.AllOrdersActivity
import com.example.facefit.ui.theme.Black
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.Gray100
import com.example.facefit.ui.theme.Gray200
import com.example.facefit.ui.theme.Gray600
import com.example.facefit.ui.theme.White
import com.example.facefit.ui.theme.lightBackground
import com.example.facefit.ui.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale


@AndroidEntryPoint
class ProfileActivity : ComponentActivity() {
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            com.example.facefit.ui.theme.FaceFitTheme {
                val userState by viewModel.userState.collectAsStateWithLifecycle()
                val isLoggedOut by viewModel.isLoggedOut.collectAsStateWithLifecycle()
                val updateState by viewModel.updateState.collectAsStateWithLifecycle()
                val profileEditUiState by viewModel.profileEditUiState.collectAsStateWithLifecycle()
                val imageUploadState by viewModel.imageUploadState.collectAsStateWithLifecycle()
                val ordersState by viewModel.ordersState.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) {
                    viewModel.loadUserOrders(3)
                }

                val isRefreshing = userState is ProfileState.Loading || ordersState is OrdersState.Loading

                LaunchedEffect(updateState) {
                    when (updateState) {
                        is UpdateState.Success -> {
                            Toast.makeText(
                                this@ProfileActivity,
                                "Your profile has been updated successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            viewModel.clearUpdateState()
                        }

                        is UpdateState.Error -> {
                            Toast.makeText(
                                this@ProfileActivity,
                                (updateState as UpdateState.Error).message, // Use the message from the state
                                Toast.LENGTH_LONG
                            ).show()
                            viewModel.clearUpdateState()
                        }

                        is UpdateState.ValidationError -> {
                            Toast.makeText(
                                this@ProfileActivity,
                                "Please check the information you entered. Some fields have errors.",
                                Toast.LENGTH_LONG
                            ).show()
                            viewModel.clearUpdateState()
                        }

                        else -> {}
                    }
                }

                LaunchedEffect(imageUploadState) {
                    when (imageUploadState) {
                        is ImageUploadState.Success -> {
                            Toast.makeText(
                                this@ProfileActivity,
                                "Profile picture updated successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            viewModel.clearImageUploadState()
                        }

                        is ImageUploadState.Error -> {
                            Toast.makeText(
                                this@ProfileActivity,
                                (imageUploadState as ImageUploadState.Error).message, // Use the message from the state
                                Toast.LENGTH_LONG
                            ).show()
                            viewModel.clearImageUploadState()
                        }

                        else -> {}
                    }
                }

                LaunchedEffect(isLoggedOut) {
                    if (isLoggedOut) {
                        val intent = Intent(this@ProfileActivity, LoginPage::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (val state = userState) {
                        is ProfileState.Loading -> ShimmerProfileScreen()
                        is ProfileState.Error -> {
                            val message = state.message
                            val isNetworkError = message.contains(
                                "internet connection",
                                ignoreCase = true
                            ) || message.contains("network error", ignoreCase = true) || message.contains("timeout", ignoreCase = true)

                            PullToRefreshContainer(
                                isRefreshing = isRefreshing,
                                onRefresh = { viewModel.loadUserProfile() },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                ErrorScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    title = if (isNetworkError) "No Internet Connection" else "Something Went Wrong",
                                    message = if (isNetworkError) "Please check your connection and try again." else message,
                                    imageResId = if (isNetworkError) R.drawable.no_int else R.drawable.error
                                )
                            }
                        }

                        is ProfileState.Success -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 80.dp)
                            ) {
                                ProfileScreen(
                                    user = state.user,
                                    profileEditUiState = profileEditUiState,
                                    onSignOut = { viewModel.signOut() },
                                    onFirstNameChange = { viewModel.updateFirstName(it) },
                                    onLastNameChange = { viewModel.updateLastName(it) },
                                    onPhoneChange = { viewModel.updatePhone(it) },
                                    onAddressChange = { viewModel.updateAddress(it) },
                                    onUpdateProfile = {
                                        viewModel.updateUserProfile()
                                    },
                                    onCancelEdit = {
                                        viewModel.clearEditStateAndErrors()
                                    },
                                    onImageSelected = { uri ->
                                        viewModel.uploadProfileImage(uri, applicationContext)
                                    },
                                    isImageUploading = imageUploadState is ImageUploadState.Loading,
                                    ordersState = ordersState,
                                    onViewAllOrders = {
                                        val intent = Intent(this@ProfileActivity, AllOrdersActivity::class.java)
                                        startActivity(intent)
                                    }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                        AppBottomNavigation()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.loadUserProfile()
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ProfileScreen(
    user: User,
    profileEditUiState: ProfileEditUiState,
    onSignOut: () -> Unit,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onUpdateProfile: () -> Unit,
    onCancelEdit: () -> Unit,
    onImageSelected: (Uri) -> Unit,
    isImageUploading: Boolean,
    ordersState: OrdersState,
    onViewAllOrders: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                onImageSelected(it)
            }
        }
    )

    val cameraPermissionGranted = remember {
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempCameraUri?.let { uri ->
                    onImageSelected(uri)
                }
            } else {
                Toast.makeText(context, "Image capture cancelled or failed", Toast.LENGTH_SHORT)
                    .show()
            }
            tempCameraUri = null
        }
    )

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera(context) { uri ->
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            }
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    if (showImageSourceDialog) {
        ImageSourceDialog(
            onDismiss = { showImageSourceDialog = false },
            onCameraSelected = {
                showImageSourceDialog = false
                if (cameraPermissionGranted) {
                    launchCamera(context) { uri ->
                        tempCameraUri = uri
                        cameraLauncher.launch(uri)
                    }
                } else {
                    requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
            },
            onGallerySelected = {
                showImageSourceDialog = false
                galleryLauncher.launch("image/jpeg")
            }

        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ProfileHeader(
                user = user,
                onPickPhoto = { showImageSourceDialog = true },
                isImageUploading = isImageUploading
            )
        }

        item {
            PersonalInformationCard(
                profileEditUiState = profileEditUiState,
                isEditing = isEditing,
                onToggleEdit = {
                    isEditing = !isEditing
                    if (!isEditing) {
                        onCancelEdit()
                    }
                },
                onSaveChanges = { onUpdateProfile() },
                onCancel = {
                    isEditing = false
                    onCancelEdit()
                },
                onFirstNameChange = onFirstNameChange,
                onLastNameChange = onLastNameChange,
                onPhoneChange = onPhoneChange,
                onAddressChange = onAddressChange
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Orders",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Black
                )

                Text(
                    text = "View All",
                    fontSize = 14.sp,
                    color = Blue1,
                    modifier = Modifier.clickable { onViewAllOrders() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (ordersState) {
                is OrdersState.Loading -> ShimmerOrderCard()
                is OrdersState.Error -> Text(
                    text = "Couldn't load orders: ${ordersState.message}",
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                is OrdersState.Success -> {
                    if (ordersState.orders.isEmpty()) {
                        Text(
                            text = "No orders yet",
                            color = Gray600,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ordersState.orders.forEach { order ->
                                OrderCard(order = order)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Account Settings",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Black,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            AccountSettingsCard(onSignOut = onSignOut)
        }
    }
}

@Composable
fun OrderCard(order: Order, modifier: Modifier = Modifier) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Order #${order._id.takeLast(6)}",
                        fontSize = 14.sp,
                        color = Gray600,
                        fontWeight = FontWeight.Medium
                    )

                    // Parse and format date
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val formattedDate = try {
                        val date = inputFormat.parse(order.date)
                        date?.let { outputFormat.format(it) } ?: "Unknown Date"
                    } catch (e: Exception) {
                        Log.e("OrderCard", "Error parsing date: ${e.message}")
                        "Unknown Date"
                    }

                    Text(
                        text = formattedDate,
                        fontSize = 12.sp,
                        color = Gray600,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Order status surface
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (order.status.lowercase()) {
                        "delivered" -> Color(0xFF4CAF50).copy(alpha = 0.1f) // Green for delivered
                        "shipped" -> Blue1.copy(alpha = 0.1f) // Blue for shipped
                        else -> Gray200.copy(alpha = 0.5f) // Default for other statuses
                    }
                ) {
                    Text(
                        text = order.status.replaceFirstChar { it.titlecase() }, // Capitalize first letter
                        fontSize = 12.sp,
                        color = when (order.status.lowercase()) {
                            "delivered" -> Color(0xFF4CAF50) // Green text
                            "shipped" -> Blue1 // Blue text
                            else -> Gray600 // Default text color
                        },
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (order.items.isNotEmpty() && order.items[0].item.images.isNotEmpty()) {
                    val firstOrderItemImage = order.items[0].item.images[0]
                    val imageUrl = "${Constants.GET_PRIMAGE_ENDPOINT}${order.items[0].item.images[0]}"

                    Log.e("OrderCardImage", "Attempting to load image from URL: $imageUrl")

                    Image(
                        painter = rememberAsyncImagePainter(
                            model = imageUrl,
                            error = painterResource(id = R.drawable.placeholder),
                            placeholder = painterResource(id = R.drawable.placeholder)
                        ),
                        contentDescription = "Image for order item: ${order.items[0].item.name}",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Gray100),
                        contentAlignment = Alignment.Center
                    ) {

                    }
                }

                Spacer(modifier = Modifier.weight(1f))


                Text(
                    text = "EGP ${"%.2f".format(order.total)}",
                    fontSize = 16.sp,
                    color = Black,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))


                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "View Order Details",
                    tint = Gray600,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


@Composable
fun ProfileHeader(
    user: User,
    onPickPhoto: () -> Unit,
    isImageUploading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(80.dp)) {
            val baseUrl = Constants.GET_IMAGE_ENDPOINT
            val imageUrl = user.profilePicture?.let {
                if (it.startsWith("http")) it else "$baseUrl$it"
            }

            Image(
                painter = rememberAsyncImagePainter(
                    model = imageUrl,
                    placeholder = null,
                    error = null
                ),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            if (isImageUploading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(80.dp),
                    color = Blue1,
                    strokeWidth = 4.dp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Blue1)
                        .align(Alignment.BottomEnd)
                        .clickable { onPickPhoto() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = "Edit",
                        tint = White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${user.firstName} ${user.lastName}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Black
        )

        Text(
            text = user.email,
            fontSize = 14.sp,
            color = Gray600,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun PersonalInformationCard(
    profileEditUiState: ProfileEditUiState,
    isEditing: Boolean,
    onToggleEdit: () -> Unit,
    onSaveChanges: () -> Unit,
    onCancel: () -> Unit,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAddressChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personal Information",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Black
                )

                Row {
                    if (isEditing) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = Blue1,
                            modifier = Modifier
                                .clickable { onSaveChanges() }
                                .padding(end = 12.dp)
                        )

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = Color.Red,
                            modifier = Modifier.clickable { onCancel() }
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Gray600,
                            modifier = Modifier.clickable { onToggleEdit() }
                        )
                    }
                }
            }


            // Full name row
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Gray600,
                    modifier = Modifier
                        .padding(top = if (isEditing) 20.dp else 8.dp)
                        .size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Full Name", fontSize = 12.sp, color = Gray600)

                    if (isEditing) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = profileEditUiState.firstName,
                                onValueChange = onFirstNameChange,
                                modifier = Modifier.weight(1f),
                                label = { Text("First Name") },
                                singleLine = true,
                                isError = profileEditUiState.firstNameError != null,
                                supportingText = {
                                    profileEditUiState.firstNameError?.let { errorText ->
                                        Text(text = errorText, color = Color.Red)
                                    }
                                },
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

                            OutlinedTextField(
                                value = profileEditUiState.lastName,
                                onValueChange = onLastNameChange,
                                modifier = Modifier.weight(1f),
                                label = { Text("Last Name") },
                                singleLine = true,
                                isError = profileEditUiState.lastNameError != null,
                                supportingText = {
                                    profileEditUiState.lastNameError?.let { errorText ->
                                        Text(text = errorText, color = Color.Red)
                                    }
                                },
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
                        }
                    } else {
                        Text(
                            text = "${profileEditUiState.firstName} ${profileEditUiState.lastName}",
                            fontSize = 14.sp,
                            color = Black,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }


            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Gray200)

            EditableInfoItem(
                icon = Icons.Default.Email,
                label = "E-mail",
                value = profileEditUiState.email,
                isEditing = false,
                onValueChange = { },
                isError = profileEditUiState.emailError != null,
                errorText = profileEditUiState.emailError
            )


            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Gray200)

            EditableInfoItem(
                icon = Icons.Default.Phone,
                label = "Phone Number",
                value = profileEditUiState.phone,
                isEditing = isEditing,
                onValueChange = onPhoneChange,
                isError = profileEditUiState.phoneError != null,
                errorText = profileEditUiState.phoneError
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Gray200)

            EditableInfoItem(
                icon = Icons.Default.LocationOn,
                label = "Address",
                value = profileEditUiState.address,
                isEditing = isEditing,
                onValueChange = onAddressChange,
                isError = profileEditUiState.addressError != null,
                errorText = profileEditUiState.addressError
            )
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
            tint = Gray600,
            modifier = Modifier
                .padding(top = if (isEditing) 20.dp else 8.dp)
                .size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            if (isEditing) {
                if (label == "Phone Number") {
                    EgyptPhoneNumberField(
                        phoneNumber = value,
                        onPhoneNumberChange = onValueChange,
                        isError = isError,
                        errorMessage = errorText
                    )
                } else {
                    OutlinedTextField(
                        value = value,
                        onValueChange = onValueChange,
                        label = { Text(label) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = isError,
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
                }
            } else {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = Gray600
                )
                Text(
                    text = value,
                    fontSize = 14.sp,
                    color = Black,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun PersonalInfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Gray600,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Gray600
            )

            Text(
                text = value,
                fontSize = 14.sp,
                color = Black,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun MyOrdersCard() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val orders = listOf(
            OrderItem("87652", "12/02/2024", "Delivered", "EGP 120"),
            OrderItem("87652", "12/02/2024", "Shipped", "EGP 120"),
            OrderItem("87652", "12/02/2024", "Delivered", "EGP 120")
        )

        orders.forEach { order ->
            SingleOrderCard(order = order)
        }
    }
}

@Composable
fun SingleOrderCard(order: OrderItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Order #${order.orderNumber}",
                        fontSize = 14.sp,
                        color = Gray600,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = order.date,
                        fontSize = 12.sp,
                        color = Gray600,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (order.status == "Delivered")
                        Color(0xFF4CAF50).copy(alpha = 0.1f)
                    else
                        Blue1.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = order.status,
                        fontSize = 12.sp,
                        color = if (order.status == "Delivered") Color(0xFF4CAF50) else Blue1,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = android.R.drawable.ic_menu_view),
                    contentDescription = "Glasses",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Gray100)
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = order.price,
                    fontSize = 16.sp,
                    color = Black,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "View Order",
                    tint = Gray600,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AccountSettingsCard(onSignOut: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            val signOutItem = SettingsItem(Icons.AutoMirrored.Filled.ExitToApp, "Sign out")
            SettingsItemRow(
                item = signOutItem,
                onClick = { onSignOut() }
            )
        }
    }
}


@Composable
fun SettingsItemRow(item: SettingsItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = if (item.title == "Sign out") Color.Red else Black,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = item.title,
            fontSize = 14.sp,
            color = if (item.title == "Sign out") Color.Red else Black,
            modifier = Modifier.weight(1f)
        )

        if (item.title != "Sign out") {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = Gray600,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ShimmerProfileScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBackground)
            .padding(bottom = 80.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ShimmerProfileHeader() }
        item { ShimmerPersonalInformationCard() }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerText(width = 100.dp, height = 20.dp)
                ShimmerText(width = 60.dp, height = 16.dp)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            Text(
                text = "Account Settings",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Black,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            ShimmerAccountSettingsCard()
        }
    }
}

@Composable
fun ShimmerProfileHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ShimmerBox(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(16.dp))
        ShimmerText(width = 150.dp, height = 24.dp)
        Spacer(modifier = Modifier.height(8.dp))
        ShimmerText(width = 200.dp, height = 16.dp)
    }
}

@Composable
fun ShimmerPersonalInformationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ShimmerText(width = 150.dp, height = 24.dp)
                ShimmerBox(modifier = Modifier.size(24.dp))
            }

            repeat(4) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    ShimmerBox(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        ShimmerText(width = 80.dp, height = 14.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        ShimmerText(width = 180.dp, height = 18.dp)
                    }
                }
                if (it < 3) HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = Gray200
                )
            }
        }
    }
}

@Composable
fun ShimmerOrderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    ShimmerText(width = 100.dp, height = 16.dp)
                    Spacer(modifier = Modifier.height(4.dp))
                    ShimmerText(width = 80.dp, height = 14.dp)
                }
                ShimmerBox(
                    modifier = Modifier
                        .width(70.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.weight(1f))
                ShimmerText(width = 60.dp, height = 20.dp)
                Spacer(modifier = Modifier.width(8.dp))
                ShimmerBox(modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun ShimmerAccountSettingsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            repeat(5) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerBox(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    ShimmerText(
                        modifier = Modifier.weight(1f),
                        height = 18.dp
                    )
                    if (it != 4) HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = Gray200
                    )
                }
                if (it < 4) HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = Gray200
                )
            }
        }
    }
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape
) {
    Box(
        modifier = modifier
            .shimmerEffect()
            .clip(shape)
    )
}

@Composable
fun ShimmerText(
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp = 16.dp
) {
    Box(
        modifier = modifier
            .height(height)
            .shimmerEffect()
            .then(if (width != null) Modifier.width(width) else Modifier)
    )
}

@Composable
fun Modifier.shimmerEffect(): Modifier = composed {
    val shimmerColors = listOf(
        Gray200.copy(alpha = 0.6f),
        Gray200.copy(alpha = 0.2f),
        Gray200.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition()
    val anim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    this.background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(anim - 500, anim - 500),
            end = Offset(anim, anim),
            tileMode = TileMode.Clamp
        )
    )
}

data class OrderItem(
    val orderNumber: String,
    val date: String,
    val status: String,
    val price: String
)

data class SettingsItem(
    val icon: ImageVector,
    val title: String
)

private fun launchCamera(context: Context, onUriCreated: (Uri) -> Unit) {
    val imageFile = File.createTempFile(
        "profile_photo_${System.currentTimeMillis()}",
        ".jpg",
        context.cacheDir
    )
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        imageFile
    )
    onUriCreated(uri)
}

@Composable
fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onCameraSelected: () -> Unit,
    onGallerySelected: () -> Unit
) {
    androidx.compose.material.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Image From") },
        text = {
            Column {
                Text(
                    "Camera",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onCameraSelected()
                            onDismiss()
                        }
                        .padding(16.dp)
                )
                Text(
                    "Gallery",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onGallerySelected()
                            onDismiss()
                        }
                        .padding(16.dp)
                )
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
