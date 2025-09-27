package com.fatkhun.core.network

import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor

class ApiInterceptor {

    fun provideInterceptor(): Interceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }
}