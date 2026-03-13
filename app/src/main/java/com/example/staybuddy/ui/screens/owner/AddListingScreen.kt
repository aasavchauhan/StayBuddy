package com.example.staybuddy.ui.screens.owner

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.osmdroid.util.GeoPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddListingScreen(
    onNavigateBack: () -> Unit,
    onListingAdded: () -> Unit,
    viewModel: AddListingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onListingAdded()
        }
    }

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text("Error") },
            text = { Text(uiState.error!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissError() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("${if (uiState.isEditing) "Edit" else "Add"} Property (Step ${uiState.currentStep} of 5)") 
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (uiState.currentStep == 1) onNavigateBack() else viewModel.previousStep() 
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentPadding = PaddingValues(16.dp)
            ) {
                if (uiState.currentStep > 1) {
                    OutlinedButton(
                        onClick = { viewModel.previousStep() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Back")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
                
                Button(
                    onClick = { 
                        if (uiState.currentStep < 5) viewModel.nextStep() else viewModel.submitListing() 
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(if (uiState.currentStep < 5) "Next" else if (uiState.isEditing) "Save Changes" else "Publish Listing")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (uiState.currentStep) {
                1 -> Step1BasicInfo(uiState, viewModel)
                2 -> Step2PricingDetails(uiState, viewModel)
                3 -> Step3Amenities(uiState, viewModel)
                4 -> Step4Photos(uiState, viewModel)
                5 -> Step5Location(uiState, viewModel)
            }
        }
    }
}

@Composable
fun Step1BasicInfo(uiState: AddListingUiState, viewModel: AddListingViewModel) {
    Text("Basic Information", style = MaterialTheme.typography.titleLarge)
    
    OutlinedTextField(
        value = uiState.name,
        onValueChange = { viewModel.updateField("name", it) },
        label = { Text("Property Name / Title") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    
    OutlinedTextField(
        value = uiState.description,
        onValueChange = { viewModel.updateField("description", it) },
        label = { Text("Property Description") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 4,
        placeholder = { Text("E.g., A spacious 2BHK perfect for students...") }
    )
    
    OutlinedTextField(
        value = uiState.city,
        onValueChange = { viewModel.updateField("city", it) },
        label = { Text("City (e.g., Vadodara)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    
    OutlinedTextField(
        value = uiState.area,
        onValueChange = { viewModel.updateField("area", it) },
        label = { Text("Area / Locality (e.g., Alkapuri)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step2PricingDetails(uiState: AddListingUiState, viewModel: AddListingViewModel) {
    Text("Pricing & Details", style = MaterialTheme.typography.titleLarge)
    
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = uiState.monthlyRent,
            onValueChange = { viewModel.updateField("monthlyRent", it) },
            label = { Text("Rent (₹)/mo") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        
        OutlinedTextField(
            value = uiState.depositAmount,
            onValueChange = { viewModel.updateField("depositAmount", it) },
            label = { Text("Deposit (₹)") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    Text("Property Type", style = MaterialTheme.typography.titleMedium)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val types = listOf("Private Room", "Shared Room", "Entire Flat")
        types.forEach { type ->
            FilterChip(
                selected = uiState.roomType == type,
                onClick = { viewModel.updateField("roomType", type) },
                label = { Text(type) },
                modifier = Modifier.weight(1f)
            )
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
    Text("Tenant Preference", style = MaterialTheme.typography.titleMedium)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val genders = listOf("Boys", "Girls", "Any")
        genders.forEach { gender ->
            FilterChip(
                selected = uiState.genderAllowed == gender,
                onClick = { viewModel.updateField("genderAllowed", gender) },
                label = { Text(gender) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun Step3Amenities(uiState: AddListingUiState, viewModel: AddListingViewModel) {
    Text("Amenities Provided", style = MaterialTheme.typography.titleLarge)
    
    val commonAmenities = listOf(
        "WiFi", "AC", "Power Backup", 
        "Food Included", "Laundry", "Cleaning", 
        "Attached Washroom", "TV", "Geyser",
        "Washing Machine", "Fridge", "Water Purifier"
    )
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val rows = commonAmenities.chunked(2)
        rows.forEach { rowAmenities ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowAmenities.forEach { amenity ->
                    val isSelected = uiState.amenities.contains(amenity)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleAmenity(amenity) },
                        label = { Text(amenity) },
                        modifier = Modifier.weight(1f),
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
fun Step4Photos(uiState: AddListingUiState, viewModel: AddListingViewModel) {
    Text("Property Photos", style = MaterialTheme.typography.titleLarge)
    Text(
        text = "Add at least 3 photos showing the room, bathroom, and common areas.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        viewModel.addImages(uris)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable { launcher.launch("image/*") },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Add, contentDescription = "Add Photos", tint = MaterialTheme.colorScheme.primary)
            Text("Tap to Select Photos", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }

    // Grid for displaying selected/existing photos
    val context = LocalContext.current
    if (uiState.imageUris.isNotEmpty() || uiState.existingImageUrls.isNotEmpty()) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Use a Column of Rows instead of LazyVerticalGrid to avoid nested scrolling issues
        val allImages = buildList<Any> {
            addAll(uiState.existingImageUrls)
            addAll(uiState.imageUris)
        }
        
        val rows = allImages.chunked(3)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (rowItems in rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (item in rowItems) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(item)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Property Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            IconButton(
                                onClick = { 
                                    if (item is Uri) viewModel.removeImage(item)
                                    else if (item is String) viewModel.removeExistingImage(item)
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    // Add empty spacers to maintain strict grid alignment if row is incomplete
                    for (i in 0 until (3 - rowItems.size)) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun Step5Location(uiState: AddListingUiState, viewModel: AddListingViewModel) {
    Text("Pin Property Location", style = MaterialTheme.typography.titleLarge)
    Text(
        text = "Tap on the map to mark the exact property location. Students will use this to find your property.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp, 
                MaterialTheme.colorScheme.outlineVariant, 
                RoundedCornerShape(12.dp)
            )
    ) {
        com.example.staybuddy.ui.components.OsmMapView(
            isPickerMode = true,
            currentLocation = if (uiState.latitude != 0.0) GeoPoint(uiState.latitude, uiState.longitude) else null,
            onLocationSelected = { geoPoint ->
                viewModel.updateLocation(geoPoint.latitude, geoPoint.longitude)
            }
        )
    }

    if (uiState.latitude != 0.0) {
        Text(
            text = "Location Pinned! Lat: ${"%.4f".format(uiState.latitude)}, Lng: ${"%.4f".format(uiState.longitude)}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}
