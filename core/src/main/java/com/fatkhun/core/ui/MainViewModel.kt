package com.fatkhun.core.ui

import androidx.lifecycle.ViewModel
import com.fatkhun.core.repository.DataStoreRepository
import com.fatkhun.core.repository.MainRepository

class MainViewModel(
    private val mainRepository: MainRepository,
    private val preference: DataStoreRepository
) : ViewModel() {
}