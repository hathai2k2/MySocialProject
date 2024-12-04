package com.example.mysocialproject.networking.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "MY_SHARE_PREF")
class AppPrefManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PrefHelper {
    private val data: Flow<Preferences> get() = context.dataStore.data
    private val userIdKey : Preferences.Key<String> = stringPreferencesKey("user_id")

    //flow
    private val userIdFlow: Flow<String?> = data.map {
        it[userIdKey] ?: ""
    }
    override suspend fun setUserId(id: String?) {
        context.dataStore.edit { settings ->
            if (id != null){
                settings[userIdKey] = id
            }else{
                settings.remove(userIdKey)
            }

        }
    }

    override suspend fun getUserId(): String? {
        return userIdFlow.firstOrNull()
    }
}