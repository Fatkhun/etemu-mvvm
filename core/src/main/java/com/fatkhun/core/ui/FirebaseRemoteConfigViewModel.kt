package com.fatkhun.core.ui

import com.fatkhun.core.repository.FirebaseRemoteConfigRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fatkhun.core.R
import com.fatkhun.core.utils.jsonToMap
import com.fatkhun.core.utils.toJsonObject
import kotlin.toString

class FirebaseRemoteConfigViewModel(
    private val firebaseRemoteConfigRepository: FirebaseRemoteConfigRepository
): ViewModel() {
    init {
        fetchInit()
    }

    private fun fetchInit() {
        viewModelScope.launch {
            try {
                firebaseRemoteConfigRepository.setXMLAndListener(R.xml.remote_config_firebase) {}
            }catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }

    fun fetchBaseAll(failed: () -> Unit): MutableLiveData<MutableList<String>> {
        val liveDataBaseUrl: MutableLiveData<MutableList<String>> = MutableLiveData()
        viewModelScope.launch {
            firebaseRemoteConfigRepository.setXMLAndListener(R.xml.remote_config_firebase) {
                if (it) {
                    val y: ArrayList<String> = arrayListOf()
                    val baseUrl = firebaseRemoteConfigRepository.getBaseUrl().toString()
                    y.add(
                        FirebaseRemoteConfigRepository.URL_ETEMU_CORE,
                        jsonToMap(
                            baseUrl.toJsonObject()
                        )["base_url_1"].toString()
                    )
                    liveDataBaseUrl.postValue(y)
                } else {
                    failed.invoke()
                }
            }
        }
        return liveDataBaseUrl
    }
}