package com.fatkhun.core.di

import com.fatkhun.core.repository.DataStoreRepository
import com.fatkhun.core.repository.FirebaseRemoteConfigRepository
import com.fatkhun.core.repository.MainRepository
import org.koin.dsl.module

val repoModule = module {
    single {
        FirebaseRemoteConfigRepository(get())
    }
    single {
        DataStoreRepository(get(), get())
    }
    single {
        MainRepository(get(), get(), get())
    }
}