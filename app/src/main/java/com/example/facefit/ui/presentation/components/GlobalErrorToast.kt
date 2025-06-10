package com.example.facefit.ui.presentation.components

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

@Composable
fun GlobalErrorToast(errorMessage: String?, trigger: Int) {
    val context = LocalContext.current

    LaunchedEffect(trigger) {
        errorMessage?.let {
            val message = if (it.contains("Unable to resolve host", ignoreCase = true)) {
                "Please check your internet connection"
            } else {
                it
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}


