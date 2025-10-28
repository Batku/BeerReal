package ee.mips.beerreal.data.api

import ee.mips.beerreal.data.model.BarPlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * API Service interface for Google Places API
 * Handles fetching nearby bars with opening hours information
 */
interface PlacesApiService {

    /**
     * Fetch nearby bars from Google Places API
     * @param latitude User's latitude
     * @param longitude User's longitude
     * @param apiKey Google Maps API key
     * @param radius Search radius in meters (default: 2000m)
     * @return List of BarPlace objects with opening status
     * @throws Exception if network request fails
     */
    suspend fun getNearbyBars(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        radius: Int = 2000
    ): List<BarPlace>
}

/**
 * Implementation of PlacesApiService using OkHttp
 * Makes GET requests to Google Places API and parses JSON responses
 */
class PlacesApiServiceImpl(
    private val client: OkHttpClient = OkHttpClient()
) : PlacesApiService {

    override suspend fun getNearbyBars(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        radius: Int
    ): List<BarPlace> = withContext(Dispatchers.IO) {
        try {
            // Build API URL with parameters
            val url = buildString {
                append("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
                append("?location=$latitude,$longitude")
                append("&radius=$radius")
                append("&type=bar")
                append("&key=$apiKey")
            }

            // Execute GET request
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                throw Exception("API request failed with code: ${response.code}")
            }

            val body = response.body?.string() ?: throw Exception("Empty response body")

            // Parse JSON response
            parseBarPlaces(body)
        } catch (e: Exception) {
            // Log and re-throw for error handling upstream
            throw Exception("Failed to fetch nearby bars: ${e.message}", e)
        }
    }

    /**
     * Parse JSON response from Places API into BarPlace objects
     * Extracts place_id, name, geometry, opening_hours, rating, and vicinity
     */
    private fun parseBarPlaces(jsonBody: String): List<BarPlace> {
        val json = JSONObject(jsonBody)
        val results = json.optJSONArray("results") ?: return emptyList()

        val bars = mutableListOf<BarPlace>()

        for (i in 0 until results.length()) {
            try {
                val item = results.getJSONObject(i)

                // Extract required fields
                val placeId = item.optString("place_id", "")
                val name = item.optString("name", "Unknown Bar")

                // Extract geometry/location
                val geometry = item.optJSONObject("geometry") ?: continue
                val location = geometry.optJSONObject("location") ?: continue
                val lat = location.optDouble("lat", 0.0)
                val lng = location.optDouble("lng", 0.0)

                // Extract opening hours (if available)
                val openingHours = item.optJSONObject("opening_hours")
                val isOpen = openingHours?.optBoolean("open_now")

                // Extract optional fields
                val rating = if (item.has("rating")) item.optDouble("rating") else null
                val vicinity = if (item.has("vicinity")) item.optString("vicinity") else null

                bars.add(
                    BarPlace(
                        placeId = placeId,
                        name = name,
                        latitude = lat,
                        longitude = lng,
                        isOpen = isOpen,
                        rating = rating,
                        vicinity = vicinity
                    )
                )
            } catch (_: Exception) {
                // Skip malformed entries
                continue
            }
        }

        return bars
    }
}

