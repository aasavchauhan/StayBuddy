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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.surfaceColorAtElevation
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
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        Text(
                            text = if (uiState.isEditing) "Edit Post" else "Create Post",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.weight(1f)
                        )
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
            Text(
                text = "Looking for a roommate? Fill out the details below so others can find you.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Custom Form Surface for better separation
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Post Type Toggle - Premium Pill Style
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("I am...", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(modifier = Modifier.padding(4.dp)) {
                                val isOffer = uiState.postType == RoommatePostType.OFFER
                                Surface(
                                    modifier = Modifier.weight(1f).height(44.dp).clickable { viewModel.setPostType(RoommatePostType.OFFER) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isOffer) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (isOffer) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("Offering Room", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                                Surface(
                                    modifier = Modifier.weight(1f).height(44.dp).clickable { viewModel.setPostType(RoommatePostType.SEEK) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (!isOffer) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (!isOffer) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("Searching for Room", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = uiState.city,
                        onValueChange = { viewModel.updateField("city", it) },
                        label = { Text("City") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    
                    OutlinedTextField(
                        value = uiState.location,
                        onValueChange = { viewModel.updateField("location", it) },
                        label = { Text("Locality") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = uiState.address,
                        onValueChange = { viewModel.updateField("address", it) },
                        label = { Text("Complete Address (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { 
                            if (uiState.latitude != null) {
                                Text("📍 Location Pinned", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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
                        minLines = 2,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.roomType,
                        onValueChange = { viewModel.updateField("roomType", it) },
                        label = { Text("Room Type (e.g. Shared, Single)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.updateField("description", it) },
                        label = { Text("Tell us about yourself/space...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(16.dp)
                    )
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = uiState.priceShare,
                            onValueChange = { viewModel.updateField("priceShare", it) },
                            label = { Text("Monthly Budget") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            prefix = { Text("₹") }
                        )
                        
                        if (uiState.postType == RoommatePostType.OFFER) {
                            OutlinedTextField(
                                value = uiState.availableBeds,
                                onValueChange = { viewModel.updateField("availableBeds", it) },
                                label = { Text("Beds Left") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }

                    if (uiState.postType == RoommatePostType.OFFER) {
                        OutlinedTextField(
                            value = uiState.totalBeds,
                            onValueChange = { viewModel.updateField("totalBeds", it) },
                            label = { Text("Total Beds in Flat") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
            
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 8.dp, start = 8.dp)
            )
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    PreferenceCheckbox(
                        label = "Dietary Preference",
                        checkedText = "Vegetarian Only",
                        uncheckedText = "Any",
                        isChecked = uiState.preferences["Diet"] == "Vegetarian Only",
                        onCheckedChange = { checked -> 
                            viewModel.updatePreference("Diet", if (checked) "Vegetarian Only" else "")
                        }
                    )
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    
                    PreferenceCheckbox(
                        label = "Smoking Preference",
                        checkedText = "Non-Smoker",
                        uncheckedText = "Any",
                        isChecked = uiState.preferences["Smoking"] == "Non-Smoker",
                        onCheckedChange = { checked -> 
                            viewModel.updatePreference("Smoking", if (checked) "Non-Smoker" else "")
                        }
                    )
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    
                    PreferenceCheckbox(
                        label = "Pet Friendly",
                        checkedText = "No Pets Allowed",
                        uncheckedText = "Any",
                        isChecked = uiState.preferences["Pets"] == "No Pets Allowed",
                        onCheckedChange = { checked -> 
                            viewModel.updatePreference("Pets", if (checked) "No Pets Allowed" else "")
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { viewModel.submitPost() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !uiState.isLoading,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 3.dp)
                } else {
                    Text(
                        if (uiState.isEditing) "Update Your Request" else "Post Your Request",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
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
