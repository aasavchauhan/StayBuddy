package com.example.staybuddy.ui.screens.map

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.staybuddy.ui.components.OsmMapView
import com.example.staybuddy.ui.components.map.CompactMapCard
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
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

    // Trigger to center map on user location
    var myLocationTrigger by remember { mutableStateOf(0) }

    // Request permission when screen opens
    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0) // Let content draw behind system bars if needed
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Full Screen Map
            OsmMapView(
                modifier = Modifier.fillMaxSize(),
                listings = uiState.listings,
                currentLocation = uiState.userLocation, 
                selectedListing = uiState.selectedListing,
                myLocationTrigger = myLocationTrigger,
                onMarkerClick = { listing ->
                    viewModel.selectListing(listing)
                }
            )
            
            // Top Action Area (Floating Back Button)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = paddingValues.calculateTopPadding() + 16.dp, start = 16.dp, end = 16.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.TopStart),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Navigate Back")
                }
            }

            // Bottom Area (FAB + Pager)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = paddingValues.calculateBottomPadding() + 16.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Location FAB
                FloatingActionButton(
                    onClick = {
                        if (!locationPermissionState.status.isGranted) {
                            locationPermissionState.launchPermissionRequest()
                        } else {
                            myLocationTrigger++
                        }
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                    modifier = Modifier.padding(end = 16.dp, bottom = 16.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                }

                AnimatedVisibility(
                    visible = uiState.listings.isNotEmpty(),
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
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
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 32.dp),
                        pageSpacing = 16.dp
                    ) { page ->
                        val listing = uiState.listings[page]
                        CompactMapCard(
                            listing = listing,
                            onCardClick = { onNavigateToListingDetail(listing.listingId) },
                            showFavorite = false
                        )
                    }
                }
            }
        }
    }
}
