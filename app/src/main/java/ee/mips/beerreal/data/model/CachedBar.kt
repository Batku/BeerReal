package ee.mips.beerreal.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching bar data
 * Stores bar information including location, name, rating, and opening status
 */
@Entity(tableName = "cached_bars")
data class CachedBar(
    @PrimaryKey
    val placeId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val isOpen: Boolean?,
    val rating: Double?,
    val vicinity: String?,
    val userLatitude: Double,  // User location when this was cached
    val userLongitude: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Convert CachedBar to BarPlace for display
     */
    fun toBarPlace(): BarPlace {
        return BarPlace(
            placeId = placeId,
            name = name,
            latitude = latitude,
            longitude = longitude,
            isOpen = isOpen,
            rating = rating,
            vicinity = vicinity
        )
    }
}

/**
 * Extension function to convert BarPlace to CachedBar
 */
fun BarPlace.toCachedBar(userLat: Double, userLng: Double): CachedBar {
    return CachedBar(
        placeId = placeId,
        name = name,
        latitude = latitude,
        longitude = longitude,
        isOpen = isOpen,
        rating = rating,
        vicinity = vicinity,
        userLatitude = userLat,
        userLongitude = userLng,
        timestamp = System.currentTimeMillis()
    )
}

