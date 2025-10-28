package ee.mips.beerreal.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ee.mips.beerreal.data.model.CachedLocation

/**
 * Data Access Object for CachedLocation
 * Provides methods to interact with the cached_locations table
 */
@Dao
interface LocationDao {

    /**
     * Insert a new location into the cache
     * Replaces existing entry on conflict
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: CachedLocation)

    /**
     * Get the most recent cached location
     * @return The latest CachedLocation or null if cache is empty
     */
    @Query("SELECT * FROM cached_locations ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastLocation(): CachedLocation?

    /**
     * Delete all cached locations
     */
    @Query("DELETE FROM cached_locations")
    suspend fun clearAll()

    /**
     * Delete locations older than the specified timestamp
     * @param timestamp Cutoff time in milliseconds
     */
    @Query("DELETE FROM cached_locations WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}

