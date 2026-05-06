package com.example.staybuddy.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.staybuddy.data.model.PgListing
import com.example.staybuddy.ui.components.map.PriceBubbleUtils
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
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
    listings: List<PgListing> = emptyList(),
    currentLocation: GeoPoint? = null,
    selectedListing: PgListing? = null,
    isPickerMode: Boolean = false,
    myLocationTrigger: Int = 0,
    onLocationSelected: (GeoPoint) -> Unit = {},
    onMarkerClick: (PgListing) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            setMultiTouchControls(true)

            currentLocation?.let { loc ->
                controller.setZoom(14.0)
                controller.setCenter(loc)
            } ?: run {
                controller.setZoom(12.0)
                controller.setCenter(GeoPoint(22.3072, 73.1812))
            }
        }
    }

    val myLocationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
        }
    }

    val pickerMarker = remember {
        Marker(mapView).apply {
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Pinned Location"
        }
    }

    val clusterer = remember {
        RadiusMarkerClusterer(context).apply {
            // Default icon is a folder from resources but let's build a circular cluster icon
            val clusterIcon = createClusterIcon(context)
            setIcon(clusterIcon)
        }
    }

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

    // Animate map when selected listing changes or update marker colors
    LaunchedEffect(selectedListing) {
        selectedListing?.let {
            if (it.latitude != 0.0 && it.longitude != 0.0) {
                mapView.controller.animateTo(GeoPoint(it.latitude, it.longitude))
            }
        }
        
        if (!isPickerMode) {
            clusterer.items.forEach { item ->
                if (item is Marker) {
                    val listingId = item.id
                    val isSelected = (listingId == selectedListing?.listingId)
                    val listing = listings.find { it.listingId == listingId }
                    if (listing != null) {
                        item.icon = PriceBubbleUtils.createPriceBubbleDrawable(context, listing.price, isSelected)
                    }
                }
            }
            clusterer.invalidate()
            mapView.invalidate()
        }
    }
    
    // Handle "My Location" trigger
    LaunchedEffect(myLocationTrigger) {
        if (myLocationTrigger > 0) {
            val loc = myLocationOverlay.myLocation
            if (loc != null) {
                mapView.controller.animateTo(loc)
            } else {
                currentLocation?.let { mapView.controller.animateTo(it) }
            }
        }
    }

    LaunchedEffect(listings, currentLocation, isPickerMode) {
        mapView.overlays.clear()
        mapView.overlays.add(myLocationOverlay)

        if (isPickerMode) {
            val eventsOverlay = org.osmdroid.views.overlay.MapEventsOverlay(object : org.osmdroid.events.MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                    pickerMarker.position = p
                    if (!mapView.overlays.contains(pickerMarker)) {
                        mapView.overlays.add(pickerMarker)
                    }
                    onLocationSelected(p)
                    mapView.invalidate()
                    return true
                }
                override fun longPressHelper(p: GeoPoint): Boolean = false
            })
            mapView.overlays.add(eventsOverlay)

            currentLocation?.let {
                if (it.latitude != 0.0 || it.longitude != 0.0) {
                    pickerMarker.position = it
                    if (!mapView.overlays.contains(pickerMarker)) {
                        mapView.overlays.add(pickerMarker)
                    }
                }
            }
        } else {
            clusterer.items.clear()
            listings.forEach { listing ->
                if (listing.latitude != 0.0 && listing.longitude != 0.0) {
                    val isSelected = (listing.listingId == selectedListing?.listingId)
                    val marker = Marker(mapView)
                    marker.id = listing.listingId
                    marker.position = GeoPoint(listing.latitude, listing.longitude)
                    marker.title = listing.title
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    
                    marker.icon = PriceBubbleUtils.createPriceBubbleDrawable(context, listing.price, isSelected)
                    
                    marker.setOnMarkerClickListener { _, _ ->
                        onMarkerClick(listing)
                        true
                    }
                    clusterer.items.add(marker)
                    
                    // Note: Osmdroid's RadiusMarkerClusterer does not immediately render the selection above clusters if part of it. 
                    // To handle this perfectly, we might add selected markers separate from the clusterer.
                    // But RadiusMarkerClusterer doesn't easily let us say "cluster everything except this".
                    // For now, this is okay.
                }
            }
            mapView.overlays.add(clusterer)
        }
        
        // Ensure clusterer redraws if anything changed
        clusterer.invalidate()
        mapView.invalidate()
    }

    AndroidView(
        factory = { 
            mapView.apply {
                setOnTouchListener { v, _ ->
                    v.parent.requestDisallowInterceptTouchEvent(true)
                    false
                }
            }
        },
        modifier = modifier
    )
}

private fun createClusterIcon(context: Context): Bitmap {
    val scale = context.resources.displayMetrics.density
    val size = (40f * scale).toInt()
    
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4B5563") // Gray for clusters
    }
    
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
    
    paint.color = Color.WHITE
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 2f * scale
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - paint.strokeWidth/2, paint)
    
    return bitmap
}
