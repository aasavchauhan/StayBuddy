package com.example.staybuddy.ui.screens.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.staybuddy.ui.screens.roommate.PreferenceCheckbox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddListingScreen(
    onNavigateBack: () -> Unit,
    onListingAdded: () -> Unit,
    viewModel: AddListingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
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
                    Text("Add Property (Step ${uiState.currentStep} of 3)") 
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
                        if (uiState.currentStep < 3) viewModel.nextStep() else viewModel.submitListing() 
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(if (uiState.currentStep < 3) "Next" else "Publish")
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
                3 -> Step3AmenitiesPhotos(uiState, viewModel)
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
        label = { Text("Property Name") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FilterChip(
            selected = uiState.type == "PG",
            onClick = { viewModel.updateField("type", "PG") },
            label = { Text("PG") },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = uiState.type == "Hostel",
            onClick = { viewModel.updateField("type", "Hostel") },
            label = { Text("Hostel") },
            modifier = Modifier.weight(1f)
        )
    }
    
    OutlinedTextField(
        value = uiState.city,
        onValueChange = { viewModel.updateField("city", it) },
        label = { Text("City (e.g. Bangalore)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    
    OutlinedTextField(
        value = uiState.location,
        onValueChange = { viewModel.updateField("location", it) },
        label = { Text("Area/Locality (e.g. Indiranagar)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    
    OutlinedTextField(
        value = uiState.address,
        onValueChange = { viewModel.updateField("address", it) },
        label = { Text("Full Address") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3
    )
}

@Composable
fun Step2PricingDetails(uiState: AddListingUiState, viewModel: AddListingViewModel) {
    Text("Pricing & Details", style = MaterialTheme.typography.titleLarge)
    
    OutlinedTextField(
        value = uiState.monthlyRent,
        onValueChange = { viewModel.updateField("monthlyRent", it) },
        label = { Text("Monthly Rent (₹)") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
    
    OutlinedTextField(
        value = uiState.depositAmount,
        onValueChange = { viewModel.updateField("depositAmount", it) },
        label = { Text("Deposit Amount (₹)") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
    
    OutlinedTextField(
        value = uiState.description,
        onValueChange = { viewModel.updateField("description", it) },
        label = { Text("Property Description") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 5,
        placeholder = { Text("Tell students why they should choose your property...") }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step3AmenitiesPhotos(uiState: AddListingUiState, viewModel: AddListingViewModel) {
    Text("Amenities & Photos", style = MaterialTheme.typography.titleLarge)
    
    Text("Select Amenities provided:", style = MaterialTheme.typography.bodyMedium)
    
    val commonAmenities = listOf("WiFi", "AC", "Power Backup", "Food Included", "Laundry", "Cleaning", "Attached Bathroom", "TV")
    
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        commonAmenities.forEach { amenity ->
            FilterChip(
                selected = uiState.amenities.contains(amenity),
                onClick = { viewModel.toggleAmenity(amenity) },
                label = { Text(amenity) },
                leadingIcon = if (uiState.amenities.contains(amenity)) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )
        }
    }
    
    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
    
    Text("Photos (Optional)", style = MaterialTheme.typography.titleMedium)
    Text(
        text = "Enter comma-separated direct image URLs. Leave blank to use a default placeholder image.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    
    OutlinedTextField(
        value = uiState.imageUrls,
        onValueChange = { viewModel.updateField("imageUrls", it) },
        label = { Text("Image URLs") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3
    )
}
