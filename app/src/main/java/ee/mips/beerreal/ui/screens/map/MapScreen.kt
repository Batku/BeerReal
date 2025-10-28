package ee.mips.beerreal.ui.screens.map

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import ee.mips.beerreal.R
import ee.mips.beerreal.data.api.PlacesApiServiceImpl
import ee.mips.beerreal.data.local.AppDatabase
import ee.mips.beerreal.data.model.BarPlace
import ee.mips.beerreal.data.repository.BarRepository
import ee.mips.beerreal.data.repository.LocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * MapScreen displays a Google Map with user location and nearby bars
 * Features:
 * - Shows user location with a blue marker
 * - Displays nearby bars (open in red, closed in gray)
 * - Caches location data in Room DB
 * - Shows loading spinner while fetching data
 * - Handles errors with toast messages
 */
@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Initialize Room DB and API service
    val database = remember { AppDatabase.getDatabase(context) }
    val locationRepository = remember { LocationRepository(database.locationDao()) } // For caching user location (underground bars or bad GPS)
    val barRepository = remember { BarRepository(database.barDao()) }
    val apiService = remember { PlacesApiServiceImpl() }

    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val cameraPositionState = rememberCameraPositionState()
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val barMarkers = remember { mutableStateListOf<BarPlace>() }
    var isLoading by remember { mutableStateOf(false) }
    var isOffline by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val fine = results[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        permissionGranted = fine || coarse
        if (permissionGranted) {
            coroutineScope.launch {
                fetchAndCenterUser(
                    fusedClient, context, apiService, cameraPositionState,
                    barMarkers, locationRepository, barRepository,
                    onLocationFetched = { loc -> userLocation = loc },
                    onLoadingChange = { loading -> isLoading = loading },
                    onOfflineChange = { offline -> isOffline = offline },
                    onError = { error -> errorMessage = error }
                )
            }
        }
    }

    // Auto-request permissions or fetch location on launch
    LaunchedEffect(Unit) {
        if (permissionGranted) {
            // Try to use cached location first
            val cachedLocation = locationRepository.getRecentCachedLocation()
            if (cachedLocation != null) {
                val latLng = LatLng(cachedLocation.latitude, cachedLocation.longitude)
                userLocation = latLng
                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                // Load cached bars for this location
                val cachedBars = barRepository.getCachedBarsNearLocation(
                    cachedLocation.latitude,
                    cachedLocation.longitude
                )
                if (cachedBars.isNotEmpty()) {
                    barMarkers.clear()
                    barMarkers.addAll(cachedBars)
                    isOffline = true  // Show offline indicator since using cached data
                }
            }

            // Fetch fresh data
            fetchAndCenterUser(
                fusedClient, context, apiService, cameraPositionState,
                barMarkers, locationRepository, barRepository,
                onLocationFetched = { loc -> userLocation = loc },
                onLoadingChange = { loading -> isLoading = loading },
                onOfflineChange = { offline -> isOffline = offline },
                onError = { error -> errorMessage = error }
            )
        } else {
            launcher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    // Show error toast when error occurs
    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            errorMessage = null // Clear after showing
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            // User location marker - bright blue and more visible
            userLocation?.let { pos ->
                Marker(
                    state = MarkerState(position = pos),
                    title = "You Are Here",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                    zIndex = 1f
                )
            }

            // Bar markers - red for open, gray for closed
            barMarkers.forEach { bar ->
                val pos = LatLng(bar.latitude, bar.longitude)
                val markerColor = when (bar.isOpen) {
                    true -> BitmapDescriptorFactory.HUE_RED  // Open bars - red
                    false -> BitmapDescriptorFactory.HUE_VIOLET  // Closed bars - violet
                    null -> BitmapDescriptorFactory.HUE_ORANGE  // Unknown status - orange
                }

                val snippet = buildString {
                    bar.isOpen?.let {
                        append(if (it) "🟢 Open Now" else "🔴 Closed")
                    }
                    bar.rating?.let {
                        if (isNotEmpty()) append(" • ")
                        append("⭐ $it")
                    }
                }

                Marker(
                    state = MarkerState(position = pos),
                    title = bar.name,
                    snippet = snippet.ifEmpty { null },
                    icon = BitmapDescriptorFactory.defaultMarker(markerColor)
                )
            }
        }

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Offline indicator
        if (isOffline) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "📡 Offline - Showing cached data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

/**
 * Fetch user location, center camera, cache location in DB, and fetch nearby bars
 * Handles loading states and error cases
 * Falls back to cached bar data when offline
 *
 * @param fusedClient FusedLocationProviderClient for getting device location
 * @param context Android context
 * @param apiService PlacesApiService for fetching nearby bars
 * @param cameraPositionState Camera state for map positioning
 * @param barMarkers Mutable list to store fetched bars
 * @param locationRepository Repository for caching location
 * @param barRepository Repository for caching bar data
 * @param onLocationFetched Callback when location is fetched
 * @param onLoadingChange Callback for loading state changes
 * @param onOfflineChange Callback for offline state changes
 * @param onError Callback for error messages
 */
private suspend fun fetchAndCenterUser(
    fusedClient: com.google.android.gms.location.FusedLocationProviderClient,
    context: android.content.Context,
    apiService: PlacesApiServiceImpl,
    cameraPositionState: com.google.maps.android.compose.CameraPositionState,
    barMarkers: MutableList<BarPlace>,
    locationRepository: LocationRepository,
    barRepository: BarRepository,
    onLocationFetched: (LatLng) -> Unit,
    onLoadingChange: (Boolean) -> Unit,
    onOfflineChange: (Boolean) -> Unit,
    onError: (String) -> Unit
) {
    // Check runtime permission explicitly
    val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

    if (!hasPermission) {
        onError("Location permission not granted")
        return
    }

    withContext(Dispatchers.Main) {
        try {
            onLoadingChange(true)

            fusedClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    onLocationFetched(userLatLng)

                    // Cache location in Room DB
                    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                        try {
                            locationRepository.cacheLocation(location.latitude, location.longitude)
                        } catch (_: Exception) {
                            // Log but don't fail if caching fails
                        }
                    }

                    // Center camera on user location with good zoom level
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))

                    // Fetch nearby bars in background using API service
                    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val apiKey = context.getString(R.string.google_maps_key)
                            val bars = apiService.getNearbyBars(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                apiKey = apiKey,
                                radius = 2000
                            )

                            // Cache the fetched bars with user location
                            barRepository.cacheBars(bars, location.latitude, location.longitude)

                            // Update UI on main thread
                            withContext(Dispatchers.Main) {
                                barMarkers.clear()
                                barMarkers.addAll(bars)
                                onLoadingChange(false)
                                onOfflineChange(false)
                            }
                        } catch (_: Exception) {
                            // Network error - try to use cached bars
                            val cachedBars = barRepository.getCachedBarsNearLocation(
                                location.latitude,
                                location.longitude
                            )

                            withContext(Dispatchers.Main) {
                                onLoadingChange(false)

                                if (cachedBars.isNotEmpty()) {
                                    // Show cached bars with offline indicator
                                    barMarkers.clear()
                                    barMarkers.addAll(cachedBars)
                                    onOfflineChange(true)
                                } else {
                                    // No cached data available - show error
                                    onOfflineChange(false)
                                    onError("No internet connection and no cached data available")
                                }
                            }
                        }
                    }
                } else {
                    onLoadingChange(false)
                    onError("Unable to get current location. Please try again.")
                }
            }.addOnFailureListener { e ->
                onLoadingChange(false)
                onError("Location error: ${e.message}")
            }
        } catch (_: SecurityException) {
            onLoadingChange(false)
            onError("Location permission denied")
        } catch (_: Exception) {
            onLoadingChange(false)
            onError("Unexpected error: Unable to get location")
        }
    }
}

