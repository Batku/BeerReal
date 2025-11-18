package ee.mips.beerreal.data.api

import org.json.JSONObject

/**
 * Complete API response from Google Places API
 * Maps the full JSON response structure
 */
data class PlacesApiResponse(
    val htmlAttributions: List<String>,
    val nextPageToken: String?,
    val results: List<PlaceResult>,
    val status: String
) {
    companion object {
        /**
         * Parse JSON response into PlacesApiResponse
         */
        fun fromJson(jsonBody: String): PlacesApiResponse {
            val json = JSONObject(jsonBody)

            // Parse html_attributions
            val htmlAttributions = mutableListOf<String>()
            val htmlAttrsArray = json.optJSONArray("html_attributions")
            if (htmlAttrsArray != null) {
                for (i in 0 until htmlAttrsArray.length()) {
                    htmlAttributions.add(htmlAttrsArray.getString(i))
                }
            }

            // Parse results
            val results = mutableListOf<PlaceResult>()
            val resultsArray = json.optJSONArray("results")
            if (resultsArray != null) {
                for (i in 0 until resultsArray.length()) {
                    try {
                        val placeResult = PlaceResult.fromJson(resultsArray.getJSONObject(i))
                        results.add(placeResult)
                    } catch (_: Exception) {
                        // Skip malformed entries
                        continue
                    }
                }
            }

            return PlacesApiResponse(
                htmlAttributions = htmlAttributions,
                nextPageToken = json.optString("next_page_token").takeIf { it.isNotEmpty() },
                results = results,
                status = json.optString("status", "UNKNOWN")
            )
        }
    }
}

/**
 * Individual place result from Places API
 */
data class PlaceResult(
    val placeId: String,
    val name: String,
    val businessStatus: String?,
    val geometry: Geometry,
    val openingHours: OpeningHours?,
    val rating: Double?,
    val userRatingsTotal: Int?,
    val priceLevel: Int?,
    val vicinity: String?,
    val internationalPhoneNumber: String?,
    val types: List<String>,
    val icon: String?,
    val iconBackgroundColor: String?,
    val photos: List<Photo>?,
    val permanentlyClosed: Boolean?
) {
    companion object {
        fun fromJson(json: JSONObject): PlaceResult {
            // Parse geometry
            val geometryJson = json.getJSONObject("geometry")
            val geometry = Geometry.fromJson(geometryJson)

            // Parse opening_hours
            val openingHours = json.optJSONObject("opening_hours")?.let {
                OpeningHours.fromJson(it)
            }

            // Parse types
            val types = mutableListOf<String>()
            val typesArray = json.optJSONArray("types")
            if (typesArray != null) {
                for (i in 0 until typesArray.length()) {
                    types.add(typesArray.getString(i))
                }
            }

            // Parse photos
            val photos = mutableListOf<Photo>()
            val photosArray = json.optJSONArray("photos")
            if (photosArray != null) {
                for (i in 0 until photosArray.length()) {
                    try {
                        photos.add(Photo.fromJson(photosArray.getJSONObject(i)))
                    } catch (_: Exception) {
                        continue
                    }
                }
            }

            return PlaceResult(
                placeId = json.optString("place_id", ""),
                name = json.optString("name", "Unknown"),
                businessStatus = if (json.has("business_status")) json.optString("business_status") else null,
                geometry = geometry,
                openingHours = openingHours,
                rating = if (json.has("rating")) json.optDouble("rating") else null,
                userRatingsTotal = if (json.has("user_ratings_total")) json.optInt("user_ratings_total") else null,
                priceLevel = if (json.has("price_level")) json.optInt("price_level") else null,
                vicinity = if (json.has("vicinity")) json.optString("vicinity") else null,
                internationalPhoneNumber = if (json.has("international_phone_number")) json.optString("international_phone_number") else null,
                types = types,
                icon = if (json.has("icon")) json.optString("icon") else null,
                iconBackgroundColor = if (json.has("icon_background_color")) json.optString("icon_background_color") else null,
                photos = if (photos.isNotEmpty()) photos else null,
                permanentlyClosed = if (json.has("permanently_closed")) json.optBoolean("permanently_closed") else null
            )
        }
    }
}

/**
 * Geometry information (location and viewport)
 */
data class Geometry(
    val location: Location,
    val viewport: Viewport?
) {
    companion object {
        fun fromJson(json: JSONObject): Geometry {
            val locationJson = json.getJSONObject("location")
            val location = Location(
                lat = locationJson.optDouble("lat", 0.0),
                lng = locationJson.optDouble("lng", 0.0)
            )

            val viewport = json.optJSONObject("viewport")?.let {
                Viewport.fromJson(it)
            }

            return Geometry(location, viewport)
        }
    }
}

/**
 * Location coordinates
 */
data class Location(
    val lat: Double,
    val lng: Double
)

/**
 * Viewport bounds
 */
data class Viewport(
    val northeast: Location,
    val southwest: Location
) {
    companion object {
        fun fromJson(json: JSONObject): Viewport {
            val northeast = json.getJSONObject("northeast")
            val southwest = json.getJSONObject("southwest")

            return Viewport(
                northeast = Location(
                    lat = northeast.optDouble("lat", 0.0),
                    lng = northeast.optDouble("lng", 0.0)
                ),
                southwest = Location(
                    lat = southwest.optDouble("lat", 0.0),
                    lng = southwest.optDouble("lng", 0.0)
                )
            )
        }
    }
}

/**
 * Opening hours information
 */
data class OpeningHours(
    val openNow: Boolean?
) {
    companion object {
        fun fromJson(json: JSONObject): OpeningHours {
            return OpeningHours(
                openNow = if (json.has("open_now")) json.optBoolean("open_now") else null
            )
        }
    }
}

/**
 * Photo information
 */
data class Photo(
    val height: Int,
    val width: Int,
    val photoReference: String,
    val htmlAttributions: List<String>
) {
    companion object {
        fun fromJson(json: JSONObject): Photo {
            val htmlAttributions = mutableListOf<String>()
            val htmlAttrsArray = json.optJSONArray("html_attributions")
            if (htmlAttrsArray != null) {
                for (i in 0 until htmlAttrsArray.length()) {
                    htmlAttributions.add(htmlAttrsArray.getString(i))
                }
            }

            return Photo(
                height = json.optInt("height", 0),
                width = json.optInt("width", 0),
                photoReference = json.optString("photo_reference", ""),
                htmlAttributions = htmlAttributions
            )
        }
    }
}

