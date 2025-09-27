package com.fatkhun.core.repository

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.fatkhun.core.utils.logError
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigClientException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigFetchThrottledException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlin.math.pow

class FirebaseRemoteConfigRepository(val context: Context) {

    companion object {
        const val URL_ETEMU_CORE = 0
    }

    private var remoteConfig = FirebaseRemoteConfig.getInstance()
    private var retryCount: Int = 0
    private val MAX_RETRY_ATTEMPTS = 3

    fun initRemoteConfig() {
        // Set the default values locally
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(getCacheEx())
            .setFetchTimeoutInSeconds(60).build()
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    fun getCacheEx(): Long {
        var cacheExpiration: Long = 3600
        cacheExpiration = 0
        return cacheExpiration
    }

    fun setXMLAndListener(
        xml: Int,
        callback: (success: Boolean) -> Unit
    ) { // true sukses, false gagal
        initRemoteConfig()
        remoteConfig.setDefaultsAsync(xml)
        fetchAndActivateRemoteConfig {
            callback.invoke(it)
        }
    }

    private fun fetchAndActivateRemoteConfig(callback: (success: Boolean) -> Unit) {
        try {
            if (retryCount >= MAX_RETRY_ATTEMPTS) {
                logError("RemoteConfig Max retry attempts reached")
                retryCount = 0
                callback.invoke(true)
                return
            }

            remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    retryCount = 0 // Reset retry count on success
                    callback.invoke(true)
                } else {
                    val exception = task.exception
                    if (exception is FirebaseRemoteConfigFetchThrottledException && retryCount < MAX_RETRY_ATTEMPTS) {
                        val retryAfter = exception.throttleEndTimeMillis
                        val backoffTime = 2.0.pow(retryCount.toDouble()).toLong() * 1000 // Exponential backoff
                        val delay = retryAfter.coerceAtLeast(backoffTime)
                        logError("RemoteConfig Fetch throttled. Retrying after: $delay milliseconds")
                        retryCount++
                        scheduleRetry(delay) {
                            callback.invoke(it)
                        }
                    } else if (exception is FirebaseRemoteConfigClientException && retryCount < MAX_RETRY_ATTEMPTS) {
                        logError("RemoteConfig Firebase Installation auth token fetch failed: ${exception.message}")
                        retryCount++
                        // Retry logic or notify user
                        scheduleRetry(3000) {
                            callback.invoke(it)
                        }
                    } else {
                        callback.invoke(false)
                    }
                }
            }
        } catch (e: Exception){
            callback.invoke(false)
        }
    }

    private fun scheduleRetry(retryAfterMillis: Long, callback: (success: Boolean) -> Unit)  {
        Handler(Looper.getMainLooper()).postDelayed({
            fetchAndActivateRemoteConfig{
                callback.invoke(it)
            } // Retry fetching remote config
        }, retryAfterMillis)
    }

    fun getBaseUrl(): Any {
        return remoteConfig.getString("base_url")
    }
}