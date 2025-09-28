package com.fatkhun.core.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.fatkhun.core.helper.NetworkHelper
import com.fatkhun.core.helper.StoreDataHelper
import com.fatkhun.core.model.LoginForm
import com.fatkhun.core.model.LoginResponse
import com.fatkhun.core.model.RegisterForm
import com.fatkhun.core.model.RegisterResponse
import com.fatkhun.core.network.RetrofitInstance
import com.fatkhun.core.utils.Resource
import com.fatkhun.core.utils.isNotNull
import com.fatkhun.core.utils.isSuccessAndNotNull
import com.google.gson.Gson
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
}