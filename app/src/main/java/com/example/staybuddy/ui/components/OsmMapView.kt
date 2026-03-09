package com.example.staybuddy.ui.components

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.staybuddy.data.model.PgListing
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    listings: List<PgListing>,
    currentLocation: GeoPoint? = null,
    onMarkerClick: (PgListing) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Remember map view to be used across recompositions but managed by AndroidView
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            setMultiTouchControls(true)
            
            // Set initial center if provided
            currentLocation?.let { loc ->
                controller.setZoom(14.0)
                controller.setCenter(loc)
            } ?: run {
                // Default to Bangalore
                controller.setZoom(12.0)
                controller.setCenter(GeoPoint(12.9716, 77.5946))
            }
        }
    }
    
    // Handle user location overlay with lifecycle
    val myLocationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
        }
    }
    
    // Handle Lifecycle events
    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                mapView.onResume()
                myLocationOverlay.enableMyLocation()
            }
            override fun onPause(owner: LifecycleOwner) {
                mapView.onPause()
                myLocationOverlay.disableMyLocation()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            myLocationOverlay.disableMyLocation()
            mapView.onDetach()
        }
    }
    
    // Update markers when listings change
    LaunchedEffect(listings, currentLocation) {
        mapView.overlays.clear()
        
        // Add location overlay
        mapView.overlays.add(myLocationOverlay)
        
        // Add markers
        listings.forEach { listing ->
            if (listing.latitude != 0.0 && listing.longitude != 0.0) {
                val marker = Marker(mapView)
                marker.position = GeoPoint(listing.latitude, listing.longitude)
                marker.title = listing.title
                marker.snippet = "₹${listing.price}/mo"
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                
                marker.setOnMarkerClickListener { _, _ ->
                    onMarkerClick(listing)
                    true
                }
                
                mapView.overlays.add(marker)
            }
        }
        mapView.invalidate()
    }
    
    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}
