package com.example.facefit.ui.presentation.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.facefit.ui.presentation.screens.reviews.CustomersReviewsScreen
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.FaceFitTheme

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