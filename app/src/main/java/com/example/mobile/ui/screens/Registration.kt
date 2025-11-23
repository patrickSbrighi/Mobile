package com.example.mobile.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobile.ui.Route
import com.example.mobile.ui.composables.AppBar

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

    Scaffold(
        topBar = { AppBar(navController, title = "Registrazione") }
    ) { contentPadding ->
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
                label = { Text("Nome") },
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
                    val icon = if (passwordVisible)
                        Icons.Default.Visibility
                    else Icons.Default.VisibilityOff
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
                    val icon = if (confermPasswordVisible)
                        Icons.Default.Visibility
                    else Icons.Default.VisibilityOff
                    IconButton(onClick = { confermPasswordVisible = !confermPasswordVisible }) {
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
                Text("Hai già un account?")
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
                            name,
                            email,
                            password,
                            confermPassword,
                            onNameError = {
                                nameError = true
                                name = ""
                            },
                            onEmailError = {
                                emailError = true
                                email = ""
                            },
                            onPasswordError = {
                                passwordError = true
                                password = ""
                            },
                            onConfermPasswordError = {
                                confermPasswordError  = true
                                confermPassword = ""
                            },
                            onErrorMessage = { errorMessage = it }
                        )) {
                        navController.navigate(Route.Login)
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
    onNameError: () -> Unit,
    onEmailError: () -> Unit,
    onPasswordError: () -> Unit,
    onConfermPasswordError: () -> Unit,
    onErrorMessage: (String) -> Unit
): Boolean {

    if (name.isBlank()) {
        onNameError()
        onErrorMessage("Compilare tutti i campi")
        return false
    }

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

    if (confermPassword.isBlank()) {
        onConfermPasswordError()
        onErrorMessage("Compilare tutti i campi")
        return false
    }

    //FIREBASE
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