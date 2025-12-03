package com.example.mobile.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mobile.ui.data.ALL_GENRES
import com.example.mobile.ui.data.Event
import com.example.mobile.ui.data.FirebaseFunction
import com.example.mobile.ui.utils.getCoordinatesFromAddress
import com.example.mobile.ui.utils.getUserLocation
import com.example.mobile.ui.utils.saveImageToInternalStorage
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(navController: NavController) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }

    var locationGeoPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var isGeocodingLoading by remember { mutableStateOf(false) }

    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = saveImageToInternalStorage(context, uri)
        }
        showImageSourceDialog = false
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            imageUri = saveImageToInternalStorage(context, tempCameraUri!!)
        }
        showImageSourceDialog = false
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = File(context.cacheDir, "temp_event_img.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getUserLocation(context) { locationString, geo ->
                val parts = locationString.split(",")
                if (parts.isNotEmpty()) city = parts[0].trim()
                locationGeoPoint = geo
                Toast.makeText(context, "Posizione GPS rilevata", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day -> selectedDate = "$day/${month + 1}/$year" },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute -> selectedTime = String.format("%02d:%02d", hour, minute) },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuovo Evento") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (showImageSourceDialog) {
                AlertDialog(
                    onDismissRequest = { showImageSourceDialog = false },
                    title = { Text("Carica Locandina") },
                    confirmButton = {
                        TextButton(onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                val file = File(context.cacheDir, "temp_event_img.jpg")
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                                tempCameraUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }) { Text("Fotocamera") }
                    },
                    dismissButton = {
                        TextButton(onClick = { galleryLauncher.launch("image/*") }) { Text("Galleria") }
                    }
                )
            }

            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showImageSourceDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Locandina",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                            Text("Aggiungi Locandina", color = Color.Gray)
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titolo Evento") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrizione") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedDate,
                            onValueChange = {},
                            label = { Text("Data") },
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.CalendarToday, null) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Box(Modifier.matchParentSize().clickable { datePickerDialog.show() })
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedTime,
                            onValueChange = {},
                            label = { Text("Ora") },
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.Schedule, null) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Box(Modifier.matchParentSize().clickable { timePickerDialog.show() })
                    }
                }

                Divider()

                Text("Dove si svolge?", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = {
                            city = it
                            locationGeoPoint = null
                        },
                        label = { Text("Città") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = {
                            address = it
                            locationGeoPoint = null
                        },
                        label = { Text("Indirizzo") },
                        modifier = Modifier.weight(1.5f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            getUserLocation(context) { c, geo ->
                                val parts = c.split(",")
                                if (parts.isNotEmpty()) city = parts[0].trim()
                                locationGeoPoint = geo
                            }
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    }) {
                        Icon(Icons.Default.MyLocation, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Usa mia posizione")
                    }

                    Button(
                        onClick = {
                            if(city.isNotEmpty() && address.isNotEmpty()) {
                                isGeocodingLoading = true
                                focusManager.clearFocus()
                                scope.launch {
                                    val fullAddress = "$address, $city"
                                    val coords = getCoordinatesFromAddress(fullAddress)
                                    isGeocodingLoading = false
                                    if (coords != null) {
                                        locationGeoPoint = GeoPoint(coords.first, coords.second)
                                        Toast.makeText(context, "Indirizzo trovato sulla mappa!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Indirizzo non trovato", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Inserisci Città e Indirizzo", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        if (isGeocodingLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Verifica Mappa")
                        }
                    }
                }

                if(locationGeoPoint != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green)
                        Spacer(Modifier.width(8.dp))
                        Text("Posizione confermata: ${locationGeoPoint!!.latitude}, ${locationGeoPoint!!.longitude}", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    Text("⚠ Verifica l'indirizzo per attivare la mappa", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }

                Divider()

                Text("Genere Musicale", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ALL_GENRES.forEach { genre ->
                        FilterChip(
                            selected = selectedGenre == genre,
                            onClick = { selectedGenre = genre },
                            label = { Text(genre) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (title.isNotEmpty() && selectedDate.isNotEmpty() && city.isNotEmpty() && address.isNotEmpty() && selectedGenre.isNotEmpty()) {
                            if(locationGeoPoint == null) {
                                Toast.makeText(context, "Premi 'Verifica Mappa' prima di pubblicare", Toast.LENGTH_LONG).show()
                            } else {
                                isLoading = true
                                val newEvent = Event(
                                    title = title,
                                    description = description,
                                    location = "$address, $city",
                                    date = selectedDate,
                                    time = selectedTime,
                                    genre = selectedGenre,
                                    imageUrl = imageUri?.toString() ?: "",
                                    lat = locationGeoPoint?.latitude ?: 0.0,
                                    lng = locationGeoPoint?.longitude ?: 0.0
                                )

                                FirebaseFunction.createEvent(
                                    event = newEvent,
                                    onSuccess = {
                                        isLoading = false
                                        Toast.makeText(context, "Evento Creato!", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    },
                                    onFailure = {
                                        isLoading = false
                                        Toast.makeText(context, "Errore: $it", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        } else {
                            Toast.makeText(context, "Compila tutti i campi", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = locationGeoPoint != null
                ) {
                    Text("PUBBLICA EVENTO")
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}