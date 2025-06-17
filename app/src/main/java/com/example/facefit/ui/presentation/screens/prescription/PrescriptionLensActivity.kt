package com.example.facefit.ui.presentation.screens.prescription

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.rememberRipple // Import rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.facefit.R
import com.example.facefit.domain.models.Glasses
import com.example.facefit.domain.utils.NetworkUtils
import com.example.facefit.domain.utils.Resource
import com.example.facefit.ui.presentation.screens.cart.ShoppingCartActivity
import com.example.facefit.ui.theme.Black
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.FaceFitTheme
import com.example.facefit.ui.theme.Gray100
import com.example.facefit.ui.theme.Gray200
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.core.view.WindowCompat
import com.example.facefit.ui.presentation.screens.cart.CartViewModel
import androidx.compose.ui.composed // Import composed
import androidx.compose.ui.semantics.Role // Import Role
import androidx.compose.foundation.interaction.MutableInteractionSource // Import MutableInteractionSource
import androidx.compose.material3.AlertDialog
import kotlinx.coroutines.delay // Import delay for debouncing

@AndroidEntryPoint
class PrescriptionLensActivity : ComponentActivity() {
    private val viewModel: PrescriptionLensViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            val productId = intent.getStringExtra("productId") ?: ""
            val color = intent.getStringExtra("color") ?: ""
            cartViewModel.setSelectedColor(color)

            FaceFitTheme {
                LensPrescriptionFlow(
                    viewModel = viewModel,
                    productId = productId,
                    color = color,
                    onClose = { finish() },
                    activityContext = this
                )
            }
        }
    }
}

@Composable
fun LensPrescriptionFlow(
    viewModel: PrescriptionLensViewModel,
    productId: String,
    color: String,
    onClose: () -> Unit,
    activityContext: ComponentActivity
) {
    var currentStep by remember { mutableIntStateOf(1) }
    var selectedLensType by remember { mutableStateOf("") }
    var prescriptionId by remember { mutableStateOf<String?>(null) }
    var lastSelectedLensOptionIsPrescription by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Local loading state for this composable's direct control over UI elements
    var isLocalLoading by remember { mutableStateOf(false) }


    DisposableEffect(currentStep) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Ensure back button is disabled during local loading for step 2
                if (isLocalLoading && currentStep == 2) return

                if (currentStep == 3 && !lastSelectedLensOptionIsPrescription) {
                    currentStep = 1
                    selectedLensType = ""
                    prescriptionId = null
                    viewModel.resetPrescriptionState()
                    lastSelectedLensOptionIsPrescription = false
                } else if (currentStep > 1) {
                    currentStep--
                    if (currentStep == 1) {
                        selectedLensType = ""
                        prescriptionId = null
                        viewModel.resetPrescriptionState()
                        lastSelectedLensOptionIsPrescription = false
                    }
                } else {
                    onClose()
                }
            }
        }
        activityContext.onBackPressedDispatcher.addCallback(callback)
        onDispose {
            callback.remove()
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray100)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(WindowInsets.systemBars.asPaddingValues())
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_left),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(24.dp)
                        // Use isLocalLoading to disable back button during prescription save
                        .clickable(enabled = !isLocalLoading) {
                            if (currentStep == 3 && !lastSelectedLensOptionIsPrescription) {
                                currentStep = 1
                                selectedLensType = ""
                                prescriptionId = null
                                viewModel.resetPrescriptionState()
                                lastSelectedLensOptionIsPrescription = false
                            } else if (currentStep > 1) {
                                currentStep--
                                if (currentStep == 1) {
                                    selectedLensType = ""
                                    prescriptionId = null
                                    viewModel.resetPrescriptionState()
                                    lastSelectedLensOptionIsPrescription = false
                                }
                            } else {
                                onClose()
                            }
                        }
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                ) {
                    StepIndicator(currentStep = currentStep, totalSteps = 3)
                }

                Icon(
                    painter = painterResource(id = R.drawable.close),
                    contentDescription = "Close",
                    modifier = Modifier
                        .size(24.dp)
                        // Use isLocalLoading to disable close button during prescription save
                        .clickable(enabled = !isLocalLoading) { onClose() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (currentStep) {
                1 -> PrescriptionTypeScreen(
                    onNext = { lensType ->
                        selectedLensType = lensType
                        lastSelectedLensOptionIsPrescription = (lensType == "Prescription")
                        currentStep = if (lensType == "Prescription") 2 else 3
                    },
                    productId = productId,
                    color = color
                )

                2 -> EnterPrescriptionScreen(
                    viewModel = viewModel,
                    onSavePrescription = {
                        coroutineScope.launch {
                            if (viewModel.validate()) {
                                isLocalLoading = true // Show loading for prescription save
                                if (NetworkUtils.isNetworkAvailable(activityContext)) {
                                    viewModel.createPrescription { id ->
                                        prescriptionId = id
                                        currentStep = 3
                                        isLocalLoading = false // Hide loading
                                    }
                                } else {
                                    Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
                                    isLocalLoading = false // Hide loading
                                }
                            }
                        }
                    },
                    isLoading = isLocalLoading // Pass local loading state to EnterPrescriptionScreen
                )

                3 -> LensSpecificationScreen(
                    onNext = { lensSpecification ->
                        // This onNext is not actually used for navigation to the next step
                        // The navigation to cart is handled within LensSpecificationScreen
                    },
                    productId = productId,
                    color = color,
                    selectedLensType = selectedLensType,
                    prescriptionId = prescriptionId,
                    viewModel = viewModel
                )
            }
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
                    .size(24.dp)
                    .background(
                        Color.Transparent,
                        shape = CircleShape
                    )
            ) {
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
                        .size(24.dp)
                        .align(Alignment.Center)
                )
            }
            if (step < totalSteps) {
                Spacer(
                    modifier = Modifier
                        .height(2.dp)
                        .width(30.dp)
                        .align(Alignment.CenterVertically)
                        .background(Gray200)
                )
            }
        }
    }
}


@Composable
fun EnterPrescriptionScreen(
    viewModel: PrescriptionLensViewModel,
    onSavePrescription: () -> Unit,
    isLoading: Boolean = false // Added isLoading parameter for the button
) {
    val state = viewModel.prescriptionState
    val errors = viewModel.fieldErrors
    val isSinglePD = viewModel.isSinglePD

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

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("OD (Right Eye)")
        PrescriptionField(
            sphValue = state.odSph,
            onSphChange = { viewModel.updateField(PrescriptionField.OD_SPH, it) },
            cylValue = state.odCyl,
            onCylChange = { viewModel.updateField(PrescriptionField.OD_CYL, it) },
            axisValue = state.odAxis,
            onAxisChange = { viewModel.updateField(PrescriptionField.OD_AXIS, it) },
            errorSph = errors[PrescriptionField.OD_SPH],
            errorCyl = errors[PrescriptionField.OD_CYL],
            errorAxis = errors[PrescriptionField.OD_AXIS],
            enabled = !isLoading // Disable fields when loading
        )
        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("OS (Left Eye)")
        PrescriptionField(
            sphValue = state.osSph,
            onSphChange = { viewModel.updateField(PrescriptionField.OS_SPH, it) },
            cylValue = state.osCyl,
            onCylChange = { viewModel.updateField(PrescriptionField.OS_CYL, it) },
            axisValue = state.osAxis,
            onAxisChange = { viewModel.updateField(PrescriptionField.OS_AXIS, it) },
            errorSph = errors[PrescriptionField.OS_SPH],
            errorCyl = errors[PrescriptionField.OS_CYL],
            errorAxis = errors[PrescriptionField.OS_AXIS],
            enabled = !isLoading // Disable fields when loading
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
                onClick = { viewModel.setSinglePD(true) },
                colors = RadioButtonDefaults.colors(selectedColor = Blue1),
                enabled = !isLoading // Disable radio buttons when loading
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
                onClick = { viewModel.setSinglePD(false) },
                colors = RadioButtonDefaults.colors(selectedColor = Blue1),
                enabled = !isLoading // Disable radio buttons when loading
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
                value = state.singlePD,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d{0,2}(\\.\\d{0,2})?$"))) {
                        viewModel.updateField(PrescriptionField.SINGLE_PD, newValue)
                    }
                },
                isError = errors[PrescriptionField.SINGLE_PD] != null,
                supportingText = {
                    errors[PrescriptionField.SINGLE_PD]?.let {
                        Text(text = it, color = Color.Red)
                    }
                },
                label = {
                    Text(
                        text = "PD",
                        color = if (errors[PrescriptionField.SINGLE_PD] != null) Color.Red else Blue1
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (errors[PrescriptionField.SINGLE_PD] != null) Color.Red else Blue1,
                    unfocusedBorderColor = if (errors[PrescriptionField.SINGLE_PD] != null) Color.Red else Blue1,
                    cursorColor = Blue1
                ),
                enabled = !isLoading // Disable text field when loading
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = state.leftPD,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d{0,2}(\\.\\d{0,2})?$"))) {
                            viewModel.updateField(PrescriptionField.LEFT_PD, newValue)
                        }


                    },
                    isError = errors[PrescriptionField.LEFT_PD] != null,
                    supportingText = {
                        errors[PrescriptionField.LEFT_PD]?.let {
                            Text(text = it, color = Color.Red)
                        }
                    },
                    label = {
                        Text(
                            "Left PD",
                            color = if (errors[PrescriptionField.LEFT_PD] != null) Color.Red else Blue1
                        )
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (errors[PrescriptionField.LEFT_PD] != null) Color.Red else Blue1,
                        unfocusedBorderColor = if (errors[PrescriptionField.LEFT_PD] != null) Color.Red else Blue1,
                        cursorColor = Blue1
                    ),
                    enabled = !isLoading // Disable text field when loading
                )
                OutlinedTextField(
                    value = state.rightPD,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d{0,2}(\\.\\d{0,2})?$"))) {
                            viewModel.updateField(PrescriptionField.RIGHT_PD, newValue)
                        }


                    },
                    isError = errors[PrescriptionField.RIGHT_PD] != null,
                    supportingText = {
                        errors[PrescriptionField.RIGHT_PD]?.let {
                            Text(text = it, color = Color.Red)
                        }
                    },
                    label = {
                        Text(
                            "Right PD",
                            color = if (errors[PrescriptionField.RIGHT_PD] != null) Color.Red else Blue1
                        )
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (errors[PrescriptionField.RIGHT_PD] != null) Color.Red else Blue1,
                        unfocusedBorderColor = if (errors[PrescriptionField.RIGHT_PD] != null) Color.Red else Blue1,
                        cursorColor = Blue1
                    ),
                    enabled = !isLoading // Disable text field when loading
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSavePrescription,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue1),
            enabled = !isLoading // Disable button when loading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Save Prescription",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
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
                color = Black,
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


@Composable
fun PrescriptionField(
    sphValue: String,
    onSphChange: (String) -> Unit,
    cylValue: String,
    onCylChange: (String) -> Unit,
    axisValue: String,
    onAxisChange: (String) -> Unit,
    errorSph: String? = null,
    errorCyl: String? = null,
    errorAxis: String? = null,
    enabled: Boolean = true // Added enabled parameter
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = sphValue,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^-?\\d{0,2}(\\.\\d{0,2})?$"))) {
                    onSphChange(newValue)
                }
            },
            label = { Text("SPH") },
            isError = errorSph != null,
            supportingText = { if (errorSph != null) Text(errorSph, color = Color.Red) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Blue1,
                unfocusedBorderColor = Blue1
            ),
            enabled = enabled // Apply enabled state
        )
        OutlinedTextField(
            value = cylValue,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^-?\\d{0,2}(\\.\\d{0,2})?$"))) {
                    onCylChange(newValue)
                }
            },
            label = { Text("CYL") },
            isError = errorCyl != null,
            supportingText = { if (errorCyl != null) Text(errorCyl, color = Color.Red) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Blue1,
                unfocusedBorderColor = Blue1
            ),
            enabled = enabled // Apply enabled state
        )
        OutlinedTextField(
            value = axisValue,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d{0,3}$"))) {
                    onAxisChange(newValue)
                }
            },
            label = { Text("AXIS") },
            isError = errorAxis != null,
            supportingText = { if (errorAxis != null) Text(errorAxis, color = Color.Red) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Blue1,
                unfocusedBorderColor = Blue1
            ),
            enabled = enabled // Apply enabled state
        )
    }
}


@Composable
fun BottomNavigationBar(
    onNext: () -> Unit,
    currentStep: Int,
    modifier: Modifier = Modifier,
    productId: String,
    color: String,
    viewModel: PrescriptionLensViewModel = hiltViewModel()
) {
    val productDetails by produceState<Resource<Glasses>>(
        initialValue = Resource.Loading(),
        key1 = productId
    ) {
        value = viewModel.getGlassesById(productId)
    }

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
                when (productDetails) {
                    is Resource.Success -> {
                        val glasses = (productDetails as Resource.Success<Glasses>).data
                        Text(
                            text = glasses?.name ?: "Unknown Product",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight(400),
                                color = Black,
                                letterSpacing = 0.8.sp,
                            )
                        )
                        Text(
                            text = "EGP ${glasses?.price ?: 0}",
                            style = TextStyle(
                                fontWeight = FontWeight(700),
                                color = Black,
                                letterSpacing = 1.sp,
                            )
                        )
                    }

                    else -> {
                        Text("Loading...", color = Black)
                    }
                }
            }

            Button(
                onClick = onNext,
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
                        3 -> "Add To Cart"
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
fun LensSpecificationScreen(
    onNext: (lensSpecification: String) -> Unit,
    productId: String,
    color: String,
    selectedLensType: String,
    prescriptionId: String?,
    viewModel: PrescriptionLensViewModel = hiltViewModel(),
) {
    // Local loading state specific to this screen and its options
    var isScreenLoading by remember { mutableStateOf(false) }
    var selectedOptionDuringLoading by remember { mutableStateOf<String?>(null) }
    var showStockErrorDialog by remember { mutableStateOf(false) }
    var stockErrorMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Select Lens Specification",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val options = listOf(
            LensOptionData(LensOptions.STANDARD, "EGP 50", "Basic lenses for everyday use"),
            LensOptionData(LensOptions.BLUE_LIGHT, "EGP 50", "Filters harmful blue light from screens"),
            LensOptionData(LensOptions.DRIVING, "EGP 50", "Anti-glare coating for night driving")
        )

        options.forEach { option ->
            OptionItem(
                title = option.title,
                price = option.price,
                description = option.description,
                onClick = {
                    if (!isScreenLoading) {
                        isScreenLoading = true
                        selectedOptionDuringLoading = option.title
                        coroutineScope.launch {
                            if (NetworkUtils.isNetworkAvailable(context)) {
                                viewModel.addToCart(
                                    productId = productId,
                                    color = color,
                                    lensType = selectedLensType,
                                    size = "standard",
                                    lensSpecification = option.title,
                                    prescriptionId = prescriptionId,
                                    onComplete = { result ->
                                        isScreenLoading = false
                                        selectedOptionDuringLoading = null
                                        when (result) {
                                            is Resource.Success -> {
                                                val intent = Intent(context, ShoppingCartActivity::class.java).apply {
                                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                                }
                                                context.startActivity(intent)
                                                (context as? ComponentActivity)?.finish()
                                            }
                                            is Resource.Error -> {
                                                val errorMessage = result.message ?: "Failed to add to cart"
                                                if (errorMessage.contains("Insufficient stock")) {
                                                    // Extract the stock error message
                                                    stockErrorMessage = errorMessage.substringAfter("error\":\"").substringBefore("\"")
                                                    showStockErrorDialog = true
                                                } else {
                                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            is Resource.Loading -> { /* Not used here */ }
                                        }
                                    }
                                )
                            } else {
                                Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
                                isScreenLoading = false
                                selectedOptionDuringLoading = null
                            }
                        }
                    }
                },
                isLoading = isScreenLoading && selectedOptionDuringLoading == option.title
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Stock error dialog
    if (showStockErrorDialog) {
        AlertDialog(
            onDismissRequest = { showStockErrorDialog = false },
            title = { Text("Item Availability") },
            text = {
                Column {
                    Text("We couldn't add this item to your cart because:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• The item quantity exceeds available stock.",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("What would you like to do?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStockErrorDialog = false
                        // Optionally refresh data or navigate to cart
                        val intent = Intent(context, ShoppingCartActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        context.startActivity(intent)
                        (context as? ComponentActivity)?.finish()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue1)
                ) {
                    Text("View Cart")
                }
            }
        )
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
        OptionItem(
            title = "1.61 High Index",
            price = "EGP 50",
            description = "• Property 1\n• Property 2",
            onClick = onComplete
        )

        Spacer(modifier = Modifier.height(16.dp))
        OptionItem(
            title = "1.67 High Index",
            price = "EGP 75",
            description = "• Property 1\n• Property 2\n• Property 3",
            onClick = onComplete
        )

        Spacer(modifier = Modifier.height(16.dp))
        OptionItem(
            title = "1.74 High Index",
            price = "EGP 100",
            description = "• Property 1\n• Property 2\n• Property 3\n• Property 4",
            onClick = onComplete
        )
    }
}

@Composable
fun PrescriptionTypeScreen(
    onNext: (lensType: String) -> Unit,
    productId: String,
    color: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Gray100),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Select Lens Type",
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight(700),
                color = Black,
                letterSpacing = 1.sp,
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OptionItem(
            title = LensOptions.SINGLE_VISION,
            description = "Most common prescription lenses",
            onClick = { onNext("Prescription") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OptionItem(
            title = LensOptions.NON_PRESCRIPTION,
            description = "Lens without any prescription",
            onClick = { onNext("No-Prescription") }
        )
    }
}

@Composable
fun OptionItem(
    title: String,
    price: String? = null,
    description: String? = null,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    // Using rememberCoroutineScope to ensure the scope is tied to the composable's lifecycle
    val coroutineScope = rememberCoroutineScope()
    // Using a mutable state to prevent rapid re-launching of click handlers
    val clickedState = remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                // The enabled state of the clickable modifier.
                // It's false if isLoading is true (from parent state) OR if a click has just been registered locally.
                enabled = !isLoading && !clickedState.value,
                onClick = {
                    if (!isLoading && !clickedState.value) { // Double check
                        clickedState.value = true // Mark as clicked immediately
                        onClick() // Execute the actual click action

                        // Reset clickedState after a short delay, regardless of the outcome
                        coroutineScope.launch {
                            delay(500L) // Adjust this debounce time as needed (e.g., 500ms)
                            clickedState.value = false
                        }
                    }
                },
                // Provide a custom interactionSource to remove default ripple effect if needed
                // For this scenario, keeping the ripple but ensuring one click.
                // interactionSource = remember { MutableInteractionSource() },
                // indication = rememberRipple() // Keep default ripple
            )
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
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

            if (isLoading) { // This isLoading comes from the parent (LensSpecificationScreen's local state)
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = Blue1
                )
            }
        }
    }
}

data class LensOptionData(
    val title: String,
    val price: String,
    val description: String
)