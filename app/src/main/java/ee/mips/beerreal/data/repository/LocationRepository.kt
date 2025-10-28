package ee.mips.beerreal.data.repository

import ee.mips.beerreal.data.local.LocationDao
import ee.mips.beerreal.data.model.CachedLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing location data
 * Handles caching and retrieval of user location
 */
class LocationRepository(private val locationDao: LocationDao) {

    /**
     * Cache the current user location
     * @param latitude User's latitude
     * @param longitude User's longitude
     */
    suspend fun cacheLocation(latitude: Double, longitude: Double) {
        withContext(Dispatchers.IO) {
            val cachedLocation = CachedLocation(
                latitude = latitude,
                longitude = longitude,
                timestamp = System.currentTimeMillis()
            )
            locationDao.insertLocation(cachedLocation)

            // Clean up old cache entries (older than 24 hours)
            val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
            locationDao.deleteOlderThan(oneDayAgo)
        }
    }

    /**
     * Get the last cached location if it's recent (within 1 hour)
     * @return CachedLocation if available and recent, null otherwise
     */
    suspend fun getRecentCachedLocation(): CachedLocation? {
        return withContext(Dispatchers.IO) {
            val cached = locationDao.getLastLocation()

            // Only return if cached within last hour
            if (cached != null) {
                val oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000)
                if (cached.timestamp >= oneHourAgo) {
                    cached
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    /**
     * Clear all cached locations
     */
    suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            locationDao.clearAll()
        }
    }
}

