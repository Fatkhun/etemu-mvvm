package com.fatkhun.core.di

import com.fatkhun.core.helper.NetworkHelper
import com.fatkhun.core.helper.SecurityHelper
import com.fatkhun.core.helper.StoreDataHelper
import com.fatkhun.core.network.ApiInterceptor
import com.fatkhun.core.network.OkHttpClients
import com.fatkhun.core.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { ApiInterceptor() }
    single { OkHttpClients(get()) }
    single { RetrofitInstance(get(), get()) }
    single { NetworkHelper(androidContext()) }
    single { SecurityHelper() }
    single { StoreDataHelper(androidContext(), get()) }
}