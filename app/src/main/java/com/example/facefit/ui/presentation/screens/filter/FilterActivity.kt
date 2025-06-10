package com.example.facefit.ui.presentation.screens.filter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.facefit.R
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.FaceFitTheme

class FilterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceFitTheme {
                FilterScreenOverlay(
                    onDismiss = { finish() },
                    onApply = { gender, type, minPrice, maxPrice, shape, material -> },
                    currentFilters = intent.getSerializableExtra("CURRENT_FILTERS") as? Map<String, Any?>
                )
            }
        }
    }
}

@Composable
fun FilterScreenOverlay(
    onDismiss: () -> Unit,
    onApply: (
        gender: String?,
        type: String?,
        minPrice: Double?,
        maxPrice: Double?,
        shape: String?,
        material: String?
    ) -> Unit,
    currentFilters: Map<String, Any?>? = null
) {
    var showFilter by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { showFilter = true }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(3.dp)
                .clickable { onDismiss() }
        )

        AnimatedVisibility(
            visible = showFilter,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
                    .background(Color.White)
            ) {
                FilterScreen(
                    onClose = { showFilter = false; onDismiss() },
                    onApply = { gender, type, minPrice, maxPrice, shape, material ->
                        onApply(gender, type, minPrice, maxPrice, shape, material)
                        showFilter = false
                        onDismiss()
                    },
                    currentFilters = currentFilters
                )
            }
        }
    }
}

@Composable
fun FilterScreen(
    onClose: () -> Unit = {},
    onApply: (
        gender: String?,
        type: String?,
        minPrice: Double?,
        maxPrice: Double?,
        shape: String?,
        material: String?
    ) -> Unit = { _, _, _, _, _, _ -> },
    currentFilters: Map<String, Any?>? = null
) {
    val backgroundColor = Color(0xFFF5F5F5)
    val scrollState = rememberScrollState()

    // Initialize state with current filters
    var selectedGender by remember {
        mutableStateOf<String?>(currentFilters?.get("gender") as? String)
    }
    var selectedType by remember {
        mutableStateOf<String?>(currentFilters?.get("type") as? String)
    }
    var selectedPriceRange by remember {
        mutableStateOf<Pair<Double?, Double?>?>(
            (currentFilters?.get("minPrice") as? Double) to
                    (currentFilters?.get("maxPrice") as? Double)
        )
    }
    var selectedShape by remember {
        mutableStateOf<String?>(currentFilters?.get("shape") as? String)
    }
    var selectedMaterial by remember {
        mutableStateOf<String?>(currentFilters?.get("material") as? String)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Header(onClose)

        FilterCategory(stringResource(R.string.gender)) {
            FilterRow(
                items = listOf(
                    stringResource(R.string.men) to "Men",
                    stringResource(R.string.women) to "Women"
                ),
                selectedItem = selectedGender,
                onItemSelected = { gender ->
                    selectedGender = if (selectedGender == gender) null else gender
                }
            )
        }

        FilterCategory(stringResource(R.string.type)) {
            FilterRow(
                items = listOf(
                    stringResource(R.string.eyeglasses) to "eyeglasses",
                    stringResource(R.string.sunglasses) to "sunglasses"
                ),
                selectedItem = selectedType,
                onItemSelected = { type ->
                    selectedType = if (selectedType == type) null else type
                }
            )
        }

        FilterCategory(stringResource(R.string.price)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                var isPriceFilterDisabled by remember { mutableStateOf(false) }

                AnimatedVisibility(visible = !isPriceFilterDisabled) {
                    Column {
                        val priceRanges = listOf(
                            stringResource(R.string.price_under_100) to (null to 100.0),
                            stringResource(R.string.price_100_300) to (100.0 to 300.0),
                            stringResource(R.string.price_300_500) to (300.0 to 500.0),
                            stringResource(R.string.price_500_800) to (500.0 to 800.0),
                            stringResource(R.string.price_800_plus) to (800.0 to null)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            priceRanges.take(3).forEach { (label, range) ->
                                PriceButton(
                                    text = label,
                                    selected = selectedPriceRange == range && !isPriceFilterDisabled,
                                    onClick = {
                                        if (!isPriceFilterDisabled) {
                                            selectedPriceRange = if (selectedPriceRange == range) null else range
                                        }
                                    },
                                    enabled = !isPriceFilterDisabled,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            priceRanges.drop(3).forEach { (label, range) ->
                                PriceButton(
                                    text = label,
                                    selected = selectedPriceRange == range && !isPriceFilterDisabled,
                                    onClick = {
                                        if (!isPriceFilterDisabled) {
                                            selectedPriceRange = if (selectedPriceRange == range) null else range
                                        }
                                    },
                                    enabled = !isPriceFilterDisabled,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (priceRanges.size % 2 != 0) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        FilterCategory(stringResource(R.string.shape)) {
            FilterRow(
                items = listOf(
                    stringResource(R.string.rounded) to "Round",
                    stringResource(R.string.square) to "Square"
                ),
                selectedItem = selectedShape,
                onItemSelected = { shape ->
                    selectedShape = if (selectedShape == shape) null else shape
                }
            )
        }

        FilterCategory(stringResource(R.string.material)) {
            FilterRow(
                items = listOf(
                    stringResource(R.string.plastic) to "Plastic",
                    stringResource(R.string.metal) to "Metal"
                ),
                selectedItem = selectedMaterial,
                onItemSelected = { material ->
                    selectedMaterial = if (selectedMaterial == material) null else material
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onApply(
                    selectedGender,
                    selectedType,
                    selectedPriceRange?.first,
                    selectedPriceRange?.second,
                    selectedShape,
                    selectedMaterial
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue1),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(stringResource(R.string.apply_filters))
        }

        Spacer(modifier = Modifier.height(8.dp))

Button(
    onClick = {
        selectedGender = null
        selectedType = null
        selectedPriceRange = null
        selectedShape = null
        selectedMaterial = null
        onApply(null, null, null, null, null, null)
    },
    modifier = Modifier
        .fillMaxWidth()
        .height(48.dp),
    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
    shape = RoundedCornerShape(24.dp)
) {
    Text(stringResource(R.string.clear_filters))
}
    }
}

@Composable
fun Header(onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(R.string.filter), fontSize = 20.sp, fontWeight = FontWeight.Medium)
        IconButton(onClick = { onClose() }, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.close),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun FilterRow(
    items: List<Pair<String, String>>,
    selectedItem: String?,
    onItemSelected: (String) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (displayText, value) ->
            val isSelected = value == selectedItem

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Blue1 else Color.White)
                    .clickable {
                        onItemSelected(value)
                    }
                    .border(1.dp, if (isSelected) Blue1 else Color.LightGray, RoundedCornerShape(8.dp))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayText,
                    fontSize = 14.sp,
                    color = if (isSelected) Color.White else Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun FilterCategory(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
fun PriceButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    !enabled -> Color.LightGray.copy(alpha = 0.5f)
                    selected -> Blue1
                    else -> Color.White
                }
            )
            .clickable(enabled = enabled, onClick = onClick)
            .border(
                1.dp,
                when {
                    !enabled -> Color.Gray.copy(alpha = 0.3f)
                    selected -> Blue1
                    else -> Color.LightGray
                },
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = when {
                !enabled -> Color.Gray
                selected -> Color.White
                else -> Color.Black
            },
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FilterScreenPreview() {
    FaceFitTheme {

    }
}
