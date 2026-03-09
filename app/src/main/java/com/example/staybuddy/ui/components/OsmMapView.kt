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
    
    // Manage Osmdroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        // Important: sets the user agent to avoid being blocked by OSM servers. Usually BuildConfig.APPLICATION_ID
        Configuration.getInstance().userAgentValue = context.packageName
    }
    
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
    
    // Handle Lifecycle events
    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                mapView.onResume()
            }
            override fun onPause(owner: LifecycleOwner) {
                mapView.onPause()
            }
            override fun onDestroy(owner: LifecycleOwner) {
                mapView.onDetach()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }
    
    // Update markers when listings change
    LaunchedEffect(listings) {
        // Clear all map overlays except my location overlay
        val nonMarkerOverlays = mapView.overlays.filter { it is MyLocationNewOverlay }
        mapView.overlays.clear()
        mapView.overlays.addAll(nonMarkerOverlays)
        
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
    
    // Handle user location overy
    LaunchedEffect(currentLocation) {
        if (currentLocation != null) {
            val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
            myLocationOverlay.enableMyLocation()
            // Remove previous location overlays to avoid duplicates
            mapView.overlays.removeAll { it is MyLocationNewOverlay }
            mapView.overlays.add(myLocationOverlay)
            
            // Optionally center on user
            // mapView.controller.animateTo(currentLocation)
        }
    }
    
    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}
