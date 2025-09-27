package com.fatkhun.core.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.fatkhun.core.helper.SecurityHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import java.io.IOException
import javax.crypto.spec.SecretKeySpec

class DataStoreRepository(context: Context, val security: SecurityHelper) {
    private val NAME_PREF = "ANDROID ETEMU-ETEMU-data-pref"
    val securityKeyAlias = "ANDROID ETEMU-ETEMU-data-store"
    var secretKeySpec: SecretKeySpec? = null

    private val Context.dataStore by preferencesDataStore(name = NAME_PREF)

    val dataStore = context.dataStore

    init {
        if (secretKeySpec == null) {
            secretKeySpec = security.generateAESKey(securityKeyAlias)
        }
    }

    suspend fun <T> DataStore<Preferences>.getValueFlow(
        key: Preferences.Key<T>,
        defaultValue: T
    ): Flow<T> {
        return this.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }

        }.map { Preferences ->
            Preferences[key] ?: defaultValue
        }.take(1)
    }

    suspend fun <T> DataStore<Preferences>.setValue(key: Preferences.Key<T>, value: T) {
        this.edit { preferences ->
            preferences[key] = value
        }
    }

    suspend fun <T> DataStore<Preferences>.setSecureValue(key: Preferences.Key<T>, value: T) {
        this.edit { preferences ->
            preferences[key] = value
        }
    }

    suspend fun <T> DataStore<Preferences>.clearValue(key: Preferences.Key<T>) {
        this.edit {
            it.remove(key)
        }
    }

    suspend inline fun <reified T> setValueData(key: Preferences.Key<T>, value: T) {
        dataStore.setValue(key, value)
    }

    suspend inline fun <reified T> setSecureValueData(key: Preferences.Key<T>, value: T) {
        secretKeySpec?.let { secret ->
            security.encryptAES(
                value.toString(),
                secret
            )?.let { value ->
                dataStore.setSecureValue(key, value as T)
            }
        }
    }

    suspend fun <T> clearValueData(key: Preferences.Key<T>) {
        dataStore.clearValue(key)
    }

    suspend fun <T> getValueData(key: Preferences.Key<T>, defValue: T, callback: (t: T) -> Unit) {
        dataStore.getValueFlow(key, defValue).collect {
            callback.invoke(it)
        }
    }

    suspend inline fun <reified T> getSecureValueData(
        key: Preferences.Key<T>,
        defValue: T,
        crossinline callback: (T) -> Unit
    ) {
        dataStore.getValueFlow(key, defValue).collect {
            secretKeySpec?.let { secretKey ->
                security.decryptAES(
                    it.toString(),
                    secretKey
                )?.let { value ->
                    callback.invoke(value as T)
                }
            }
        }
    }
}