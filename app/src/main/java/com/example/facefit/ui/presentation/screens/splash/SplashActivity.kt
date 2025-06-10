package com.example.facefit.ui.presentation.screens.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.facefit.ui.presentation.screens.auth.login.LoginPage
import com.example.facefit.ui.theme.FaceFitTheme

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceFitTheme {
                SplashScreen {
                    startActivity(Intent(this@SplashActivity, LoginPage::class.java))
                    finish()
                }
            }
        }
    }
}