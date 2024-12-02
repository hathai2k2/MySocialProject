package com.example.mysocialproject.networking.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "MY_SHARE_PREF")
class AppPrefManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PrefHelper {
    private val data: Flow<Preferences> get() = context.dataStore.data
}