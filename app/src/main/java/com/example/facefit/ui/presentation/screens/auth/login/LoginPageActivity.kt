package com.example.facefit.ui.presentation.screens.auth.login

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.facefit.R
import com.example.facefit.data.local.TokenManager
import com.example.facefit.domain.utils.Resource
import com.example.facefit.ui.presentation.components.buttons.LongButton
import com.example.facefit.ui.presentation.components.textfields.EmailField
import com.example.facefit.ui.presentation.components.textfields.PasswordField
import com.example.facefit.ui.presentation.screens.home.HomePageActivity
import com.example.facefit.ui.presentation.screens.auth.signUp.SignUpPage
import com.example.facefit.ui.presentation.screens.splash.ShrinkOverlay
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.FaceFitTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginPage : ComponentActivity() {
    @Inject lateinit var tokenManager: TokenManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (tokenManager.getToken() != null) {
            startActivity(Intent(this, HomePageActivity::class.java))
            finish()
            return
        }
        enableEdgeToEdge()
        setContent {
            FaceFitTheme {
                val viewModel: LoginViewModel = hiltViewModel()
                val loginState by viewModel.loginState.collectAsStateWithLifecycle()
                val fieldErrors by viewModel.fieldErrors.collectAsStateWithLifecycle()
                val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

                LaunchedEffect(loginState) {
                    if (loginState is Resource.Success) {
                        startActivity(Intent(this@LoginPage, HomePageActivity::class.java))
                        finish()
                    }
                }


                LoginScreen(
                    onItemClick = {
                        startActivity(Intent(this@LoginPage, SignUpPage::class.java))
                        finish()
                    },
                    onSignInClick = { email, password ->
                        viewModel.login(email, password)
                    },
                    isLoading = loginState is Resource.Loading,
                    fieldErrors = fieldErrors,
                    errorMessage = errorMessage,
                    onFieldChanged = { field -> viewModel.clearFieldError(field) }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(
    onItemClick: () -> Unit,
    onSignInClick: (String, String) -> Unit,
    isLoading: Boolean,
    fieldErrors: Map<String, String>,
    errorMessage: String?,
    onFieldChanged: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    LaunchedEffect(email) { onFieldChanged("email") }
    LaunchedEffect(password) { onFieldChanged("password") }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null && fieldErrors.isEmpty()) {
            scope.launch {
                snackbarHostState.showSnackbar(errorMessage)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(start = 24.dp, end = 24.dp, top = 82.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            LoginHeader(isKeyboardVisible)

            Spacer(modifier = Modifier.height(24.dp))

            EmailField(
                email = email,
                onEmailChange = { email = it.trim() },
                isError = fieldErrors.containsKey("email"),
                supportingText = {
                    fieldErrors["email"]?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            PasswordField(
                password = password,
                onPasswordChange = { password = it },
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                isError = fieldErrors.containsKey("password"),
                errorMessage = fieldErrors["password"]
            )

            Spacer(modifier = Modifier.height(8.dp))

            LongButton(
                text = if (isLoading) "Signing In..." else "Sign In",
                onClick = { onSignInClick(email, password) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!isKeyboardVisible) {
                AdditionalOptions(onItemClick = onItemClick)
            }
        }
    }
    ShrinkOverlay()
}

@Composable
fun LoginHeader(isKeyboardVisible: Boolean) {
    val logoFontSize = if (isKeyboardVisible) 32.sp else 48.sp
    val logoBottomPadding = if (isKeyboardVisible) 8.dp else 16.dp

    Text(
        text = stringResource(id = R.string.app_name),
        fontSize = logoFontSize,
        color = Blue1,
        modifier = Modifier.padding(bottom = logoBottomPadding)
    )

    if (!isKeyboardVisible) {
        Text(
            text = stringResource(id = R.string.welcome),
            fontSize = 24.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.enter_data),
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}



@Composable
fun AdditionalOptions(onItemClick: () -> Unit) {
    Row {
        Text(
            text = stringResource(id = R.string.new_to_app),
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = stringResource(id=R.string.create_account),
            fontSize = 14.sp,
            color = Blue1,
            modifier = Modifier.clickable { onItemClick() }
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(id=R.string.or_login_with),
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
            onItemClick = {},
            onSignInClick = { _, _ ->},
            isLoading = false,
            fieldErrors = emptyMap(),
            errorMessage = null,
            onFieldChanged = {}
        )
    }
}