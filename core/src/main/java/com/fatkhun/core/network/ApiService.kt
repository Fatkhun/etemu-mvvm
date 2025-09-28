package com.fatkhun.core.network

import com.fatkhun.core.model.LoginForm
import com.fatkhun.core.model.LoginResponse
import com.fatkhun.core.model.RegisterForm
import com.fatkhun.core.model.RegisterResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/register")
    fun registerUser(
        @Body form: RegisterForm
    ): Call<RegisterResponse>

    @POST("api/auth/login")
    fun loginUser(
        @Body form: LoginForm
    ): Call<LoginResponse>
}