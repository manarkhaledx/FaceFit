package com.example.facefit.ui.presentation.screens.profile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.facefit.R
import com.example.facefit.ui.presentation.components.navigation.AppBottomNavigation
import com.example.facefit.ui.theme.*

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceFitTheme {
                Box(modifier = Modifier.fillMaxSize()) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp)
                    ) {
                        ProfileScreen()
                    }


                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                    ) {
                        AppBottomNavigation()
                    }
                }
            }
        }

    }
}

@Composable
fun ProfileScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ProfileHeader()
        }

        item {
            PersonalInformationCard()
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
                    modifier = Modifier.clickable { }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            MyOrdersCard()
        }

        item {
            Text(
                text = "Account Settings",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Black,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            AccountSettingsCard()
        }
    }
}

@Composable
fun ProfileHeader() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile image with edit button
        Box(modifier = Modifier.size(80.dp)) {
            Image(
                painter = painterResource(id = android.R.drawable.ic_menu_camera),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Gray200),
                contentScale = ContentScale.Crop
            )

            // Edit button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Blue1)
                    .align(Alignment.BottomEnd),
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

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Zain Morad",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Black
        )

        Text(
            text = "zainmorad1@example.com",
            fontSize = 14.sp,
            color = Gray600,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}


@Composable
fun PersonalInformationCard() {
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
            // Header with title and edit icon
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

                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Gray600,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Personal info items
            PersonalInfoItem(
                icon = Icons.Default.Person,
                label = "Full Name",
                value = "Prescription Management"
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = Gray200
            )

            PersonalInfoItem(
                icon = Icons.Default.Email,
                label = "E-mail",
                value = "zainmorad1@example.com"
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = Gray200
            )

            PersonalInfoItem(
                icon = Icons.Default.Phone,
                label = "Phone Number",
                value = "+201234567893"
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = Gray200
            )

            PersonalInfoItem(
                icon = Icons.Default.LocationOn,
                label = "Address",
                value = "123 Main Street, Apt 33, 6th of October, Giza"
            )
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
            // Top row with order info and status
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

                // Status badge
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

            // Bottom row with glasses image, price and arrow
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
fun AccountSettingsCard() {
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
            val settingsItems = listOf(
                SettingsItem(Icons.Default.Star, "Payment Methods"),
                SettingsItem(Icons.Default.Star, "Prescription Management"),
                SettingsItem(Icons.Default.Star, "Reviews"),
                SettingsItem(Icons.Default.Star, "Help & Support"),
                SettingsItem(Icons.AutoMirrored.Filled.ExitToApp, "Sign out")
            )

            settingsItems.forEach { item ->
                SettingsItemRow(item = item)
                if (item != settingsItems.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = Gray200
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsItemRow(item: SettingsItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
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

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    FaceFitTheme {
        ProfileScreen()
    }
}
