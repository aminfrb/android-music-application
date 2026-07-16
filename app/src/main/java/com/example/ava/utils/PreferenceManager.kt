package com.example.ava.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class PreferenceManager(
    private val dataStore: DataStore<Preferences>,
    private val context: Context
) {
    private val darkModeKey = booleanPreferencesKey("dark_mode")
    private val languageKey = stringPreferencesKey("language")
    private val premiumKey = booleanPreferencesKey("is_premium")

    val darkModeFlow: Flow<Boolean> = dataStore.data.map { it[darkModeKey] ?: false }
    val languageFlow: Flow<String> = dataStore.data.map { it[languageKey] ?: "en" }
    val premiumFlow: Flow<Boolean> = dataStore.data.map { it[premiumKey] ?: false }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[darkModeKey] = enabled }
    }

    suspend fun setLanguage(lang: String) {
        dataStore.edit { it[languageKey] = lang }
    }

    suspend fun setPremium(value: Boolean) {
        dataStore.edit { it[premiumKey] = value }
    }

    fun isPremium(): Boolean {
        // synchronous read (blocking) - not recommended; use flow instead
        return runBlocking { dataStore.data.map { it[premiumKey] ?: false }.first() }
    }
}
