package com.example.staybuddy.ui.screens.roommate

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.staybuddy.data.model.RoommatePostType
import com.example.staybuddy.ui.components.LocationPicker
import org.osmdroid.util.GeoPoint
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.MyLocation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRoommatePostScreen(
    onNavigateBack: () -> Unit,
    onPostAdded: () -> Unit,
    viewModel: AddRoommatePostViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var showMapPicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onPostAdded()
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
                title = { Text(if (uiState.isEditing) "Edit Roommate Request" else "Post Roommate Request") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
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
            Text(
                text = "Looking for a roommate? Fill out the details below so others can find you.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Post Type Toggle
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("I am...", style = MaterialTheme.typography.labelLarge)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = uiState.postType == RoommatePostType.OFFER,
                        onClick = { viewModel.setPostType(RoommatePostType.OFFER) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("Offering a Room")
                    }
                    SegmentedButton(
                        selected = uiState.postType == RoommatePostType.SEEK,
                        onClick = { viewModel.setPostType(RoommatePostType.SEEK) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("Searching for a Room")
                    }
                }
            }
            
            OutlinedTextField(
                value = uiState.city,
                onValueChange = { viewModel.updateField("city", it) },
                label = { Text("City (e.g. Vadodara)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = uiState.location,
                onValueChange = { viewModel.updateField("location", it) },
                label = { Text("Area/Locality (e.g. Sayajigunj)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.address,
                onValueChange = { viewModel.updateField("address", it) },
                label = { Text("Full Address (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { 
                    if (uiState.latitude != null) {
                        Text("Location Pinned: ${uiState.latitude}, ${uiState.longitude}", color = MaterialTheme.colorScheme.primary)
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { showMapPicker = true }) {
                        Icon(
                            imageVector = if (uiState.latitude != null) Icons.Default.MyLocation else Icons.Default.PinDrop,
                            contentDescription = "Pin on Map",
                            tint = if (uiState.latitude != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                minLines = 2
            )

            OutlinedTextField(
                value = uiState.roomType,
                onValueChange = { viewModel.updateField("roomType", it) },
                label = { Text("Room Type (e.g. Shared, Single, Flat)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateField("description", it) },
                label = { Text("Description (e.g. Looking for a quiet roommate...)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = uiState.priceShare,
                    onValueChange = { viewModel.updateField("priceShare", it) },
                    label = { Text("Budget (₹/mo)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                if (uiState.postType == RoommatePostType.OFFER) {
                    OutlinedTextField(
                        value = uiState.availableBeds,
                        onValueChange = { viewModel.updateField("availableBeds", it) },
                        label = { Text("Beds Available") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }

            if (uiState.postType == RoommatePostType.OFFER) {
                OutlinedTextField(
                    value = uiState.totalBeds,
                    onValueChange = { viewModel.updateField("totalBeds", it) },
                    label = { Text("Total Beds in Room/Flat") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium
            )
            
            // Preference checkboxes
            PreferenceCheckbox(
                label = "Diet",
                checkedText = "Vegetarian Only",
                uncheckedText = "Any",
                isChecked = uiState.preferences["Diet"] == "Vegetarian Only",
                onCheckedChange = { checked -> 
                    viewModel.updatePreference("Diet", if (checked) "Vegetarian Only" else "")
                }
            )
            
            PreferenceCheckbox(
                label = "Smoking",
                checkedText = "Non-Smoker",
                uncheckedText = "Any",
                isChecked = uiState.preferences["Smoking"] == "Non-Smoker",
                onCheckedChange = { checked -> 
                    viewModel.updatePreference("Smoking", if (checked) "Non-Smoker" else "")
                }
            )
            
            PreferenceCheckbox(
                label = "Pets",
                checkedText = "No Pets Allowed",
                uncheckedText = "Any",
                isChecked = uiState.preferences["Pets"] == "No Pets Allowed",
                onCheckedChange = { checked -> 
                    viewModel.updatePreference("Pets", if (checked) "No Pets Allowed" else "")
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { viewModel.submitPost() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(if (uiState.isEditing) "Update Request" else "Post Request")
                }
            }
        }
    }

    if (showMapPicker) {
        ModalBottomSheet(
            onDismissRequest = { showMapPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Box(modifier = Modifier.fillMaxHeight(0.8f)) {
                LocationPicker(
                    initialLocation = if (uiState.latitude != null) GeoPoint(uiState.latitude!!, uiState.longitude!!) else null,
                    onLocationSelected = { geoPoint ->
                        viewModel.updateLocation(geoPoint.latitude, geoPoint.longitude, uiState.address)
                        showMapPicker = false
                    }
                )
            }
        }
    }
}

@Composable
fun PreferenceCheckbox(
    label: String,
    checkedText: String,
    uncheckedText: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = if (isChecked) checkedText else uncheckedText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}
