package com.example.pathbuilder.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.pathbuilder.model.Route
import com.example.pathbuilder.model.Waypoint
import com.example.pathbuilder.viewmodel.MapLayer
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun MapboxMapView(
    modifier: Modifier = Modifier,
    layer: MapLayer,
    routes: List<Route>,
    activePins: List<Waypoint>,
    activeRouteGeometry: List<Waypoint> = emptyList(), // The route line being built
    onMapClick: (Waypoint) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var locationPermissionGranted by remember { mutableStateOf(false) }

    // Request location permission
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Check and request permissions
    LaunchedEffect(Unit) {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            locationPermissionGranted = true
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            setBuiltInZoomControls(true) // Show permanent zoom +/- buttons
            zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.ALWAYS)
            controller.setZoom(15.0) // Good zoom level for neighborhoods
            
            // Default center (will be overridden by location)
            controller.setCenter(GeoPoint(37.7749, -122.4194))
        }
    }

    // Add location overlay
    val locationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
            enableFollowLocation()
        }
    }

    // Center map on user location
    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val userLocation = GeoPoint(it.latitude, it.longitude)
                        mapView.controller.setCenter(userLocation)
                        mapView.controller.setZoom(15.0)
                    }
                }
            } catch (e: SecurityException) {
                // Permission denied, stay at default location
            }
        }
    }

    // Handle lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    mapView.onResume()
                    locationOverlay.enableMyLocation()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    mapView.onPause()
                    locationOverlay.disableMyLocation()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            locationOverlay.disableMyLocation()
            mapView.onDetach()
        }
    }

    // Update tile source based on layer
    LaunchedEffect(layer) {
        mapView.setTileSource(
            when (layer) {
                MapLayer.STANDARD -> TileSourceFactory.MAPNIK
                MapLayer.SATELLITE -> TileSourceFactory.USGS_SAT
            }
        )
    }

    // Handle map clicks
    LaunchedEffect(Unit) {
        mapView.overlays.add(locationOverlay)
        mapView.overlays.add(
            org.osmdroid.views.overlay.MapEventsOverlay(
                object : org.osmdroid.events.MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        onMapClick(
                            Waypoint(
                                latitude = p.latitude,
                                longitude = p.longitude,
                                elevationFeet = 0.0
                            )
                        )
                        return true
                    }

                    override fun longPressHelper(p: GeoPoint): Boolean = false
                }
            )
        )
    }

    // Draw routes and pins
    LaunchedEffect(routes, activePins, activeRouteGeometry) {
        // Clear existing overlays except location and events overlay
        val locationOv = mapView.overlays.find { it is MyLocationNewOverlay }
        val eventsOv = mapView.overlays.find { it is org.osmdroid.views.overlay.MapEventsOverlay }
        mapView.overlays.clear()
        
        if (locationOv != null) mapView.overlays.add(locationOv)
        if (eventsOv != null) mapView.overlays.add(eventsOv)

        // Draw saved routes as polylines using routeGeometry
        routes.forEach { route ->
            val geometryToUse = if (route.routeGeometry.isNotEmpty()) 
                route.routeGeometry 
            else 
                route.waypoints
                
            if (geometryToUse.size >= 2) {
                val polyline = Polyline(mapView).apply {
                    outlinePaint.color = android.graphics.Color.parseColor("#4F8EF7")
                    outlinePaint.strokeWidth = 8f
                    setPoints(geometryToUse.map { GeoPoint(it.latitude, it.longitude) })
                    
                    // Make polyline clickable and show route info
                    title = route.name
                    snippet = "${String.format("%.2f", route.distanceMiles)} mi • ${route.difficulty} • ${route.type.replaceFirstChar { it.uppercase() }}"
                    setOnClickListener { polyline, mapView, eventPos ->
                        // Show info window at the middle of the route
                        val midPoint = geometryToUse[geometryToUse.size / 2]
                        val marker = Marker(mapView)
                        marker.position = GeoPoint(midPoint.latitude, midPoint.longitude)
                        marker.title = route.name
                        marker.snippet = """
                            Distance: ${String.format("%.2f", route.distanceMiles)} mi
                            Elevation: ${String.format("%.0f", route.elevationFeet)} ft
                            Time: ${route.estimatedTimeMinutes} min
                            Type: ${route.type.replaceFirstChar { it.uppercase() }}
                            Difficulty: ${route.difficulty}
                        """.trimIndent()
                        marker.showInfoWindow()
                        true
                    }
                }
                mapView.overlays.add(polyline)
            }
        }

        // Draw active route geometry as BLACK LINE
        if (activeRouteGeometry.size >= 2) {
            val activePolyline = Polyline(mapView).apply {
                outlinePaint.color = android.graphics.Color.BLACK
                outlinePaint.strokeWidth = 10f
                setPoints(activeRouteGeometry.map { GeoPoint(it.latitude, it.longitude) })
            }
            mapView.overlays.add(activePolyline)
        }

        // Draw active pins as markers
        activePins.forEachIndexed { index, waypoint ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(waypoint.latitude, waypoint.longitude)
                title = "Pin ${index + 1}"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                
                // Last pin is different color for emphasis
                if (index == activePins.lastIndex) {
                    icon = ContextCompat.getDrawable(context, android.R.drawable.presence_online)
                    icon?.setTint(android.graphics.Color.parseColor("#00FF00")) // Green for latest pin
                } else {
                    icon = ContextCompat.getDrawable(context, android.R.drawable.presence_busy)
                    icon?.setTint(android.graphics.Color.RED)
                }
            }
            mapView.overlays.add(marker)
        }

        mapView.invalidate()
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}
