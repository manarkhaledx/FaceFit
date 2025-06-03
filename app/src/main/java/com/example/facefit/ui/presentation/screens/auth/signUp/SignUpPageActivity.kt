package com.example.facefit.ui.presentation.screens.auth.signUp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.facefit.R
import com.example.facefit.domain.utils.Resource
import com.example.facefit.ui.presentation.screens.auth.login.LoginPage
import com.example.facefit.ui.presentation.screens.home.HomePageActivity
import com.example.facefit.ui.theme.Blue1
import com.example.facefit.ui.theme.FaceFitTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceFitTheme {
                SignUpScreen(
                    onNavigateBack = {
                        val intent = Intent(this, LoginPage::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onSignUpSuccess = {
                        val intent = Intent(this, HomePageActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onSignUpSuccess: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val signUpState by viewModel.signUpState.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val passwordVisible = remember { mutableStateOf(false) }
    val confirmPasswordVisible = remember { mutableStateOf(false) }

    LaunchedEffect(signUpState) {
        val message = (signUpState as? Resource.Error)?.message
        if (!message.isNullOrBlank()) {
            scope.launch {
                snackBarHostState.showSnackbar(message)
            }
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.return_to_login)
                    )
                }
                Text(
                    text = stringResource(id = R.string.return_to_login),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.firstName,
                    onValueChange = { viewModel.updateFirstName(it) },
                    label = { Text(stringResource(id = R.string.first_name)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    isError = uiState.firstNameError != null,
                    supportingText = {
                        uiState.firstNameError?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                )
                OutlinedTextField(
                    value = uiState.lastName,
                    onValueChange = { viewModel.updateLastName(it) },
                    label = { Text(stringResource(id = R.string.last_name)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    isError = uiState.lastNameError != null,
                    supportingText = {
                        uiState.lastNameError?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                )
            }

            EgyptPhoneNumberField(
                phoneNumber = uiState.phone,
                onPhoneNumberChange = { viewModel.updatePhone(it) },
                isError = uiState.phoneError != null,
                errorMessage = uiState.phoneError
            )

            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.updateEmail(it) },
                label = {
                    Log.d("UI_REBUILD", "Email field rebuilt with error: ${uiState.emailError}")
                    Text(stringResource(id = R.string.email))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = uiState.emailError != null,
                supportingText = {
                    uiState.emailError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )

            PasswordField(
                password = uiState.password,
                onPasswordChange = { viewModel.updatePassword(it) },
                passwordVisible = passwordVisible.value,
                onPasswordVisibilityChange = { passwordVisible.value = !passwordVisible.value },
                isError = uiState.passwordError != null,
                errorMessage = uiState.passwordError
            )

            PasswordField(
                password = uiState.confirmPassword,
                onPasswordChange = { viewModel.updateConfirmPassword(it) },
                passwordVisible = confirmPasswordVisible.value,
                onPasswordVisibilityChange = { confirmPasswordVisible.value = !confirmPasswordVisible.value },
                label = stringResource(id = R.string.confirm_password),
                isError = uiState.confirmPasswordError != null,
                errorMessage = uiState.confirmPasswordError
            )

            Spacer(modifier = Modifier.height(16.dp))

            val isLoading = signUpState is Resource.Loading
            Button(
                onClick = { viewModel.signUp() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Blue1),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(text = stringResource(id = R.string.sign_up))
                }
            }
        }
    }
}

@Composable
fun EgyptPhoneNumberField(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var localPhone by remember { mutableStateOf(phoneNumber.removePrefix("+20")) }

    LaunchedEffect(phoneNumber) {
        if (!phoneNumber.startsWith("+20")) {
            localPhone = ""
        }
    }

    OutlinedTextField(
        value = localPhone,
        onValueChange = {
            if (it.all { char -> char.isDigit() }) {
                localPhone = it.take(11)
                onPhoneNumberChange("+20$localPhone")
            }
        },
        label = { Text("Phone Number") },
        leadingIcon = {
            Text(
                text = "🇪🇬 +20",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        },
        shape = RoundedCornerShape(12.dp),
        isError = isError,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Phone
        ),
        modifier = Modifier.fillMaxWidth(),
        supportingText = {
            if (isError && !errorMessage.isNullOrEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
}

@Composable
fun PasswordField(
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    isError: Boolean = false,
    errorMessage: String? = null,
label: String = "Password"
) {
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        isError = isError,
        trailingIcon = {
            IconButton(onClick = onPasswordVisibilityChange) {
Icon(
    painter = androidx.compose.ui.res.painterResource(
        id = if (passwordVisible) R.drawable.eye_visible else R.drawable.eye_not_visibile
    ),
    contentDescription = if (passwordVisible) "Hide password" else "Show password"
)
            }
        },
        supportingText = {
            if (isError && !errorMessage.isNullOrEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
}

@Preview
@Composable
fun SignUpScreenPreview() {
    FaceFitTheme {
        SignUpScreen(
            onNavigateBack = { },
            onSignUpSuccess = { }
        )
    }
}