package com.fatkhun.core.di

import com.fatkhun.core.ui.DataStoreViewModel
import com.fatkhun.core.ui.FirebaseRemoteConfigViewModel
import com.fatkhun.core.ui.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        FirebaseRemoteConfigViewModel(get())
    }
    viewModel {
        DataStoreViewModel(get())
    }
    viewModel {
        MainViewModel(get(), get())
    }
}