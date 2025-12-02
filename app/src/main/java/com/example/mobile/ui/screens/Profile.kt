package com.example.mobile.ui.screens

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mobile.ui.data.FirebaseFunction
import com.example.mobile.ui.Route

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var savedGenres by remember { mutableStateOf(listOf<String>()) }
    var localGenres by remember { mutableStateOf(listOf<String>()) }

    val hasUnsavedChanges = savedGenres.toSet() != localGenres.toSet()

    var isLoading by remember { mutableStateOf(true) }
    var isEditingName by remember { mutableStateOf(false) }
    var tempUsername by remember { mutableStateOf("") }

    val allGenres = listOf(
        "Pop", "Indie", "Jazz", "Classica", "Hip Hop",
        "Rock classico", "Hard rock", "Alternative rock", "Metal",
        "Heavy metal", "Punk rock", "Hardcore punk", "Grunge",
        "Post-punk", "Stoner rock", "Metalcore", "Garage rock",
        "Noise rock", "Post-hardcore", "Thrash metal"
    )

    LaunchedEffect(Unit) {
        if (FirebaseFunction.getCurrentUser() != null) {
            FirebaseFunction.getUserProfile { profile ->
                if (profile != null) {
                    username = profile.username
                    email = profile.email
                    role = profile.role
                    savedGenres = profile.genres
                    localGenres = profile.genres
                }
                isLoading = false
            }
        } else {
            navController.navigate(Route.Login) { popUpTo(0) }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profilo", fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = {
                        FirebaseFunction.logout()
                        navController.navigate(Route.Login) {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { contentPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .clickable { galleryLauncher.launch("image/*") }
                            .background(Color.LightGray)
                    ) {
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Foto Profilo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp),
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        if (isEditingName) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = tempUsername,
                                    onValueChange = { tempUsername = it },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedContainerColor = Color.Transparent
                                    )
                                )
                                IconButton(onClick = {
                                    FirebaseFunction.updateUserField(
                                        field = "username",
                                        value = tempUsername,
                                        onSuccess = {
                                            username = tempUsername
                                            isEditingName = false
                                            Toast.makeText(context, "Nome aggiornato", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }) {
                                    Icon(Icons.Default.Check, contentDescription = "Salva", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { isEditingName = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Annulla", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    tempUsername = username
                                    isEditingName = true
                                }
                            ) {
                                Text(
                                    text = username,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Modifica",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.Gray
                                )
                            }
                        }

                        Text(text = email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = role.uppercase(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Generi preferiti", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Seleziona ciò che ti piace", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }

                    if (hasUnsavedChanges) {
                        Button(
                            onClick = {
                                FirebaseFunction.updateUserField(
                                    field = "genres",
                                    value = localGenres,
                                    onSuccess = {
                                        savedGenres = localGenres
                                        Toast.makeText(context, "Generi salvati", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Salva", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allGenres.forEach { genre ->
                        val isSelected = localGenres.contains(genre)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                localGenres = if (isSelected) {
                                    localGenres - genre
                                } else {
                                    localGenres + genre
                                }
                            },
                            label = { Text(genre) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}