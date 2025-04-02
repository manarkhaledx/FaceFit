package com.example.facefit.ui.presentation.screens.prescription

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.facefit.R
import com.example.facefit.ui.presentation.screens.cart.ShoppingCartActivity
import com.example.facefit.ui.theme.Black
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.FaceFitTheme
import com.example.facefit.ui.theme.Gray100
import com.example.facefit.ui.theme.Gray200
import com.example.facefit.ui.theme.LavenderBlue

class PrescriptionLensActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceFitTheme {
                LensPrescriptionFlow(
                    onNavigateToCart = {
                        val intent = Intent(this, ShoppingCartActivity::class.java)
                        startActivity(intent)
                    },
                    onClose = { finish() } // Close the activity and go back to ProductDetailsActivity
                    // Same as close if on first step
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LensPrescriptionFlowPreview() {
    FaceFitTheme {
        LensPrescriptionFlow(
            onNavigateToCart = {

            },
            onClose = {  } // Close the activity and go back to ProductDetailsActivity
            // Same as close if on first step
        )
    }
}

@Composable
fun LensPrescriptionFlow(
    onNavigateToCart: () -> Unit,
    onClose: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray100)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                Icon(
                    painter = painterResource(id = R.drawable.arrow_left), // Replace with your back icon
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            if (currentStep > 1) {
                                currentStep--
                            } else {
                                onClose()
                            }
                        }
                )

                // Step Indicator
                Box(
                    modifier = Modifier
                        .weight(1f) // Ensures it takes proportional space
                        .wrapContentWidth(Alignment.CenterHorizontally) // Centers content within the available space
                ) {
                    StepIndicator(currentStep = currentStep, totalSteps = 4)
                }

                // Close Button
                Icon(
                    painter = painterResource(id = R.drawable.close), // Replace with your close icon
                    contentDescription = "Close",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onClose() }
                )
            }


            Spacer(modifier = Modifier.height(16.dp))

            when (currentStep) {
                1 -> PrescriptionTypeScreen(onNext = { currentStep++ })
                2 -> EnterPrescriptionScreen()
                3 -> LensTypeScreen(onNext = { currentStep++ })
                4 -> LensMaterialScreen(onComplete = onNavigateToCart)
            }
        }

        if (currentStep > 1) {
            BottomNavigationBar(
                onNext = {
                    if (currentStep < 4) currentStep++
                },
                onNavigateToCart = onNavigateToCart,
                currentStep = currentStep,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}





@Composable
fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        (1..totalSteps).forEach { step ->
            Box(
                modifier = Modifier
                    .size(24.dp) // Fixed circle size
                    .background(
                        Color.Transparent,
                        shape = CircleShape
                    ) // Circle shape without hardcoded color
            ) {
                // Display the correct image based on the current step
                Image(
                    painter = painterResource(
                        id = when {
                            step < currentStep -> R.drawable.completed_state
                            step == currentStep -> R.drawable.active_state
                            else -> R.drawable.inactive_state
                        }
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp) // Ensure the image fills the circle
                        .align(Alignment.Center)
                )
            }
            if (step < totalSteps) {
                Spacer(
                    modifier = Modifier
                        .height(2.dp)
                        .width(30.dp)
                        .align(Alignment.CenterVertically)
                        .background(Gray200) // Neutral separator
                )
            }
        }
    }
}




@Composable
fun EnterPrescriptionScreen() {
    var odSph by remember { mutableStateOf("") }
    var odCyl by remember { mutableStateOf("") }
    var odAxis by remember { mutableStateOf("") }
    var osSph by remember { mutableStateOf("") }
    var osCyl by remember { mutableStateOf("") }
    var osAxis by remember { mutableStateOf("") }
    var pdValue by remember { mutableStateOf("") }
    var isSinglePD by remember { mutableStateOf(true) }
    var leftPD by remember { mutableStateOf("") }
    var rightPD by remember { mutableStateOf("") }
    var isSavePrescriptionChecked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray100)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Enter Your Prescription",
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight(700),
                color = Color(0xFF151616),
                letterSpacing = 1.sp,
            ),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // "Apply Pre-Saved Prescription" Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .border(
                    width = 1.dp,
                    color = Color(0xFFD4D8DF),
                    shape = RoundedCornerShape(size = 8.dp)
                )
                .background(color = Color(0xFFFAFBFC), shape = RoundedCornerShape(size = 8.dp))
                .clickable { /* Handle click */ }
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(LavenderBlue, CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.perscription_icon),
                    contentDescription = "Apply pre-saved prescription",
                    tint = Blue1,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Apply pre-saved prescription",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF151616),
                    letterSpacing = 0.8.sp,
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Prescription Fields
        SectionTitle("OD (Right Eye)")
        PrescriptionField(
            sphValue = odSph,
            onSphChange = { odSph = it },
            cylValue = odCyl,
            onCylChange = { odCyl = it },
            axisValue = odAxis,
            onAxisChange = { odAxis = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("OS (Left Eye)")
        PrescriptionField(
            sphValue = osSph,
            onSphChange = { osSph = it },
            cylValue = osCyl,
            onCylChange = { osCyl = it },
            axisValue = osAxis,
            onAxisChange = { osAxis = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Pupillary Distance",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight(600),
                color = Color(0xFF151616),
                letterSpacing = 0.9.sp,
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = isSinglePD,
                onClick = { isSinglePD = true },
                colors = RadioButtonDefaults.colors(selectedColor = Blue1)
            )
            Text(
                text = "One Number",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF151616),
                    letterSpacing = 0.8.sp,
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = !isSinglePD,
                onClick = { isSinglePD = false },
                colors = RadioButtonDefaults.colors(selectedColor = Blue1)
            )
            Text(
                text = "Two Numbers",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF151616),
                    letterSpacing = 0.8.sp,
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isSinglePD) {
            OutlinedTextField(
                value = pdValue,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.toFloatOrNull() != null) {
                        pdValue = newValue
                    }
                },
                label = { Text("PD", color = Blue1) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Blue1,
                    unfocusedBorderColor = Blue1,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = Blue1
                )
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = leftPD,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.toFloatOrNull() != null) {
                            leftPD = newValue
                        }
                    },
                    label = { Text("Left PD", color = Blue1) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue1,
                        unfocusedBorderColor = Blue1,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Blue1
                    )
                )
                OutlinedTextField(
                    value = rightPD,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.toFloatOrNull() != null) {
                            rightPD = newValue
                        }
                    },
                    label = { Text("Right PD", color = Blue1) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue1,
                        unfocusedBorderColor = Blue1,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Blue1
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Prism Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Prism ",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFF151616),
                    letterSpacing = 0.9.sp,
                )
            )
            Text(
                text = "(If Included)",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight(600),
                    color = Color.Gray,
                    letterSpacing = 0.9.sp,
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { /* Handle Prism add */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Blue1
                ),
                border = BorderStroke(1.dp, Blue1),
            ) {
                Text(
                    text = "Add",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight(500),
                        color = Blue1,
                    )
                )
            }
        }

        // Save Prescription Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSavePrescriptionChecked,
                onCheckedChange = { isSavePrescriptionChecked = it },
                colors = CheckboxDefaults.colors(checkedColor = Blue1)
            )
            Text(
                text = "Save my prescription",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF151616),
                    letterSpacing = 0.8.sp,
                ),
            )
        }


    }
}

@Composable
fun SectionTitle(title: String) {
    Row(
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        Text(
            text = title.split(" (")[0],
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight(600),
                color = Color.Black,
                letterSpacing = 0.9.sp,
            )
        )
        title.split(" (").getOrNull(1)?.let {
            Text(
                text = "($it",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight(600),
                    color = Color.Gray,
                    letterSpacing = 0.9.sp,
                )
            )
        }
    }
}

fun String.isValidSph(): Boolean {
    val value = this.toFloatOrNull() ?: return false
    return value in -20.0f..20.0f && this.matches(Regex("^-?\\d+(\\.\\d{1,2})?$"))
}

fun String.isValidCyl(): Boolean {
    val value = this.toFloatOrNull() ?: return false
    return value in -6.0f..6.0f && this.matches(Regex("^-?\\d+(\\.\\d{1,2})?$"))
}

fun String.isValidAxis(): Boolean {
    val value = this.toIntOrNull() ?: return false
    return value in 1..180 && this.matches(Regex("^\\d{1,3}$"))
}

fun String.isValidPD(): Boolean {
    val value = this.toFloatOrNull() ?: return false
    return value in 20.0f..80.0f && this.matches(Regex("^\\d{2}(\\.\\d{1,2})?$"))
}

@Composable
fun PrescriptionField(
    sphValue: String,
    onSphChange: (String) -> Unit,
    cylValue: String,
    onCylChange: (String) -> Unit,
    axisValue: String,
    onAxisChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = sphValue,
            onValueChange = { if (it.isValidSph() || it.isEmpty()) onSphChange(it) },
            label = { Text("SPH") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Blue1,
                unfocusedBorderColor = Blue1
            )
        )
        OutlinedTextField(
            value = cylValue,
            onValueChange = { if (it.isValidCyl() || it.isEmpty()) onCylChange(it) },
            label = { Text("CYL") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Blue1,
                unfocusedBorderColor = Blue1
            )
        )
        OutlinedTextField(
            value = axisValue,
            onValueChange = { if (it.isValidAxis() || it.isEmpty()) onAxisChange(it) },
            label = { Text("Axis") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Blue1,
                unfocusedBorderColor = Blue1
            )
        )
    }
}

@Composable
fun BottomNavigationBar(
    onNext: () -> Unit,
    onNavigateToCart: () -> Unit,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Browline glasses",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight(400),
                        color = Black,
                        letterSpacing = 0.8.sp,
                    )
                )
                Text(
                    text = "EGP 150",
                    style = TextStyle(
                        fontWeight = FontWeight(700),
                        color = Black,
                        letterSpacing = 1.sp,
                    )
                )
            }

            Button(
                onClick = {
                    if (currentStep == 4) {
                        onNavigateToCart()
                    } else {
                        onNext()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue1,
                    contentColor = Blue1
                ),
                modifier = Modifier
                    .width(190.dp)
                    .height(42.dp)
            ) {
                Text(
                    text = when (currentStep) {
                        2 -> "Submit"
                        3 -> "Next"
                        4 -> "Add To Cart"
                        else -> "Submit"
                    },
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight(500),
                        color = Color.White,
                    )
                )
            }
        }
    }
}



@Composable
fun LensTypeScreen(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Lens Type",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OptionItem(title = "Standard Eyeglass Lenses", price = "EGP 50", onClick = onNext)

        Spacer(modifier = Modifier.height(16.dp))

        OptionItem(title = "Blue Light Blocking", price = "EGP 50", onClick = onNext)

        Spacer(modifier = Modifier.height(16.dp))

        OptionItem(title = "Driving Lenses", price = "EGP 50", onClick = onNext)
    }
}


@Composable
fun LensMaterialScreen(onComplete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Lens Material",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OptionItem(title = "1.57 Mid-Index", price = "Free", onClick = onComplete)

        Spacer(modifier = Modifier.height(16.dp))
        OptionItem(title = "1.61 High Index", price = "EGP 50", description = "• Property 1\n• Property 2", onClick = onComplete)

        Spacer(modifier = Modifier.height(16.dp))
        OptionItem(title = "1.67 High Index", price = "EGP 75", description = "• Property 1\n• Property 2\n• Property 3", onClick = onComplete)

        Spacer(modifier = Modifier.height(16.dp))
        OptionItem(title = "1.74 High Index", price = "EGP 100", description = "• Property 1\n• Property 2\n• Property 3\n• Property 4", onClick = onComplete)
    }
}

@Composable
fun PrescriptionTypeScreen(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Gray100),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Prescription Type",
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight(700),
                color = Black,
                letterSpacing = 1.sp,
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        OptionItem(
            title = "Single Vision",
            description = "Most common prescription lenses, used for near, intermediate or distance",
            onClick = { onNext() }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OptionItem(
            title = "Non-Prescription",
            description = "Lens without any prescription",
            onClick = { onNext() }
        )
    }
}

@Composable
fun OptionItem(title: String, price: String? = null, description: String? = null, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White) // Set the background color to white
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
                price?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }


        }
    }
}

