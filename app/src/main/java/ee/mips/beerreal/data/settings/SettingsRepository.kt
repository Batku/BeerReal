package ee.mips.beerreal.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

object SettingsRepository {
    private val DARK_KEY = booleanPreferencesKey("dark_mode")

    fun darkModeFlow(context: Context): Flow<Boolean> =
        context.settingsDataStore.data.map { prefs -> prefs[DARK_KEY] ?: false }

    suspend fun setDarkMode(context: Context, value: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[DARK_KEY] = value
        }
    }
}
