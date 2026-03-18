package com.example.staybuddy.ui.screens.listing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.animation.*
import androidx.compose.animation.core.*

import com.example.staybuddy.ui.components.OsmMapView
import org.osmdroid.util.GeoPoint

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ListingDetailScreen(
    listingId: String,
    onNavigateToChat: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ListingDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var showInquiryDialog by remember { mutableStateOf(false) }
    
    // Determine top bar transparency based on scroll
    val topBarAlpha = (scrollState.value / 300f).coerceIn(0f, 1f)

    if (uiState.isInquirySent) {
        AlertDialog(
            onDismissRequest = { /* Handle reset? For now just navigate back or show toast */ },
            title = { Text("Inquiry Sent! 📩") },
            text = { Text("The property manager will get back to you soon. You can also chat with them directly.") },
            confirmButton = {
                Button(onClick = { onNavigateBack() }) {
                    Text("Great!")
                }
            }
        )
    }

    if (showInquiryDialog) {
        InquiryDialog(
            listing = uiState.listing!!,
            onDismiss = { showInquiryDialog = false },
            onConfirm = { date, type, msg ->
                viewModel.sendInquiry(date, type, msg)
                showInquiryDialog = false
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (uiState.listing != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 24.dp,
                    tonalElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "₹${uiState.listing!!.price}",
                                style = MaterialTheme.typography.displaySmall.copy(fontSize = 24.sp),
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "per month",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        FilledTonalIconButton(
                            onClick = { /* TODO: Call intent */ },
                            modifier = Modifier.size(52.dp),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(24.dp))
                        }
                        
                        Button(
                            onClick = { showInquiryDialog = true },
                            modifier = Modifier
                                .height(56.dp)
                                .weight(1.5f),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Text("Inquire Now", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                }
            } else if (uiState.listing != null) {
                val listing = uiState.listing!!
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Image Carousel with extended height and gradient overlay
                    val pagerState = rememberPagerState(pageCount = { 
                        if (listing.images.isEmpty()) 1 else listing.images.size 
                    })
                    
                    Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(if (listing.images.isNotEmpty()) listing.images[page] else null)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Listing Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Gradient Overlays
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .align(Alignment.TopCenter)
                                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent)))
                        )
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .align(Alignment.BottomCenter)
                                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f))))
                        )
                        
                        // Refined Pager Indicators
                        if (listing.images.size > 1) {
                            Row(
                                Modifier
                                    .wrapContentHeight()
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 40.dp)
                                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(listing.images.size) { iteration ->
                                    val width by animateDpAsState(
                                        targetValue = if (pagerState.currentPage == iteration) 18.dp else 6.dp,
                                        label = "indicatorWidth"
                                    )
                                    val alpha by animateFloatAsState(
                                        targetValue = if (pagerState.currentPage == iteration) 1f else 0.5f,
                                        label = "indicatorAlpha"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .padding(2.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = alpha))
                                            .height(6.dp)
                                            .width(width)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Content Details
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(initialOffsetY = { 40 }) + fadeIn(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .offset(y = (-32).dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)) {
                        // Title and Rating
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = listing.title,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Surface(
                                color = Color(0xFFFFB300).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (listing.rating > 0) "%.1f".format(listing.rating) else "NEW",
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFFE65100)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Badge(listing.roomType.uppercase(), MaterialTheme.colorScheme.primaryContainer)
                            Badge(listing.genderAllowed.uppercase(), MaterialTheme.colorScheme.secondaryContainer)
                            
                            if (listing.availableBeds > 0) {
                                Badge("${listing.availableBeds} BEDS LEFT", MaterialTheme.colorScheme.errorContainer)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Location Info Card
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = listing.area,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = listing.city,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // About Section
                        SectionTitle("About this PG")
                        Text(
                            text = listing.description,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 26.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Amenities Section
                        SectionTitle("Amenities")
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listing.amenities.forEach { amenity ->
                                AmenityChip(amenity)
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Map Section
                        SectionTitle("Neighborhood")
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (listing.latitude != 0.0 && listing.longitude != 0.0) {
                                    OsmMapView(
                                        listings = listOf(listing),
                                        currentLocation = GeoPoint(listing.latitude, listing.longitude),
                                        onMarkerClick = {}
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Owner Info
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(56.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(14.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "PG Manager",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Verified Property Owner",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
            
            // Floating Back and Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = topBarAlpha),
                                MaterialTheme.colorScheme.surface.copy(alpha = topBarAlpha),
                                Color.Transparent
                            )
                        )
                    )
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Color.Black.copy(alpha = (0.4f * (1 - topBarAlpha)).coerceAtLeast(0f)),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Back",
                            tint = if (topBarAlpha > 0.5f) MaterialTheme.colorScheme.onSurface else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    if (topBarAlpha > 0.7f) {
                        Text(
                            text = uiState.listing?.title ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(start = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Spacer(Modifier.weight(1f))
                    
                    IconButton(
                        onClick = viewModel::toggleFavorite,
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Color.Black.copy(alpha = (0.4f * (1 - topBarAlpha)).coerceAtLeast(0f)),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (uiState.isFavorite) Color.Red else if (topBarAlpha > 0.5f) MaterialTheme.colorScheme.onSurface else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
        }
    }
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InquiryDialog(
    listing: com.example.staybuddy.data.model.PgListing,
    onDismiss: () -> Unit,
    onConfirm: (Long, String, String) -> Unit
) {
    var moveInDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedRoomType by remember { mutableStateOf(listing.roomType) }
    var message by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = moveInDate)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    moveInDate = datePickerState.selectedDateMillis ?: moveInDate
                    showDatePicker = false
                }) { Text("Confirm") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onConfirm(moveInDate, selectedRoomType, message) }) {
                Text("Confirm Inquiry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Property Inquiry", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Let the owner know you're interested in ${listing.title}.")
                
                // Date Picker Trigger
                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Preferred Move-in Date", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(moveInDate)),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Room Type (Optional selection if multiple allowed, for now just shows current)
                OutlinedTextField(
                    value = selectedRoomType,
                    onValueChange = { selectedRoomType = it },
                    label = { Text("Preferred Room Type") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Message
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message to Owner (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("e.g. I'm a student at MSU...") }
                )
            }
        }
    )
}

@Composable
fun Badge(text: String, containerColor: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, containerColor.copy(alpha = 0.3f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            color = containerColor.copy(alpha = 1f).takeOrElse { MaterialTheme.colorScheme.primary },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
fun AmenityChip(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle, 
                contentDescription = null, 
                modifier = Modifier.size(18.dp), 
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text, 
                style = MaterialTheme.typography.bodyMedium, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

