package com.dmirzayants.runlab.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dmirzayants.runlab.constants.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

data class UserProfile(
    val name: String = "",
    val weightKg: Float = Constants.DEFAULT_WEIGHT,
    val isOnboarded: Boolean = false
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val NAME = stringPreferencesKey("KEY_NAME")
        val WEIGHT_KG = floatPreferencesKey("KEY_WEIGHT_KG")
        val LEGACY_WEIGHT = stringPreferencesKey("KEY_WEIGHT") // the first version stored weight as text
        val FIRST_TIME = booleanPreferencesKey("KEY_FIRST_TIME_TOGGLE")
    }

    val userProfile: Flow<UserProfile> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            UserProfile(
                name = prefs[Keys.NAME].orEmpty(),
                weightKg = prefs[Keys.WEIGHT_KG]
                    ?: prefs[Keys.LEGACY_WEIGHT]?.toFloatOrNull()
                    ?: Constants.DEFAULT_WEIGHT,
                isOnboarded = prefs[Keys.FIRST_TIME] == false
            )
        }

    suspend fun saveProfile(name: String, weightKg: Float) {
        dataStore.edit { prefs ->
            prefs[Keys.NAME] = name
            prefs[Keys.WEIGHT_KG] = weightKg
            prefs.remove(Keys.LEGACY_WEIGHT)
            prefs[Keys.FIRST_TIME] = false
        }
    }
}
