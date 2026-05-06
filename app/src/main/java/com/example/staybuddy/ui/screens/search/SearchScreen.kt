package com.example.staybuddy.ui.screens.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.staybuddy.domain.model.AutocompletePrediction
import com.example.staybuddy.ui.components.OsmMapView
import com.example.staybuddy.ui.components.PgListingCard
import com.example.staybuddy.ui.components.map.CompactMapCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToListingDetail: (String) -> Unit,
    onNavigateToMapView: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
            ) {
                // Background Layer: Map or List
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(strokeWidth = 3.dp, modifier = Modifier.size(48.dp))
                    }
                } else if (uiState.isMapView) {
                    // MAP VIEW
                    Box(modifier = Modifier.fillMaxSize()) {
                        OsmMapView(
                            modifier = Modifier.fillMaxSize(),
                            listings = uiState.filteredListings,
                            selectedListing = uiState.selectedListing,
                            onMarkerClick = { listing -> viewModel.selectListing(listing) }
                        )

                        // Map Carousel
                        if (uiState.filteredListings.isNotEmpty()) {
                            val pagerState = rememberPagerState(pageCount = { uiState.filteredListings.size })
                            
                            LaunchedEffect(uiState.selectedListing) {
                                uiState.selectedListing?.let { selected ->
                                    val index = uiState.filteredListings.indexOfFirst { it.listingId == selected.listingId }
                                    if (index != -1 && pagerState.currentPage != index) {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                            }

                            LaunchedEffect(pagerState.currentPage) {
                                if (uiState.filteredListings.isNotEmpty()) {
                                    viewModel.selectListing(uiState.filteredListings[pagerState.currentPage])
                                }
                            }

                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 100.dp) // Leave space for Floating Toggle
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 32.dp),
                                pageSpacing = 16.dp
                            ) { page ->
                                val listing = uiState.filteredListings[page]
                                CompactMapCard(
                                    listing = listing,
                                    onCardClick = { onNavigateToListingDetail(listing.listingId) },
                                    onFavoriteClick = { viewModel.toggleFavorite(listing.listingId) },
                                    modifier = Modifier
                                        .graphicsLayer {
                                            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                                            alpha = lerp(
                                                start = 0.5f,
                                                stop = 1f,
                                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                            )
                                            scaleY = lerp(
                                                start = 0.85f,
                                                stop = 1f,
                                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                            )
                                        }
                                )
                            }
                        }
                    }
                } else {
                    // LIST VIEW
                    if (uiState.filteredListings.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No listings found.",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Try adjusting your filters.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 130.dp, bottom = 100.dp)
                        ) {
                            item {
                                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                                    Text(
                                        text = "${uiState.filteredListings.size} properties",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "found in your area",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            items(uiState.filteredListings) { listing ->
                                PgListingCard(
                                    listing = listing,
                                    onCardClick = { onNavigateToListingDetail(listing.listingId) },
                                    onFavoriteClick = { viewModel.toggleFavorite(listing.listingId) },
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }

                // Foreground layer: Floating Search Bar
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (uiState.isMapView) 
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Transparent)
                                    )
                                else 
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                    )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .statusBarsPadding()
                                .padding(bottom = 8.dp)
                        ) {
                            // Search Pill
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(28.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 8.dp,
                                tonalElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = onNavigateBack) {
                                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        TextField(
                                            value = uiState.query,
                                            onValueChange = viewModel::onQueryChange,
                                            placeholder = { 
                                                Text(
                                                    "Search PGs, hostels...", 
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                ) 
                                            },
                                            modifier = Modifier.fillMaxSize(),
                                            singleLine = true,
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                disabledContainerColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent,
                                            ),
                                            trailingIcon = {
                                                if (uiState.query.isNotEmpty()) {
                                                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.Close,
                                                            contentDescription = "Clear",
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            },
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(32.dp)
                                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    )

                                    IconButton(
                                        onClick = { viewModel.toggleFilterSheet(true) },
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FilterList, 
                                            contentDescription = "Filter",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            // Offline Banner
                            if (uiState.isOffline) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shadowElevation = 4.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudOff,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "You're offline. Showing cached results.",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }

                            // Suggestions & History
                            val showHistory = uiState.query.isEmpty() && uiState.recentSearches.isNotEmpty()
                            val showPredictions = uiState.query.isNotEmpty() && uiState.locationPredictions.isNotEmpty()
                            
                            if (showHistory || showPredictions) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 8.dp
                                ) {
                                    Column(modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (showHistory) "Recent Searches" else "Suggestions",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            if (showHistory) {
                                                TextButton(
                                                    onClick = viewModel::clearSearchHistory,
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Text("Clear All", style = MaterialTheme.typography.labelMedium)
                                                }
                                            }
                                        }
                                        
                                        val displayList = if (showHistory) uiState.recentSearches else uiState.locationPredictions
                                        displayList.forEach { prediction ->
                                            LocationSuggestionItem(
                                                prediction = prediction,
                                                icon = if (showHistory) Icons.Default.History else Icons.Default.LocationOn,
                                                onClick = { 
                                                    viewModel.selectLocation(prediction)
                                                    focusManager.clearFocus()
                                                },
                                                onRemove = if (showHistory) { { viewModel.removeFromHistory(prediction) } } else null
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Foreground layer: Floating Toggle Map/List Button
                FloatingToggleButton(
                    isMapView = uiState.isMapView,
                    onToggle = viewModel::toggleMapView,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .navigationBarsPadding()
                )
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
                    onSortOrderChange = viewModel::updateSortOption,
                    onClearFilters = viewModel::clearFilters
                )
            }
        }
    )
}

@Composable
fun FloatingToggleButton(
    isMapView: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .clickable { onToggle(!isMapView) }
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isMapView) Icons.Default.ViewList else Icons.Default.Map,
                contentDescription = if (isMapView) "List View" else "Map View",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isMapView) "List" else "Map",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
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
    onSortOrderChange: (SortOption) -> Unit,
    onClearFilters: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Refine Search",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onClearFilters) {
                    Text("Reset", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sort By
            FilterSectionHeader("Sort By")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SortOption.values().forEach { order ->
                    val isSelected = uiState.sortOption == order
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSortOrderChange(order) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 12.dp)) {
                            Text(
                                text = order.displayName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Price Range
            FilterSectionHeader("Price Range", "₹${uiState.priceRange.start.toInt()} - ₹${uiState.priceRange.endInclusive.toInt()}")
            RangeSlider(
                value = uiState.priceRange,
                onValueChange = onPriceChange,
                valueRange = 500f..30000f,
                steps = 59,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    thumbColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Gender
            FilterSectionHeader("Gender Preference")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Any", "Male", "Female").forEach { gender ->
                    val isSelected = uiState.selectedGender == gender
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onGenderChange(gender) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 12.dp)) {
                            Text(
                                text = gender,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Room Type
            FilterSectionHeader("Room Selection")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val roomTypes = listOf("Single", "Double", "Triple", "Dorm")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    roomTypes.take(2).forEach { type ->
                        RoomTypeChip(
                            text = type,
                            isSelected = uiState.selectedRoomTypes.contains(type),
                            onClick = { onRoomTypeToggle(type) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    roomTypes.drop(2).forEach { type ->
                        RoomTypeChip(
                            text = type,
                            isSelected = uiState.selectedRoomTypes.contains(type),
                            onClick = { onRoomTypeToggle(type) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Amenities
            FilterSectionHeader("Essential Amenities")
            val amenities = listOf("WiFi", "AC", "Food", "Laundry", "Power Backup", "Gym")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                amenities.chunked(3).forEach { chunk ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        chunk.forEach { amenity ->
                            AmenityFilterChip(
                                text = amenity,
                                isSelected = uiState.selectedAmenities.contains(amenity),
                                onClick = { onAmenityToggle(amenity) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Distance
            FilterSectionHeader("Max Distance", "${uiState.maxDistanceKm.toInt()} km")
            Slider(
                value = uiState.maxDistanceKm,
                onValueChange = onDistanceChange,
                valueRange = 1f..15f,
                steps = 14,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Apply Button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("Show Properties", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FilterSectionHeader(title: String, value: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun RoomTypeChip(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 12.dp)) {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LocationSuggestionItem(
    prediction: AutocompletePrediction,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.LocationOn,
    onClick: () -> Unit,
    onRemove: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = prediction.primaryText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (prediction.secondaryText.isNotEmpty()) {
                Text(
                    text = prediction.secondaryText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (onRemove != null) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Remove from history",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AmenityFilterChip(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
