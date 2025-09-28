package com.fatkhun.core.di

import com.fatkhun.core.repository.DataStoreRepository
import com.fatkhun.core.repository.FirebaseRemoteConfigRepository
import com.fatkhun.core.repository.MainRepository
import com.fatkhun.core.ui.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
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