package com.example.facefit.ui.presentation.screens.cart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.facefit.R
import com.example.facefit.ui.theme.Black
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.FaceFitTheme
import com.example.facefit.ui.theme.Gray600


class CheckoutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceFitTheme {
                CheckoutScreen(
                    onBackClick = { finish() }
                )
            }
        }
    }
}


@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {

    var isEditingAddress by remember { mutableStateOf(false) }
    var streetName by remember { mutableStateOf("123 Main Street") }
    var buildingNameNo by remember { mutableStateOf("Apt 33") }
    var floorApartmentVillaNo by remember { mutableStateOf("3rd Floor") }
    var cityArea by remember { mutableStateOf("6th of October, Giza") }

    val fullAddress = remember(streetName, buildingNameNo, floorApartmentVillaNo, cityArea) {
        listOf(streetName, buildingNameNo, floorApartmentVillaNo, cityArea)
            .filter { it.isNotBlank() }
            .joinToString(separator = ", ")
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
                onClick = { /* TODO: Implement Place Order logic here */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue1),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text("Place Order", color = Color.White, fontSize = 18.sp)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F6F7))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.eye_glasses),
                            contentDescription = "Browline Glasses",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Browline Glasses",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Black,
                                )
                            )
                            Text(
                                text = "Color: Black",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = Color(0xFF4D5159)
                                )
                            )
                            Text(
                                text = "Lens: Standard",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = Color(0xFF4D5159)
                                )
                            )
                            Text(
                                text = "Quantity: 1",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = Color(0xFF4D5159)
                                )
                            )
                        }
                        Text(
                            text = "EGP 120",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Black,
                            )
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

                        IconButton(onClick = { isEditingAddress = !isEditingAddress }) {
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
                            value = streetName,
                            isEditing = true,
                            onValueChange = { streetName = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        EditableInfoItem(
                            icon = Icons.Default.Home,
                            label = "Building name/no",
                            value = buildingNameNo,
                            isEditing = true,
                            onValueChange = { buildingNameNo = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        EditableInfoItem(
                            icon = Icons.Default.Place,
                            label = "Floor, apartment, or villa no.",
                            value = floorApartmentVillaNo,
                            isEditing = true,
                            onValueChange = { floorApartmentVillaNo = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        EditableInfoItem(
                            icon = Icons.Default.Place,
                            label = "City/Area",
                            value = cityArea,
                            isEditing = true,
                            onValueChange = { cityArea = it }
                        )

                    } else {

                        Column {
                            Text(
                                text = fullAddress,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = Color(0xFF4D5159)
                                )
                            )
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
                            text = "EGP 120",
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
                            text = "EGP 50",
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
                            text = "Tax",
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = Black,
                            )
                        )
                        Text(
                            text = "EGP 50",
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = Black,
                            )
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE0E0E0))
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
                            text = "EGP 220",
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
    }
}

@Preview(showBackground = true)
@Composable
fun CheckoutScreenPreview() {
    FaceFitTheme {
        CheckoutScreen(onBackClick = {})
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
