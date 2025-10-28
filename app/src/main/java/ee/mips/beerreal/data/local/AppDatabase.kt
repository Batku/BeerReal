package ee.mips.beerreal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ee.mips.beerreal.data.model.CachedLocation
import ee.mips.beerreal.data.model.CachedBar

/**
 * Room Database for BeerReal app
 * Manages cached location and bar data for offline support
 */
@Database(
    entities = [CachedLocation::class, CachedBar::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun locationDao(): LocationDao
    abstract fun barDao(): BarDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Get singleton instance of the database
         * Thread-safe implementation using double-checked locking
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "beerreal_database"
                )
                    .fallbackToDestructiveMigration() // Simple migration strategy for development
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

