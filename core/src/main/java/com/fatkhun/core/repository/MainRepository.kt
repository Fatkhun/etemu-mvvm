package com.fatkhun.core.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.fatkhun.core.helper.NetworkHelper
import com.fatkhun.core.helper.StoreDataHelper
import com.fatkhun.core.model.BaseResponse
import com.fatkhun.core.model.LoginForm
import com.fatkhun.core.model.LoginResponse
import com.fatkhun.core.model.LostFoundForm
import com.fatkhun.core.model.LostFoundItemList
import com.fatkhun.core.model.LostFoundResponse
import com.fatkhun.core.model.PostingItemForm
import com.fatkhun.core.model.RegisterForm
import com.fatkhun.core.model.RegisterResponse
import com.fatkhun.core.network.RetrofitInstance
import com.fatkhun.core.network.RetrofitRoutes
import com.fatkhun.core.ui.LostFoundPagingSource
import com.fatkhun.core.utils.Constant
import com.fatkhun.core.utils.Resource
import com.fatkhun.core.utils.isNotNull
import com.fatkhun.core.utils.isSuccessAndNotNull
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class MainRepository(
    private val retrofitInstance: RetrofitInstance,
    private val networkHelper: NetworkHelper,
    private val storeDataHelper: StoreDataHelper
) {
    fun registerUser(
        form: RegisterForm
    ): MutableLiveData<Resource<RegisterResponse>> {
        val dataResponse = MutableLiveData<Resource<RegisterResponse>>()

        dataResponse.postValue(Resource.loading(null))
        if (networkHelper.isNetworkConnected()) {
            retrofitInstance.provideRetrofit(RetrofitInstance.DesClient.ETEMU)
                .registerUser(form).enqueue(object : Callback<RegisterResponse> {
                    override fun onResponse(
                        call: Call<RegisterResponse>,
                        response: Response<RegisterResponse>
                    ) {
                        if (response.isSuccessAndNotNull()) {
                            response.body()?.let {
                                dataResponse.postValue(
                                    Resource.success(
                                        it,
                                        response.code(),
                                        (call.request() as Request)
                                    )
                                )
                            }
                        } else {
                            response.errorBody()?.let {
                                val raw = it.string()
                                if (raw.isNotNull()) {
                                    try {
                                        val gson = Gson().fromJson(raw, RegisterResponse::class.java)
                                        dataResponse.postValue(
                                            Resource.error(
                                                response.message(),
                                                response.code(),
                                                gson,
                                                (call.request() as Request)
                                            )
                                        )
                                    }catch (e: Exception) {
                                        dataResponse.postValue(
                                            Resource.error(
                                                response.message(),
                                                response.code(),
                                                null,
                                                (call.request() as Request)
                                            )
                                        )
                                    }
                                } else {
                                    dataResponse.postValue(
                                        Resource.error(
                                            response.message(),
                                            response.code(),
                                            null,
                                            (call.request() as Request)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                        dataResponse.postValue(
                            Resource.error(
                                t.message.toString(),
                                0,
                                null,
                                (call.request() as Request)
                            )
                        )
                    }
                })
        } else {
            dataResponse.postValue(Resource.error("No internet connection", 0, null, null))
        }
        return dataResponse
    }

    fun loginUser(
        form: LoginForm
    ): MutableLiveData<Resource<LoginResponse>> {
        val dataResponse = MutableLiveData<Resource<LoginResponse>>()

        dataResponse.postValue(Resource.loading(null))
        if (networkHelper.isNetworkConnected()) {
            retrofitInstance.provideRetrofit(RetrofitInstance.DesClient.ETEMU)
                .loginUser(form).enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        if (response.isSuccessAndNotNull()) {
                            response.body()?.let {
                                dataResponse.postValue(
                                    Resource.success(
                                        it,
                                        response.code(),
                                        (call.request() as Request)
                                    )
                                )
                            }
                        } else {
                            response.errorBody()?.let {
                                val raw = it.string()
                                if (raw.isNotNull()) {
                                    try {
                                        val gson = Gson().fromJson(raw, LoginResponse::class.java)
                                        dataResponse.postValue(
                                            Resource.error(
                                                response.message(),
                                                response.code(),
                                                gson,
                                                (call.request() as Request)
                                            )
                                        )
                                    }catch (e: Exception) {
                                        dataResponse.postValue(
                                            Resource.error(
                                                response.message(),
                                                response.code(),
                                                null,
                                                (call.request() as Request)
                                            )
                                        )
                                    }
                                } else {
                                    dataResponse.postValue(
                                        Resource.error(
                                            response.message(),
                                            response.code(),
                                            null,
                                            (call.request() as Request)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        dataResponse.postValue(
                            Resource.error(
                                t.message.toString(),
                                0,
                                null,
                                (call.request() as Request)
                            )
                        )
                    }
                })
        } else {
            dataResponse.postValue(Resource.error("No internet connection", 0, null, null))
        }
        return dataResponse
    }

    fun getLostFoundList(
        form: LostFoundForm
    ): MutableLiveData<Resource<LostFoundResponse>> {
        val dataResponse = MutableLiveData<Resource<LostFoundResponse>>()

        val data: HashMap<String, String> = hashMapOf()
        data["q"] = form.keyword
        data["categoryId"] = form.category_id
        data["status"] = form.status
        data["type"] = form.type
        data["limit"] = form.limit.toString()
        data["offset"] = form.offset.toString()

        dataResponse.postValue(Resource.loading(null))
        if (networkHelper.isNetworkConnected()) {
            retrofitInstance.provideRetrofit(RetrofitInstance.DesClient.ETEMU)
                .getLostFoundList(data).enqueue(object : Callback<LostFoundResponse> {
                    override fun onResponse(
                        call: Call<LostFoundResponse>,
                        response: Response<LostFoundResponse>
                    ) {
                        if (response.isSuccessAndNotNull()) {
                            response.body()?.let {
                                dataResponse.postValue(
                                    Resource.success(
                                        it,
                                        response.code(),
                                        (call.request() as Request)
                                    )
                                )
                            }
                        } else {
                            response.errorBody()?.let {
                                val raw = it.string()
                                if (raw.isNotNull()) {
                                    try {
                                        val gson = Gson().fromJson(raw, LostFoundResponse::class.java)
                                        dataResponse.postValue(
                                            Resource.error(
                                                response.message(),
                                                response.code(),
                                                gson,
                                                (call.request() as Request)
                                            )
                                        )
                                    }catch (e: Exception) {
                                        dataResponse.postValue(
                                            Resource.error(
                                                response.message(),
                                                response.code(),
                                                null,
                                                (call.request() as Request)
                                            )
                                        )
                                    }
                                } else {
                                    dataResponse.postValue(
                                        Resource.error(
                                            response.message(),
                                            response.code(),
                                            null,
                                            (call.request() as Request)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<LostFoundResponse>, t: Throwable) {
                        dataResponse.postValue(
                            Resource.error(
                                t.message.toString(),
                                0,
                                null,
                                (call.request() as Request)
                            )
                        )
                    }
                })
        } else {
            dataResponse.postValue(Resource.error("No internet connection", 0, null, null))
        }
        return dataResponse
    }

    fun getLostFoundPaging(
        form: LostFoundForm
    ): LiveData<PagingData<LostFoundItemList>> = liveData(Dispatchers.IO){
        val api: RetrofitRoutes =
            retrofitInstance.provideRetrofits(RetrofitInstance.DesClient.ETEMU)

        val pagingLive = Pager(
            config = PagingConfig(
                pageSize = Constant.PAGE_HALF_SIZE,
                initialLoadSize = Constant.PAGE_SIZE,
                prefetchDistance = Constant.PAGE_HALF_SIZE * 3,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                LostFoundPagingSource(
                    service = { api },
                    form
                )
            }
        ).liveData
        emitSource(pagingLive)
    }

    fun postingItem(
        token: String,
        form: PostingItemForm
    ): MutableLiveData<Resource<BaseResponse>> {
        val dataResponse = MutableLiveData<Resource<BaseResponse>>()
        try {
            // Prepare the file part
            val path = form.file_evidence_path
            val file = File(path) // Replace with your file path
            val requestFile = file.asRequestBody("image/jpg".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file_evidence", file.name, requestFile)

            // Prepare other parts
            val categoryId = form.categoryId.toRequestBody("text/plain".toMediaTypeOrNull())
            val type = form.type.toRequestBody("text/plain".toMediaTypeOrNull())
            val name = form.name.toRequestBody("text/plain".toMediaTypeOrNull())
            val description = form.description.toRequestBody("text/plain".toMediaTypeOrNull())
            val contactType = form.contactType.toRequestBody("text/plain".toMediaTypeOrNull())
            val contactValue = form.contactValue.toRequestBody("text/plain".toMediaTypeOrNull())

            dataResponse.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                retrofitInstance.provideRetrofit(RetrofitInstance.DesClient.ETEMU)
                    .postingItem("Bearer $token", categoryId, type, name, description, contactType, contactValue, filePart).enqueue(object : Callback<BaseResponse> {
                        override fun onResponse(
                            call: Call<BaseResponse>,
                            response: Response<BaseResponse>
                        ) {
                            if (response.isSuccessAndNotNull()) {
                                response.body()?.let {
                                    dataResponse.postValue(
                                        Resource.success(
                                            it,
                                            response.code(),
                                            (call.request() as Request)
                                        )
                                    )
                                }
                            } else {
                                response.errorBody()?.let {
                                    val raw = it.string()
                                    if (raw.isNotNull()) {
                                        try {
                                            val gson = Gson().fromJson(raw, BaseResponse::class.java)
                                            dataResponse.postValue(
                                                Resource.error(
                                                    response.message(),
                                                    response.code(),
                                                    gson,
                                                    (call.request() as Request)
                                                )
                                            )
                                        }catch (e: Exception) {
                                            dataResponse.postValue(
                                                Resource.error(
                                                    response.message(),
                                                    response.code(),
                                                    null,
                                                    (call.request() as Request)
                                                )
                                            )
                                        }
                                    } else {
                                        dataResponse.postValue(
                                            Resource.error(
                                                response.message(),
                                                response.code(),
                                                null,
                                                (call.request() as Request)
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                            dataResponse.postValue(
                                Resource.error(
                                    t.message.toString(),
                                    0,
                                    null,
                                    (call.request() as Request)
                                )
                            )
                        }
                    })
            } else {
                dataResponse.postValue(Resource.error("No internet connection", 0, null, null))
            }
            return dataResponse
        }catch (e: Exception){
            return dataResponse
        }
    }
}