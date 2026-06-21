package com.example.emoneytransfer.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_ACCOUNT_KEY = stringPreferencesKey("user_account_number")
        private val USER_PASSWORD_KEY = stringPreferencesKey("user_password")
        private val PIN_KEY = stringPreferencesKey("user_pin")
        private val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")
    }

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val userName: Flow<String?> = context.dataStore.data.map { it[USER_NAME_KEY] }
    val userEmail: Flow<String?> = context.dataStore.data.map { it[USER_EMAIL_KEY] }
    val accountNumber: Flow<String?> = context.dataStore.data.map { it[USER_ACCOUNT_KEY] }
    val pin: Flow<String?> = context.dataStore.data.map { it[PIN_KEY] }
    val userPassword: Flow<String?> = context.dataStore.data.map { it[USER_PASSWORD_KEY] }
    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { it[BIOMETRIC_ENABLED_KEY] ?: false }

    suspend fun saveLoginData(token: String, name: String, email: String, accountNumber: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_NAME_KEY] = name
            prefs[USER_EMAIL_KEY] = email
            prefs[USER_ACCOUNT_KEY] = accountNumber
        }
    }

    suspend fun saveCredentials(email: String, password: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_EMAIL_KEY] = email
            prefs[USER_PASSWORD_KEY] = password
            prefs[BIOMETRIC_ENABLED_KEY] = true
        }
    }

    suspend fun savePin(pin: String) {
        context.dataStore.edit { prefs ->
            prefs[PIN_KEY] = pin
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
