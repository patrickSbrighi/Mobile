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
import com.example.mobile.ui.data.FirebaseRepository
import com.example.mobile.ui.data.UserRole
import com.example.mobile.ui.Route
import com.example.mobile.ui.composables.RoleSelectionBlock
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun RegistrationScreen(navController: NavController) {
    val context = LocalContext.current
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

    Scaffold(){ contentPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(contentPadding)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text("Benvenuto!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.size(24.dp))

            OutlinedTextField(
                value = name, onValueChange = { name = it; nameError = false },
                isError = nameError, label = { Text("Username") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
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
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = confermPassword, onValueChange = { confermPassword = it; confermPasswordError = false },
                isError = confermPasswordError, label = { Text("Conferma password") }, modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (confermPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (confermPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { confermPasswordVisible = !confermPasswordVisible }) { Icon(icon, null) }
                }
            )

            Spacer(Modifier.size(16.dp))
            Text("Chi sei?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.size(8.dp))
            RoleSelectionBlock(selectedRole = selectedRole, onRoleSelected = { selectedRole = it })

            if (errorMessage != null) {
                Spacer(Modifier.size(8.dp))
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.size(24.dp))
            Button(
                onClick = {
                    if (validateRegistration(name, email, password, confermPassword, selectedRole,
                            { nameError=true }, { emailError=true }, { passwordError=true }, { confermPasswordError=true },
                            { errorMessage=it })) {
                        FirebaseRepository.register(email, password, name, selectedRole,
                            onSuccess = { navController.navigate(Route.Login) },
                            onFailure = { errorMessage = it }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) { Text("REGISTRATI", fontWeight = FontWeight.Bold) }

            Spacer(Modifier.size(16.dp))
            Row {
                Text("Hai già un account? ")
                Text("Login", color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController.navigate(Route.Login) })
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
                        text = "Registrati con Google",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(Modifier.size(32.dp))
        }
    }
}

fun validateRegistration(
    name: String, email: String, pass: String, confPass: String, role: UserRole,
    onName: ()->Unit, onEmail: ()->Unit, onPass: ()->Unit, onConf: ()->Unit, onMsg: (String)->Unit
): Boolean {
    if (name.isBlank() || email.isBlank() || pass.isBlank() || confPass.isBlank()) {
        onMsg("Compilare tutti i campi"); if(name.isBlank()) onName(); if(email.isBlank()) onEmail()
        if(pass.isBlank()) onPass(); if(confPass.isBlank()) onConf(); return false
    }
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { onEmail(); onMsg("Email non valida"); return false }
    if (pass.length < 8) { onPass(); onMsg("Password min 8 caratteri"); return false }
    if (!pass.any { it.isUpperCase() }) { onPass(); onMsg("Password deve avere una maiuscola"); return false }
    if (!pass.any { it.isDigit() }) { onPass(); onMsg("Password deve avere un numero"); return false }
    if (confPass != pass) { onConf(); onMsg("Le password non corrispondono"); return false }
    return true
}