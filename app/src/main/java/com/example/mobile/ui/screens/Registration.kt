package com.example.mobile.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobile.ui.data.FirebaseRepository
import com.example.mobile.ui.data.UserRole
import com.example.mobile.ui.Route
import com.example.mobile.ui.composables.RoleSelectionBlock

@Composable
fun RegistrationScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confermPassword by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var confermPasswordError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confermPasswordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.FAN) }

    Scaffold(){ contentPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(contentPadding)
                .padding(12.dp)
                .fillMaxSize()
        ) {
            Text("Benvenuto!")
            Spacer(Modifier.size(12.dp))
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                isError = nameError,
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
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
                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = null)
                    }
                }
            )
            OutlinedTextField(
                value = confermPassword,
                onValueChange = {
                    confermPassword = it
                    confermPasswordError = false },
                isError = confermPasswordError,
                label = { Text("Conferma password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (confermPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (confermPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { confermPasswordVisible = !confermPasswordVisible }) {
                        Icon(imageVector = icon, contentDescription = null)
                    }
                }
            )
            Spacer(Modifier.size(16.dp))
            Text("Chi sei?", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.size(8.dp))

            RoleSelectionBlock(
                selectedRole = selectedRole,
                onRoleSelected = { role: UserRole -> selectedRole = role }
            )
            Spacer(Modifier.size(16.dp))
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.size(6.dp))
            Row {
                Text("Hai già un account? ")
                Text(
                    text = "Login",
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        navController.navigate(Route.Login)
                    }
                )
            }
            Spacer(Modifier.size(12.dp))
            Button(
                onClick = {
                    if (validateRegistration(
                            name, email, password, confermPassword, selectedRole,
                            { nameError = true; name = "" },
                            { emailError = true; email = "" },
                            { passwordError = true; password = "" },
                            { confermPasswordError = true; confermPassword = "" },
                            { errorMessage = it }
                        )) {

                        FirebaseRepository.register(
                            email = email,
                            pass = password,
                            username = name,
                            role = selectedRole,
                            onSuccess = {
                                navController.navigate(Route.Login)
                            },
                            onFailure = { error ->
                                errorMessage = error
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text("Registrati")
            }
        }
    }
}

fun validateRegistration(
    name: String,
    email: String,
    password: String,
    confermPassword: String,
    role: UserRole,
    onNameError: () -> Unit,
    onEmailError: () -> Unit,
    onPasswordError: () -> Unit,
    onConfermPasswordError: () -> Unit,
    onErrorMessage: (String) -> Unit
): Boolean {

    if (name.isBlank() || email.isBlank() || password.isBlank() || confermPassword.isBlank()) {
        onErrorMessage("Compilare tutti i campi")
        if(name.isBlank()) onNameError()
        if(email.isBlank()) onEmailError()
        if(password.isBlank()) onPasswordError()
        if(confermPassword.isBlank()) onConfermPasswordError()
        return false
    }

    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        onEmailError()
        onErrorMessage("Formato email non valido.")
        return false
    }

    if (password.length < 8) {
        onPasswordError()
        onErrorMessage("La password deve avere almeno 8 caratteri")
        return false
    }

    if (!password.any { it.isUpperCase() }) {
        onPasswordError()
        onErrorMessage("La password deve contenere almeno una lettera maiuscola")
        return false
    }

    if (!password.any { it.isLowerCase() }) {
        onPasswordError()
        onErrorMessage("La password deve contenere almeno una lettera minuscola.")
        return false
    }

    if (!password.any { it.isDigit() }) {
        onPasswordError()
        onErrorMessage("La password deve contenere almeno un numero.")
        return false
    }

    if(confermPassword != password){
        onConfermPasswordError()
        onErrorMessage("Le password non corrispondono")
        return false
    }

    return true
}