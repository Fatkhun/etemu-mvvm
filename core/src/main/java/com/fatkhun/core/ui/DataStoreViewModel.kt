package com.fatkhun.core.ui

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fatkhun.core.repository.DataStoreRepository
import kotlinx.coroutines.launch

class DataStoreViewModel(
    val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    inline fun <reified T> setDataValue(key: Preferences.Key<T>, value: T) {
        viewModelScope.launch {
            dataStoreRepository.setValueData(key, value)
        }
    }

    inline fun <reified T> setSecureDataValue(key: Preferences.Key<T>, value: T) {
        viewModelScope.launch {
            dataStoreRepository.setSecureValueData(key, value)
        }
    }

    fun <T> getDataValue(key: Preferences.Key<T>, defValue: T, callbacks: (t: T) -> Unit) {
        viewModelScope.launch {
            dataStoreRepository.getValueData(key, defValue, callbacks)
        }
    }

    inline fun <reified T> getSecureDataValue(
        key: Preferences.Key<T>,
        defValue: T,
        crossinline callbacks: (T) -> Unit
    ) {
        viewModelScope.launch {
            dataStoreRepository.getSecureValueData(key, defValue, callbacks)
        }
    }

    fun <T> clearDataValue(key: Preferences.Key<T>) {
        viewModelScope.launch {
            dataStoreRepository.clearValueData(key)
        }
    }

}