package ee.mips.beerreal.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ee.mips.beerreal.data.model.CachedBar

/**
 * Data Access Object for CachedBar
 * Provides methods to cache and retrieve bar data
 */
@Dao
interface BarDao {

    /**
     * Insert or update bars in cache
     * Replaces existing entries on conflict
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBars(bars: List<CachedBar>)

    /**
     * Get cached bars near a specific location
     * Returns bars within approximately 3km radius (0.03 degrees)
     * @param userLat User's latitude
     * @param userLng User's longitude
     * @param latRange Latitude search range (default 0.03 ~ 3km)
     * @param lngRange Longitude search range (default 0.03 ~ 3km)
     * @return List of cached bars near the user
     */
    @Query("""
        SELECT * FROM cached_bars 
        WHERE userLatitude BETWEEN :userLat - :latRange AND :userLat + :latRange
        AND userLongitude BETWEEN :userLng - :lngRange AND :userLng + :lngRange
        ORDER BY timestamp DESC
    """)
    suspend fun getBarsNearLocation(
        userLat: Double,
        userLng: Double,
        latRange: Double = 0.03,
        lngRange: Double = 0.03
    ): List<CachedBar>

    /**
     * Get all cached bars regardless of location
     * Useful as fallback when no nearby cached data exists
     */
    @Query("SELECT * FROM cached_bars ORDER BY timestamp DESC LIMIT 50")
    suspend fun getAllCachedBars(): List<CachedBar>

    /**
     * Delete bars older than the specified timestamp
     * @param timestamp Cutoff time in milliseconds
     */
    @Query("DELETE FROM cached_bars WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    /**
     * Clear all cached bars
     */
    @Query("DELETE FROM cached_bars")
    suspend fun clearAll()

    /**
     * Get count of cached bars
     */
    @Query("SELECT COUNT(*) FROM cached_bars")
    suspend fun getCachedBarCount(): Int
}

