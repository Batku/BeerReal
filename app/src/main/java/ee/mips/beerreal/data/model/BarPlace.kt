package ee.mips.beerreal.data.model

/**
 * Data class representing a bar/place from Google Places API
 * Models the JSON response from the Places API
 *
 * @property placeId Unique identifier for the place
 * @property name Name of the bar
 * @property latitude Latitude coordinate
 * @property longitude Longitude coordinate
 * @property isOpen Whether the bar is currently open (null if unknown)
 * @property rating Rating of the bar (0.0-5.0)
 * @property vicinity Address/vicinity information
 */
data class BarPlace(
    val placeId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val isOpen: Boolean? = null,
    val rating: Double? = null,
    val vicinity: String? = null
)

