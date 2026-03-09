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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRoommatePostScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddRoommatePostViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
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
                title = { Text("Post Roommate Request") },
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
                label = { Text("Area/Locality (e.g. Koramangala)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
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
                
                OutlinedTextField(
                    value = uiState.availableBeds,
                    onValueChange = { viewModel.updateField("availableBeds", it) },
                    label = { Text("Beds Available") },
                    modifier = Modifier.weight(1f),
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
                    Text("Post Request")
                }
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
