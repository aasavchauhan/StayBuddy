package com.example.staybuddy.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@Composable
fun LocationPicker(
    initialLocation: GeoPoint? = null,
    onLocationSelected: (GeoPoint) -> Unit,
    modifier: Modifier = Modifier
) {
    // Default to Vadodara if no initial location
    val startPoint = initialLocation ?: GeoPoint(22.3072, 73.1812)
    
    var mapView: MapView? by remember { mutableStateOf(null) }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                    controller.setCenter(startPoint)
                    mapView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Visual indicator in center of screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp), // Height of icon adjustment
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Pin Location",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 4.dp
        ) {
            Button(
                onClick = {
                    mapView?.let {
                        val center = it.mapCenter as GeoPoint
                        onLocationSelected(center)
                    }
                },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text("Pin this location")
            }
        }
    }
}
