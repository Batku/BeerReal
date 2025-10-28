package ee.mips.beerreal.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching user location data
 * Stores the last known user location with timestamp
 */
@Entity(tableName = "cached_locations")
data class CachedLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)

