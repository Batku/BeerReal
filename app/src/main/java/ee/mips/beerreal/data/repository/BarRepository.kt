package ee.mips.beerreal.data.repository

import ee.mips.beerreal.data.local.BarDao
import ee.mips.beerreal.data.model.BarPlace
import ee.mips.beerreal.data.model.CachedBar
import ee.mips.beerreal.data.model.toCachedBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing bar data caching
 * Handles storing and retrieving bars with full details for offline support
 */
class BarRepository(private val barDao: BarDao) {

    /**
     * Cache bars with user location context
     * @param bars List of bars to cache
     * @param userLat User's latitude when bars were fetched
     * @param userLng User's longitude when bars were fetched
     */
    suspend fun cacheBars(bars: List<BarPlace>, userLat: Double, userLng: Double) {
        withContext(Dispatchers.IO) {
            val cachedBars = bars.map { it.toCachedBar(userLat, userLng) }
            barDao.insertBars(cachedBars)

            // Clean up old cache entries (older than 7 days)
            val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            barDao.deleteOlderThan(sevenDaysAgo)
        }
    }

    /**
     * Get cached bars near a specific location
     * Returns bars cached when user was near the specified location
     * @param userLat User's current latitude
     * @param userLng User's current longitude
     * @return List of BarPlace objects from cache, or empty list if no cached data
     */
    suspend fun getCachedBarsNearLocation(userLat: Double, userLng: Double): List<BarPlace> {
        return withContext(Dispatchers.IO) {
            val cachedBars = barDao.getBarsNearLocation(userLat, userLng)

            // If we have recent cached bars near this location, return them
            if (cachedBars.isNotEmpty()) {
                val oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000)
                val recentBars = cachedBars.filter { it.timestamp >= oneHourAgo }

                if (recentBars.isNotEmpty()) {
                    return@withContext recentBars.map { it.toBarPlace() }
                }
            }

            // Fallback to any cached bars if nothing nearby
            val allCached = barDao.getAllCachedBars()
            if (allCached.isNotEmpty()) {
                val sixHoursAgo = System.currentTimeMillis() - (6 * 60 * 60 * 1000)
                return@withContext allCached
                    .filter { it.timestamp >= sixHoursAgo }
                    .take(20)
                    .map { it.toBarPlace() }
            }

            emptyList()
        }
    }

    /**
     * Check if we have any cached bar data
     * @return true if cache has data, false otherwise
     */
    suspend fun hasCachedData(): Boolean {
        return withContext(Dispatchers.IO) {
            barDao.getCachedBarCount() > 0
        }
    }

    /**
     * Clear all cached bars
     */
    suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            barDao.clearAll()
        }
    }
}

