package com.example.facefit.ui.presentation.screens.auth.signUp

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.facefit.R
import com.example.facefit.domain.utils.Resource
import com.example.facefit.ui.presentation.components.textfields.PasswordField
import com.example.facefit.ui.presentation.screens.auth.login.LoginPage
import com.example.facefit.ui.presentation.screens.home.HomePageActivity
import com.example.facefit.ui.theme.FaceFitTheme
import com.togitech.ccp.component.TogiCountryCodePicker
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
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val signUpState by viewModel.signUpState.collectAsStateWithLifecycle()
    val fieldErrors by viewModel.fieldErrors.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(firstName) { viewModel.clearFieldError("firstName") }
    LaunchedEffect(lastName) { viewModel.clearFieldError("lastName") }
    LaunchedEffect(email) { viewModel.clearFieldError("email") }
    LaunchedEffect(password) { viewModel.clearFieldError("password") }
    LaunchedEffect(confirmPassword) { viewModel.clearFieldError("confirmPassword") }

    LaunchedEffect(signUpState, fieldErrors) {
        when (signUpState) {
            is Resource.Success -> onSignUpSuccess()
            is Resource.Error -> {
                if (fieldErrors.isEmpty()) {
                    errorMessage?.let { message ->
                        scope.launch {
                            snackBarHostState.showSnackbar(message)
                        }
                    }
                }
            }
            else -> {}
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
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text(stringResource(id = R.string.first_name)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    isError = fieldErrors.containsKey("firstName"),
                    supportingText = {
                        fieldErrors["firstName"]?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text(stringResource(id = R.string.last_name)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    isError = fieldErrors.containsKey("lastName"),
                    supportingText = {
                        fieldErrors["lastName"]?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                )
            }

            Column {
                TogiCountryCodePicker(
                    onValueChange = { (phoneCode, number), _ ->
                        phoneNumber = "$phoneCode$number"
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    label = { Text(stringResource(id = R.string.phone_number)) },
                    showCountryFlag = true,
                    showCountryCode = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                if (fieldErrors.containsKey("phone")) {
                    Text(
                        text = fieldErrors["phone"] ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(id = R.string.email)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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

            PasswordField(
                password = password,
                onPasswordChange = { password = it },
                passwordVisible = passwordVisible,
                onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                isError = fieldErrors.containsKey("password"),
                errorMessage = fieldErrors["password"]
            )

            PasswordField(
                password = confirmPassword,
                onPasswordChange = { confirmPassword = it },
                passwordVisible = confirmPasswordVisible,
                onPasswordVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible },
                label = stringResource(id = R.string.confirm_password),
                isError = fieldErrors.containsKey("confirmPassword"),
                errorMessage = fieldErrors["confirmPassword"]
            )

            Spacer(modifier = Modifier.height(16.dp))

            val isLoading = signUpState is Resource.Loading
            Button(
                onClick = {
                    viewModel.signUp(
                        firstName = firstName,
                        lastName = lastName,
                        phone = phoneNumber,
                        email = email,
                        password = password,
                        confirmPassword = confirmPassword
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading,
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