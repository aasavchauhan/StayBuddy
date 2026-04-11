package com.example.staybuddy.ui.screens.map

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
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
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Full Screen Map
            OsmMapView(
                modifier = Modifier.fillMaxSize(),
                listings = uiState.listings,
                currentLocation = uiState.userLocation, // Pass user location if we tracked it in VM 
                selectedListing = uiState.selectedListing,
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

            if (uiState.listings.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { uiState.listings.size })

                LaunchedEffect(uiState.selectedListing, uiState.listings) {
                    val selectedId = uiState.selectedListing?.listingId
                    if (selectedId != null) {
                        val index = uiState.listings.indexOfFirst { it.listingId == selectedId }
                        if (index != -1 && pagerState.currentPage != index) {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                }

                LaunchedEffect(pagerState.currentPage) {
                    if (uiState.listings.isNotEmpty() && pagerState.currentPage < uiState.listings.size) {
                        val currentListing = uiState.listings[pagerState.currentPage]
                        if (uiState.selectedListing?.listingId != currentListing.listingId) {
                            viewModel.selectListing(currentListing)
                        }
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = paddingValues.calculateBottomPadding() + 16.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp),
                    pageSpacing = 16.dp
                ) { page ->
                    val listing = uiState.listings[page]
                    PgListingCard(
                        listing = listing,
                        onCardClick = { onNavigateToListingDetail(listing.listingId) },
                        onFavoriteClick = { /* Handle favorite */ }
                    )
                }
            }

            // Floating Action Button
            FloatingActionButton(
                onClick = {
                    if (!locationPermissionState.status.isGranted) {
                        locationPermissionState.launchPermissionRequest()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = if (uiState.listings.isNotEmpty()) 320.dp else 16.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "My Location")
            }
        }
    }
}
