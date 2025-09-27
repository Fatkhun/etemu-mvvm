package com.fatkhun.core.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class OkHttpClients(private val interceptor: ApiInterceptor) {

    fun provideClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(interceptor.provideInterceptor())
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()
    }
}