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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mobile.ui.data.ALL_GENRES
import com.example.mobile.ui.data.Event
import com.example.mobile.ui.data.FirebaseFunction
import com.example.mobile.ui.data.DEFAULT_LOCATION
import com.example.mobile.ui.composables.OsmLocationPicker
import com.example.mobile.ui.utils.getAddressFromCoordinates
import com.example.mobile.ui.utils.getUserLocation
import com.example.mobile.ui.utils.saveImageToInternalStorage
import com.example.mobile.ui.utils.searchPlaces
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("") }

    var country by remember { mutableStateOf("Italia") }
    var province by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var civico by remember { mutableStateOf("") }

    var locationGeoPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var isLocationConfirmed by remember { mutableStateOf(false) }

    var showMapDialog by remember { mutableStateOf(false) }
    var mapStartPoint by remember { mutableStateOf(DEFAULT_LOCATION) }
    var mapZoomLevel by remember { mutableStateOf(15.0) }
    var isGeocodingLoading by remember { mutableStateOf(false) }

    var isManualSearchRequired by remember { mutableStateOf(true) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) imageUri = saveImageToInternalStorage(context, uri)
        showImageSourceDialog = false
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraUri != null) imageUri = saveImageToInternalStorage(context, tempCameraUri!!)
        showImageSourceDialog = false
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val file = File(context.cacheDir, "temp_event_img.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            getUserLocation(context) { locationString, geo ->
                val parts = locationString.split(",")
                if (parts.isNotEmpty()) city = parts[0].trim()
                if (parts.size > 1) country = parts[1].trim()
                if (geo != null) {
                    mapStartPoint = geo
                    mapZoomLevel = 18.0
                    isManualSearchRequired = false
                }
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
    datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000

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
        if (showMapDialog) {
            Dialog(
                onDismissRequest = { showMapDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        OsmLocationPicker(
                            startLocation = mapStartPoint,
                            initialZoom = mapZoomLevel,
                            onLocationPicked = { geo ->
                                scope.launch {
                                    locationGeoPoint = geo
                                    val result = getAddressFromCoordinates(geo.latitude, geo.longitude)
                                    if (result != null) {
                                        address = result.address?.road ?: result.displayName.split(",")[0]
                                        civico = result.address?.houseNumber ?: ""
                                        if (!result.address?.city.isNullOrEmpty()) city = result.address?.city!!
                                        if (!result.address?.province.isNullOrEmpty()) province = result.address?.province!!
                                        if (!result.address?.country.isNullOrEmpty()) country = result.address?.country!!
                                    }
                                    isLocationConfirmed = false
                                    showMapDialog = false
                                }
                            }
                        )
                        IconButton(
                            onClick = { showMapDialog = false },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .background(Color.White.copy(alpha = 0.7f), shape = RoundedCornerShape(50))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Chiudi")
                        }

                        Surface(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.9f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Tocca il punto esatto sulla mappa", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

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
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
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
                                disabledBorderColor = MaterialTheme.colorScheme.outline
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
                                disabledBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Box(Modifier.matchParentSize().clickable { timePickerDialog.show() })
                    }
                }

                HorizontalDivider()

                Text("Dove si svolge?", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text("Cerca zona e via, poi conferma il punto sulla mappa", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it; isManualSearchRequired = true },
                        label = { Text("Stato") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = province,
                        onValueChange = { province = it; isManualSearchRequired = true },
                        label = { Text("Provincia") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it; isManualSearchRequired = true },
                    label = { Text("Comune") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it; isManualSearchRequired = true },
                        label = { Text("Via / Piazza") },
                        modifier = Modifier.weight(2f),
                        placeholder = { Text("Es. Via Roma") }
                    )
                    OutlinedTextField(
                        value = civico,
                        onValueChange = { civico = it; isManualSearchRequired = true },
                        label = { Text("N. Civ") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                TextButton(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            getUserLocation(context) { locationString, geo ->
                                val parts = locationString.split(",")
                                if (parts.isNotEmpty()) city = parts[0].trim()
                                if (parts.size > 1) country = parts[1].trim()
                                if (geo != null) {
                                    mapStartPoint = geo
                                    mapZoomLevel = 18.0
                                    isManualSearchRequired = false
                                }
                                Toast.makeText(context, "Posizione GPS rilevata", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Usa mia posizione GPS")
                }

                Spacer(modifier = Modifier.height(8.dp))

                val buttonColor = when {
                    locationGeoPoint != null && isLocationConfirmed -> Color(0xFF4CAF50)
                    locationGeoPoint != null && !isLocationConfirmed -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }

                val buttonText = when {
                    locationGeoPoint != null && isLocationConfirmed -> "POSIZIONE CONFERMATA"
                    locationGeoPoint != null && !isLocationConfirmed -> "CONFERMA DATI"
                    else -> "APRI MAPPA E SELEZIONA"
                }

                val buttonIcon = when {
                    locationGeoPoint != null && isLocationConfirmed -> Icons.Default.Check
                    locationGeoPoint != null && !isLocationConfirmed -> Icons.Default.Edit
                    else -> Icons.Default.Map
                }

                Button(
                    onClick = {
                        if (locationGeoPoint != null && !isLocationConfirmed) {
                            isLocationConfirmed = true
                        } else if (locationGeoPoint != null && isLocationConfirmed) {
                            isLocationConfirmed = false

                            if (!isManualSearchRequired) {
                                showMapDialog = true
                            } else {
                                val queryParts = mutableListOf<String>()
                                if (address.isNotBlank()) queryParts.add(address)
                                if (civico.isNotBlank()) queryParts.add(civico)
                                if (city.isNotBlank()) queryParts.add(city)
                                if (province.isNotBlank()) queryParts.add(province)
                                if (country.isNotBlank()) queryParts.add(country)
                                val query = queryParts.joinToString(", ")

                                isGeocodingLoading = true
                                scope.launch {
                                    val results = searchPlaces(query)
                                    isGeocodingLoading = false
                                    if (results.isNotEmpty()) {
                                        mapStartPoint = GeoPoint(results[0].lat, results[0].lon)
                                        mapZoomLevel = 18.0
                                    }
                                    showMapDialog = true
                                }
                            }
                        } else {

                            if (!isManualSearchRequired) {
                                showMapDialog = true
                            } else {
                                val queryParts = mutableListOf<String>()
                                if (address.isNotBlank()) queryParts.add(address)
                                if (civico.isNotBlank()) queryParts.add(civico)
                                if (city.isNotBlank()) queryParts.add(city)
                                if (province.isNotBlank()) queryParts.add(province)
                                if (country.isNotBlank()) queryParts.add(country)

                                val query = queryParts.joinToString(", ")

                                if (query.length < 3) {
                                    Toast.makeText(context, "Inserisci almeno Città o Stato", Toast.LENGTH_SHORT).show()
                                } else {
                                    isGeocodingLoading = true
                                    scope.launch {
                                        val results = searchPlaces(query)
                                        isGeocodingLoading = false
                                        if (results.isNotEmpty()) {
                                            mapStartPoint = GeoPoint(results[0].lat, results[0].lon)
                                            mapZoomLevel = 18.0
                                            showMapDialog = true
                                        } else {
                                            Toast.makeText(context, "Zona non trovata, mappa centrata di default", Toast.LENGTH_SHORT).show()
                                            mapZoomLevel = 10.0
                                            showMapDialog = true
                                        }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                ) {
                    if (isGeocodingLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Icon(buttonIcon, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(buttonText)
                    }
                }

                if (locationGeoPoint != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            if (isLocationConfirmed) "(Clicca per modificare la posizione)" else "(Controlla i dati sopra e conferma)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

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
                        if (title.isNotEmpty() && selectedDate.isNotEmpty() && locationGeoPoint != null && isLocationConfirmed && selectedGenre.isNotEmpty()) {
                            isLoading = true
                            val finalAddress = if (civico.isNotBlank()) "$address $civico" else address
                            val formattedLocation = listOf(finalAddress, city).filter { it.isNotBlank() }.joinToString(", ")

                            val newEvent = Event(
                                title = title,
                                description = description,
                                location = formattedLocation,
                                date = selectedDate,
                                time = selectedTime,
                                genre = selectedGenre,
                                imageUrl = imageUri?.toString() ?: "",
                                lat = locationGeoPoint!!.latitude,
                                lng = locationGeoPoint!!.longitude
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
                        } else {
                            Toast.makeText(context, "Compila tutti i campi e CONFERMA la posizione", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = locationGeoPoint != null && isLocationConfirmed
                ) {
                    Text("PUBBLICA EVENTO")
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}