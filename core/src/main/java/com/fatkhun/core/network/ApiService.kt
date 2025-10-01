package com.fatkhun.core.network

import com.fatkhun.core.model.BaseResponse
import com.fatkhun.core.model.DetailItemResponse
import com.fatkhun.core.model.LoginForm
import com.fatkhun.core.model.LoginResponse
import com.fatkhun.core.model.LostFoundResponse
import com.fatkhun.core.model.RegisterForm
import com.fatkhun.core.model.RegisterResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface ApiService {

    @POST("api/auth/register")
    fun registerUser(
        @Body form: RegisterForm
    ): Call<RegisterResponse>

    @POST("api/auth/login")
    fun loginUser(
        @Body form: LoginForm
    ): Call<LoginResponse>

    @Multipart
    @POST("api/items")
    fun postingItem(
        @Header("Authorization") token: String,
        @Part("categoryId") categoryId: RequestBody,
        @Part("type") type: RequestBody,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("contactType") contactType: RequestBody,
        @Part("contactValue") contactValue: RequestBody,
        @Part photo: MultipartBody.Part? // File (optional)
    ): Call<BaseResponse>

    @GET("api/items")
    fun getLostFoundList(
        @QueryMap primary_credential: MutableMap<String, String>
    ): Call<LostFoundResponse>

    @GET("api/items")
    suspend fun getLostFoundPaging(
        @QueryMap primary_credential: MutableMap<String, String>
    ): LostFoundResponse

    @GET("api/items/{id}")
    fun getDetailItem(
        @Path("id") id: String
    ): Call<DetailItemResponse>

    @PATCH("api/items/{id}")
    fun updatePostingItem(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Call<BaseResponse>

}