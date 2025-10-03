package com.fatkhun.core.network

import androidx.datastore.preferences.core.Preferences
import com.fatkhun.core.ui.DataStoreViewModel
import com.fatkhun.core.utils.PrefKey
import com.fatkhun.core.utils.logError
import com.google.gson.GsonBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance(
    private val okHttpClients: OkHttpClients,
    private val dataStoreViewModel: DataStoreViewModel
) {

    enum class DesClient {
        ETEMU
    }

    private fun getBaseUrl(desClient: DesClient): Preferences.Key<String> {
        return when (desClient) {
            DesClient.ETEMU -> PrefKey.BASE_URL_ETEMU
        }
    }

    private fun buildRetrofit(url: String): RetrofitRoutes {
        val gson = GsonBuilder().setLenient().create()
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl("http://192.168.100.57:8080/")
            .client(okHttpClients.provideClient())
            .build()
            .create(RetrofitRoutes::class.java)
    }

    fun provideRetrofit(desClient: DesClient = DesClient.ETEMU): RetrofitRoutes {
        val urlKey = getBaseUrl(desClient)
        val url = runBlocking { fetchUrlSuspend(urlKey) }
        return buildRetrofit(url.ifBlank { "https://" })
    }

    suspend fun provideRetrofits(desClient: DesClient = DesClient.ETEMU): RetrofitRoutes {
        val urlKey = getBaseUrl(desClient)
        val url = fetchUrlSuspend(urlKey)
        logError("provideRetrofit: $url")
        return buildRetrofit(url)
    }

    private suspend fun fetchUrlSuspend(urlKey: Preferences.Key<String>): String {
        var base = ""
        dataStoreViewModel.getSecureDataValue(urlKey, "") { base = it }.toString()
        delay(100)
        logError("baseUrl: $base")
        return base
    }
}