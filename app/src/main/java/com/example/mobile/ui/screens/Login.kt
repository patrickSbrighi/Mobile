package com.example.mobile.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobile.R
import com.example.mobile.ui.Route
import com.example.mobile.ui.data.FirebaseRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false)}

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { token ->
                    FirebaseRepository.signInWithGoogle(token,
                        onSuccess = {
                            navController.navigate(Route.Profile) {
                                popUpTo(Route.Login) { inclusive = true }
                            }
                        },
                        onFailure = { error -> errorMessage = error }
                    )
                }
            } catch (e: ApiException) {
                errorMessage = "Google Sign In fallito: ${e.message}"
            }
        }
    }

    Scaffold() { contentPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(contentPadding)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.Check, "Logo", Modifier.size(80.dp), MaterialTheme.colorScheme.primary)
                Spacer(Modifier.size(8.dp))
                Text("Undrgrnd Hype", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.size(32.dp))

            OutlinedTextField(
                value = email, onValueChange = { email = it; emailError = false },
                isError = emailError, label = { Text("Email") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it; passwordError = false },
                isError = passwordError, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(icon, null) }
                }
            )

            if (errorMessage != null) {
                Spacer(Modifier.size(8.dp))
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.size(24.dp))

            Button(
                onClick = {
                    if (validateInputs(email, password, { emailError=true }, { passwordError=true }, { errorMessage=it })) {
                        FirebaseRepository.login(email, password,
                            onSuccess = {
                                navController.navigate(Route.Profile) { popUpTo(Route.Login) { inclusive = true } }
                            },
                            onFailure = {
                                errorMessage = it
                                password = ""; passwordError = true
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) { Text("ACCEDI", fontWeight = FontWeight.Bold) }

            Spacer(Modifier.size(16.dp))
            Row {
                Text("Non hai un account? ")
                Text("Registrati", color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController.navigate(Route.Registration) })
            }

            Spacer(Modifier.size(32.dp))
            HorizontalDivider()
            Spacer(Modifier.size(32.dp))

            Surface(
                onClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) },
                shape = MaterialTheme.shapes.medium,
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Accedi con Google",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(Modifier.size(16.dp))
        }
    }
}

fun validateInputs(e: String, p: String, onE: () -> Unit, onP: () -> Unit, onMsg: (String) -> Unit): Boolean {
    if (e.isBlank() || p.isBlank()) { onE(); onP(); onMsg("Compilare tutti i campi"); return false }
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches()) { onE(); onMsg("Email non valida"); return false }
    return true
}