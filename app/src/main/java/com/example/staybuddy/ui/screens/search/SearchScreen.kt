package com.example.staybuddy.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.staybuddy.ui.components.OsmMapView
import com.example.staybuddy.ui.components.PgListingCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToListingDetail: (String) -> Unit,
    onNavigateToMapView: () -> Unit, // This might be redundant if we have toggle, but keeping for compatibility
    onNavigateBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = { Text("Search PGs, hostels...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFilterSheet(true) }) {
                        Icon(imageVector = Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Map/List Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                SegmentedButtonToggleGroup(
                    isMapView = uiState.isMapView,
                    onToggle = viewModel::toggleMapView
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.isMapView) {
                // Map View
                OsmMapView(
                    modifier = Modifier.fillMaxSize(),
                    listings = uiState.filteredListings,
                    onMarkerClick = { listing -> onNavigateToListingDetail(listing.listingId) }
                )
            } else {
                // List View
                if (uiState.filteredListings.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No listings found matching your criteria.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(uiState.filteredListings) { listing ->
                            PgListingCard(
                                listing = listing,
                                onCardClick = { onNavigateToListingDetail(listing.listingId) },
                                onFavoriteClick = { viewModel.toggleFavorite(listing.listingId) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Filter Bottom Sheet
        if (uiState.isFilterSheetVisible) {
            FilterBottomSheet(
                uiState = uiState,
                onDismiss = { viewModel.toggleFilterSheet(false) },
                onPriceChange = viewModel::updatePriceRange,
                onRoomTypeToggle = viewModel::toggleRoomType,
                onGenderChange = viewModel::setGenderFilter,
                onAmenityToggle = viewModel::toggleAmenity,
                onDistanceChange = viewModel::updateMaxDistance,
                onClearFilters = viewModel::clearFilters
            )
        }
    }
}

@Composable
fun SegmentedButtonToggleGroup(isMapView: Boolean, onToggle: (Boolean) -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            // List Button
            Surface(
                modifier = Modifier.clickable { onToggle(false) },
                shape = RoundedCornerShape(20.dp),
                color = if (!isMapView) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                contentColor = if (!isMapView) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.ViewList, contentDescription = "List", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("List", style = MaterialTheme.typography.labelLarge)
                }
            }
            // Map Button
            Surface(
                modifier = Modifier.clickable { onToggle(true) },
                shape = RoundedCornerShape(20.dp),
                color = if (isMapView) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                contentColor = if (isMapView) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Map, contentDescription = "Map", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Map", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    uiState: SearchUiState,
    onDismiss: () -> Unit,
    onPriceChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onRoomTypeToggle: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onAmenityToggle: (String) -> Unit,
    onDistanceChange: (Float) -> Unit,
    onClearFilters: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close Filters")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price Range
            Text(
                text = "Price Range (₹${uiState.priceRange.start.toInt()} - ₹${uiState.priceRange.endInclusive.toInt()})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            RangeSlider(
                value = uiState.priceRange,
                onValueChange = onPriceChange,
                valueRange = 500f..30000f,
                steps = 59,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Gender
            Text(
                text = "Gender",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Any", "Male", "Female").forEach { gender ->
                    FilterChip(
                        selected = uiState.selectedGender == gender,
                        onClick = { onGenderChange(gender) },
                        label = { Text(gender) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Room Type
            Text(
                text = "Room Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Accommodate multiple rows if necessary - for now simple row since 4 short items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Single", "Double", "Triple", "Dorm").forEach { type ->
                    FilterChip(
                        selected = uiState.selectedRoomTypes.contains(type),
                        onClick = { onRoomTypeToggle(type) },
                        label = { Text(type) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amenities
            Text(
                text = "Amenities",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.foundation.lazy.LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(listOf("WiFi", "AC", "Food", "Laundry", "Power Backup", "Gym")) { amenity ->
                    FilterChip(
                        selected = uiState.selectedAmenities.contains(amenity),
                        onClick = { onAmenityToggle(amenity) },
                        label = { Text(amenity) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Distance
            Text(
                text = "Max Distance: ${uiState.maxDistanceKm.toInt()} km",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Slider(
                value = uiState.maxDistanceKm,
                onValueChange = onDistanceChange,
                valueRange = 1f..10f,
                steps = 8,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Footer Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onClearFilters,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear All")
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply Filters")
                }
            }
        }
    }
}

