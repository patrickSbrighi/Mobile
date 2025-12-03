package com.example.mobile.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mobile.ui.data.FirebaseFunction
import com.example.mobile.ui.Route
import com.example.mobile.ui.data.ALL_GENRES
import com.example.mobile.ui.utils.saveImageToInternalStorage
import java.io.File

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, userRole: String?) {
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var roleDisplay by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedGenres by remember { mutableStateOf(listOf<String>()) }

    var isLoading by remember { mutableStateOf(true) }
    var isEditingName by remember { mutableStateOf(false) }
    var tempUsername by remember { mutableStateOf("") }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        if (FirebaseFunction.getCurrentUser() != null) {
            FirebaseFunction.getUserProfile { profile ->
                if (profile != null) {
                    username = profile.username
                    email = profile.email
                    roleDisplay = profile.role
                    selectedGenres = profile.genres

                    if (profile.profileImageUrl.isNotEmpty()) {
                        imageUri = Uri.parse(profile.profileImageUrl)
                    }
                }
                isLoading = false
            }
        } else {
            navController.navigate(Route.Login) { popUpTo(0) }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedUri = saveImageToInternalStorage(context, uri)
            if (savedUri != null) {
                imageUri = savedUri
                FirebaseFunction.updateUserField("profileImageUrl", savedUri.toString())
            }
        }
        showImageSourceDialog = false
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            val savedUri = saveImageToInternalStorage(context, tempCameraUri!!)
            if (savedUri != null) {
                imageUri = savedUri
                FirebaseFunction.updateUserField("profileImageUrl", savedUri.toString())
            }
        }
        showImageSourceDialog = false
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = File(context.cacheDir, "temp_cam_img.jpg")
            try {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Errore FileProvider: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Permesso camera negato", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (showImageSourceDialog) {
                AlertDialog(
                    onDismissRequest = { showImageSourceDialog = false },
                    title = { Text("Modifica foto profilo") },
                    text = { Text("Scegli sorgente") },
                    confirmButton = {
                        TextButton(onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                val file = File(context.cacheDir, "temp_cam_img.jpg")
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                                tempCameraUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Fotocamera")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { galleryLauncher.launch("image/*") }) {
                            Icon(Icons.Default.Image, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Galleria")
                        }
                    }
                )
            }

            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                            .clickable { showImageSourceDialog = true }
                    ) {
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Foto Profilo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(45.dp), tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (isEditingName) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = tempUsername,
                                        onValueChange = { tempUsername = it },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = {
                                        FirebaseFunction.updateUserField("username", tempUsername, onSuccess = {
                                            username = tempUsername
                                            isEditingName = false
                                        })
                                    }) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f).clickable {
                                        tempUsername = username
                                        isEditingName = true
                                    }
                                ) {
                                    Text(text = username, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                                }

                                IconButton(onClick = {
                                    FirebaseFunction.logout()
                                    navController.navigate(Route.Login) { popUpTo(0) { inclusive = true } }
                                }) {
                                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        Text(text = email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                            Text(text = roleDisplay.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

                Text("Generi preferiti", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Seleziona ciò che ti piace", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ALL_GENRES.forEach { genre ->
                        val isSelected = selectedGenres.contains(genre)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val newGenres = if (isSelected) selectedGenres - genre else selectedGenres + genre
                                selectedGenres = newGenres
                                FirebaseFunction.updateUserField("genres", newGenres)
                            },
                            label = { Text(genre) },
                            leadingIcon = if (isSelected) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                        )
                    }
                }

                val spacerHeight = if (userRole == "ORGANIZER") 100.dp else 16.dp
                Spacer(modifier = Modifier.height(spacerHeight))
            }
        }
    }
}