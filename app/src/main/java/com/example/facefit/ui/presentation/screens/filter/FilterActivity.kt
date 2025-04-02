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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.facefit.ui.theme.FaceFitTheme

class FilterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceFitTheme {
                FilterScreenOverlay(onDismiss = { finish() })
            }
        }
    }
}

@Composable
fun FilterScreenOverlay(onDismiss: () -> Unit) {
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
                FilterScreen(onClose = { showFilter = false; onDismiss() })
            }
        }
    }
}

@Composable
fun FilterScreen(onClose: () -> Unit = {}, onApply: () -> Unit = {}) {
    val backgroundColor = Color(0xFFF5F5F5)
    val primaryColor = Color(0xFF0000CC)
    val selectedColor = Color(0xFFB794F6)
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Header(onClose)
        FilterContent(selectedColor)
        Spacer(modifier = Modifier.height(16.dp))
        BottomButtons(onClose, onApply, primaryColor)
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
        Text(text = "Filter", fontSize = 20.sp, fontWeight = FontWeight.Medium)
        IconButton(onClick = { onClose() }, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun FilterContent(selectedColor: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        FilterCategory("Gender") {
            FilterRow(listOf("Men" to true, "Women" to false))
        }
        FilterCategory("Type") {
            FilterRow(
                listOf("Full rim" to true, "Semi-rimless" to false, "Rimless" to false),
                selectedColor
            )
        }
        FilterCategory("Lens Type") {
            FilterRow(
                listOf(
                    "Single Vision" to false,
                    "Single Vision" to true,
                    "Single Vision" to false
                ), selectedColor
            )
        }
        FilterCategory("Size") {
            FilterRow(
                listOf(
                    "Narrow\n<111mm" to false,
                    "Narrow\n<111mm" to false,
                    "Narrow\n<111mm" to false
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            FilterRow(listOf("Narrow\n<111mm" to false))
        }
        FilterCategory("Price") {
            FilterRow(listOf("0-333" to false, "0-333" to false, "0-333" to false))
            Spacer(modifier = Modifier.height(8.dp))
            FilterRow(listOf("0-333" to false, "0-333" to false, "0-333" to false))
        }
        FilterCategory("Shape") {
            ShapeRow(listOf("Rectangle" to false, "Rectangle" to false, "Rectangle" to false))
            Spacer(modifier = Modifier.height(8.dp))
            ShapeRow(listOf("Rectangle" to false))
        }
        FilterCategory("Material") {
            FilterRow(listOf("Plastic" to false, "Metal" to false, "Plastic" to false))
            Spacer(modifier = Modifier.height(8.dp))
            FilterRow(listOf("Metal" to false, "Metal" to false, "Metal" to false))
        }
    }
}

@Composable
fun FilterRow(items: List<Pair<String, Boolean>>, borderColor: Color? = null) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (text, isSelected) ->
            FilterChip(
                text = text,
                isSelected = isSelected,
                borderColor = borderColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ShapeRow(items: List<Pair<String, Boolean>>) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (text, isSelected) ->
            GlassesShapeChip(text = text, isSelected = isSelected, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun BottomButtons(onClose: () -> Unit, onApply: () -> Unit, primaryColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = { onClose() },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(24.dp)
        ) { Text("Clear") }
        Button(
            onClick = { onApply() },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            shape = RoundedCornerShape(24.dp)
        ) { Text("Apply") }
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
fun FilterChip(
    text: String,
    isSelected: Boolean,
    borderColor: Color? = null,
    modifier: Modifier = Modifier
) {
    var selected by remember { mutableStateOf(isSelected) }
    val backgroundColor = if (selected) Color.White else Color(0xFFE8E8E8)
    val borderModifier =
        borderColor?.let { Modifier.border(1.dp, it, RoundedCornerShape(8.dp)) } ?: Modifier

    Box(
        modifier = modifier
            .then(borderModifier)
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { selected = !selected }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.Black,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun GlassesShapeChip(text: String, isSelected: Boolean, modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf(isSelected) }
    val backgroundColor = if (selected) Color.White else Color(0xFFE8E8E8)

    Column(
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { selected = !selected }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Black))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.Black,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FilterScreenPreview() {
    FaceFitTheme { FilterScreenOverlay(onDismiss = {}) }
}