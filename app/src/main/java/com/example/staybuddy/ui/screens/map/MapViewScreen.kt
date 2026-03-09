package com.example.staybuddy.ui.screens.map

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.staybuddy.ui.components.OsmMapView
import com.example.staybuddy.ui.components.PgListingCard
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapViewScreen(
    onNavigateToListingDetail: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Location permission state
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Request permission when screen opens
    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (!locationPermissionState.status.isGranted) {
                        locationPermissionState.launchPermissionRequest()
                    }
                    // The Map component itself handles showing location if enabled, 
                    // but we can pass a specific intent here if we integrated FusedLocationProviderClient.
                    // For now, OsmMapView uses GpsMyLocationProvider implicitly when we enable it in the component.
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(bottom = if (uiState.selectedListing != null) 200.dp else 16.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "My Location")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Full Screen Map
            OsmMapView(
                modifier = Modifier.fillMaxSize(),
                listings = uiState.listings,
                currentLocation = uiState.userLocation, // Pass user location if we tracked it in VM 
                onMarkerClick = { listing ->
                    viewModel.selectListing(listing)
                }
            )
            
            // Top App Bar overlaid on map
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = paddingValues.calculateTopPadding(), start = 16.dp, end = 16.dp, bottom = 16.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Map View",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            // Bottom Sheet for selected listing
            if (uiState.selectedListing != null) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.selectListing(null) },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .padding(bottom = 32.dp)
                    ) {
                        Text(
                            text = "Selected Listing",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        PgListingCard(
                            listing = uiState.selectedListing!!,
                            onCardClick = { 
                                onNavigateToListingDetail(uiState.selectedListing!!.listingId)
                                viewModel.selectListing(null)
                            },
                            onFavoriteClick = { /* Handle favorite */ }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { 
                                onNavigateToListingDetail(uiState.selectedListing!!.listingId)
                                viewModel.selectListing(null)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View Details")
                        }
                    }
                }
            }
        }
    }
}
