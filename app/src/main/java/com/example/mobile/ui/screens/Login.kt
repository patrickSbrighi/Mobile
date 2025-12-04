package com.example.mobile.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobile.ui.Route
import com.example.mobile.ui.data.FirebaseRepository

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false)}

    Scaffold() { contentPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(contentPadding)
                .padding(12.dp)
                .fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "Logo",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "Undrgrnd Hype",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = false
                },
                isError = emailError,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = false },
                isError = passwordError,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible)
                        Icons.Default.Visibility
                    else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = null)
                    }
                }
            )
            Spacer(Modifier.size(6.dp))
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.size(6.dp))
            Row {
                Text("Non hai un account? ")
                Text(
                    text = "Registrati",
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        navController.navigate(Route.Registration)
                    }
                )
            }
            Spacer(Modifier.size(12.dp))
            Button(
                onClick = {
                    if (validateInputs(email, password, { emailError = true; email = "" }, { passwordError = true; password = "" }, { errorMessage = it })) {
                        FirebaseRepository.login(
                            email = email,
                            pass = password,
                            onSuccess = {
                                navController.navigate(Route.Profile) {
                                    popUpTo(Route.Login) { inclusive = true }
                                }
                            },
                            onFailure = { errorMsg ->
                                errorMessage = errorMsg
                                password = ""
                                passwordError = true
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text("Login")
            }
        }
    }
}

fun validateInputs(
    email: String,
    password: String,
    onEmailError: () -> Unit,
    onPasswordError: () -> Unit,
    onErrorMessage: (String) -> Unit
): Boolean {
    if (email.isBlank()) {
        onEmailError()
        onErrorMessage("Compilare tutti i campi")
        return false
    }
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        onEmailError()
        onErrorMessage("Formato email non valido.")
        return false
    }
    if (password.isBlank()) {
        onPasswordError()
        onErrorMessage("Compilare tutti i campi")
        return false
    }
    return true
}