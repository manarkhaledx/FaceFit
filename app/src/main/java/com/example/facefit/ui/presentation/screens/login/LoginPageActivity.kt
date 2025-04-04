package com.example.facefit.ui.presentation.screens.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.facefit.R
import com.example.facefit.ui.presentation.components.buttons.LongButton
import com.example.facefit.ui.presentation.components.textfields.EmailField
import com.example.facefit.ui.presentation.components.textfields.PasswordField
import com.example.facefit.ui.presentation.screens.home.HomePageActivity
import com.example.facefit.ui.presentation.screens.signUp.SignUpPage
import com.example.facefit.ui.theme.FaceFitTheme

class LoginPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceFitTheme {
                LoginScreen(
                    onItemClick = {
                        val intent = Intent(this, SignUpPage::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onSignInClick = {
                        val intent = Intent(this, HomePageActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(onItemClick: () -> Unit, onSignInClick: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(start = 24.dp, end = 24.dp, top = 82.dp)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        LoginHeader(isKeyboardVisible)

        Spacer(modifier = Modifier.height(24.dp))

        EmailField(
            email = email,
            onEmailChange = { email = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordField(
            password = password,
            onPasswordChange = { password = it },
            passwordVisible = passwordVisible,
            onPasswordVisibilityChange = { passwordVisible = !passwordVisible }
        )

        Spacer(modifier = Modifier.height(24.dp))

LongButton(
    text = "Sign In",
    onClick = onSignInClick
)

        Spacer(modifier = Modifier.height(16.dp))

        if (!isKeyboardVisible) {
            AdditionalOptions(onItemClick = onItemClick)
        }
    }
}

@Composable
fun LoginHeader(isKeyboardVisible: Boolean) {
    val logoFontSize = if (isKeyboardVisible) 32.sp else 48.sp
    val logoBottomPadding = if (isKeyboardVisible) 8.dp else 16.dp

    Text(
        text = "FaceFit",
        fontSize = logoFontSize,
        color = Color.Black,
        modifier = Modifier.padding(bottom = logoBottomPadding)
    )

    if (!isKeyboardVisible) {
        Text(
            text = "Welcome!",
            fontSize = 24.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Please enter your data",
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}





//@Composable
//fun SignButton(btnName: String, onSignInClick: () -> Unit) {
//    Button(
//        onClick = onSignInClick,
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(56.dp),
//        colors = ButtonDefaults.buttonColors(
//            containerColor = Blue1,
//            contentColor = Color.White
//        ),
//        shape = RoundedCornerShape(size = 30.dp)
//    ) {
//        Text(
//            text = btnName,
//            fontSize = 16.sp,
//            color = Color.White
//        )
//    }
//}

@Composable
fun AdditionalOptions(onItemClick: () -> Unit) {
    Text(
        text = "New to App? Create account",
        fontSize = 14.sp,
        color = Color.Gray,
        modifier = Modifier.clickable { onItemClick() }
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "or login with",
        fontSize = 14.sp,
        color = Color.Gray
    )

    Spacer(modifier = Modifier.height(16.dp))

    SocialLoginButtons()
}

@Composable
fun SocialLoginButtons() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = { /* Handle X Login */ }) {
            Icon(
                painter = painterResource(id = R.drawable.x),
                contentDescription = "X Login",
                modifier = Modifier.size(48.dp),
                tint = Color.Unspecified
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        IconButton(onClick = { /* Handle Facebook Login */ }) {
            Icon(
                painter = painterResource(id = R.drawable.facebook),
                contentDescription = "Facebook Login",
                modifier = Modifier.size(48.dp),
                tint = Color.Unspecified
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        IconButton(onClick = { /* Handle Google Login */ }) {
            Icon(
                painter = painterResource(id = R.drawable.google),
                contentDescription = "Google Login",
                modifier = Modifier.size(48.dp),
                tint = Color.Unspecified
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    FaceFitTheme {
        LoginScreen(
            onItemClick = { /* No action needed for preview */ },
            onSignInClick = { /* No action needed for preview */ }
        )
    }
}
