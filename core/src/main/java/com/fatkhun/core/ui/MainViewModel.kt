package com.fatkhun.core.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fatkhun.core.model.LoginForm
import com.fatkhun.core.model.LoginResponse
import com.fatkhun.core.model.RegisterForm
import com.fatkhun.core.model.RegisterResponse
import com.fatkhun.core.repository.DataStoreRepository
import com.fatkhun.core.repository.MainRepository
import com.fatkhun.core.utils.Resource

class MainViewModel(
    private val mainRepository: MainRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    fun registerUser(
        form: RegisterForm
    ): MutableLiveData<Resource<RegisterResponse>> {
        return mainRepository.registerUser(form)
    }

    fun loginUser(
        form: LoginForm
    ): MutableLiveData<Resource<LoginResponse>> {
        return mainRepository.loginUser(form)
    }

}