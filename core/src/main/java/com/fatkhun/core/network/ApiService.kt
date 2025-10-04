package com.fatkhun.core.network

import com.fatkhun.core.model.BaseResponse
import com.fatkhun.core.model.CategoriesResponse
import com.fatkhun.core.model.DetailItemResponse
import com.fatkhun.core.model.LoginForm
import com.fatkhun.core.model.LoginResponse
import com.fatkhun.core.model.LostFoundResponse
import com.fatkhun.core.model.PostingItemForm
import com.fatkhun.core.model.PostingUpdateForm
import com.fatkhun.core.model.PostingUpdateStatusForm
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
import retrofit2.http.PartMap
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
        @Part("id") categoryId: RequestBody,
        @Part("type") type: RequestBody,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("contact_type") contactType: RequestBody,
        @Part("contact_value") contactValue: RequestBody,
        @Part("user_id") ownerId: RequestBody,
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

    @Multipart
    @PATCH("api/items/{id}")
    fun updatePostingItem(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @PartMap parts: Map<String, RequestBody>,
        @Part photo: MultipartBody.Part?
    ): Call<BaseResponse>

    @PATCH("api/items/{id}")
    fun updateStatusPostingItem(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body form: PostingUpdateStatusForm
    ): Call<BaseResponse>

    @GET("api/items/history")
    suspend fun getHistoryListPaging(
        @Header("Authorization") token: String,
        @QueryMap primary_credential: MutableMap<String, String>
    ): LostFoundResponse

    @GET("api/categories")
    fun getCategoryList(): Call<CategoriesResponse>

}