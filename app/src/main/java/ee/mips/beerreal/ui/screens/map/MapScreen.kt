// kotlin
package ee.mips.beerreal.ui.screens.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import ee.mips.beerreal.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val client = remember { OkHttpClient() }

    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionGranted = granted
        if (granted) {
            // trigger location fetch once permission is granted
            coroutineScope.launch { fetchAndCenterUser(fusedClient, context, client, coroutineScope) }
        }
    }

    val cameraPositionState = rememberCameraPositionState()
    val markers = remember { mutableStateListOf<Pair<LatLng, String>>() }

    // If permission already granted, fetch location and nearby bars on composition
    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            fetchAndCenterUser(fusedClient, context, client, coroutineScope, cameraPositionState, markers)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = permissionGranted),
            uiSettings = MapUiSettings(myLocationButtonEnabled = true)
        ) {
            // markers (user + bars)
            markers.forEach { (pos, title) ->
                Marker(state = MarkerState(position = pos), title = title)
            }
        }

        // Simple overlay: request permission or refresh
        Box(modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)) {
            if (!permissionGranted) {
                Button(onClick = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) {
                    Text(text = context.getString(R.string.allow_location))
                }
            } else {
                IconButton(onClick = {
                    coroutineScope.launch {
                        fetchAndCenterUser(fusedClient, context, client, coroutineScope, cameraPositionState, markers, forceRefresh = true)
                    }
                }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// Helper: fetch last location, center camera, add user marker and fetch nearby bars
private suspend fun fetchAndCenterUser(
    fusedClient: com.google.android.gms.location.FusedLocationProviderClient,
    context: android.content.Context,
    client: OkHttpClient,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    cameraPositionState: com.google.maps.android.compose.CameraPositionState? = null,
    markers: MutableList<Pair<LatLng, String>>? = null,
    forceRefresh: Boolean = false
) {
    withContext(Dispatchers.Main) {
        fusedClient.lastLocation.addOnSuccessListener { location: Location? ->
            val loc = location ?: return@addOnSuccessListener
            val userLatLng = LatLng(loc.latitude, loc.longitude)

            // center camera
            if (cameraPositionState != null) {
                coroutineScope.launch {
                    cameraPositionState.move(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.fromLatLngZoom(userLatLng, 14f)
                        )
                    )
                }
            }

            // add user marker (replace or add)
            if (markers != null) {
                // keep user marker as first entry; remove previous user marker if present (title == you_are_here)
                markers.removeAll { it.second == context.getString(R.string.you_are_here) }
                markers.add(0, userLatLng to context.getString(R.string.you_are_here))
            }

            // fetch nearby bars async
            coroutineScope.launch { fetchNearbyBars(loc.latitude, loc.longitude, context, client, markers) }
        }
    }
}

private suspend fun fetchNearbyBars(
    latitude: Double,
    longitude: Double,
    context: android.content.Context,
    client: OkHttpClient,
    markers: MutableList<Pair<LatLng, String>>? = null
) {
    withContext(Dispatchers.IO) {
        try {
            val apiKey = context.getString(R.string.google_maps_key)
            val radius = 2000
            val type = "bar"
            val url =
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$latitude,$longitude&radius=$radius&type=$type&key=$apiKey"

            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use
                val body = response.body?.string() ?: return@use
                val json = JSONObject(body)
                val results = json.optJSONArray("results") ?: return@use

                val newMarkers = mutableListOf<Pair<LatLng, String>>()
                for (i in 0 until results.length()) {
                    val item = results.getJSONObject(i)
                    val name = item.optString("name", "Bar")
                    val geometry = item.optJSONObject("geometry") ?: continue
                    val location = geometry.optJSONObject("location") ?: continue
                    val lat = location.optDouble("lat")
                    val lng = location.optDouble("lng")
                    newMarkers.add(LatLng(lat, lng) to name)
                }

                // update markers on main thread
                withContext(Dispatchers.Main) {
                    if (markers != null) {
                        // remove previous bar markers (keep user marker if present)
                        val userMarker = markers.firstOrNull { it.second == context.getString(R.string.you_are_here) }
                        markers.clear()
                        if (userMarker != null) markers.add(userMarker)
                        markers.addAll(newMarkers)
                    }
                }
            }
        } catch (_: Exception) {
            // ignore / log as needed
        }
    }
}
