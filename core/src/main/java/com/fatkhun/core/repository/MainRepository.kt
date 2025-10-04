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
import com.fatkhun.core.model.CategoriesResponse
import com.fatkhun.core.model.DetailItemResponse
import com.fatkhun.core.model.LoginForm
import com.fatkhun.core.model.LoginResponse
import com.fatkhun.core.model.LostFoundForm
import com.fatkhun.core.model.LostFoundItemList
import com.fatkhun.core.model.LostFoundResponse
import com.fatkhun.core.model.PostingItemForm
import com.fatkhun.core.model.PostingUpdateForm
import com.fatkhun.core.model.PostingUpdateStatusForm
import com.fatkhun.core.model.RegisterForm
import com.fatkhun.core.model.RegisterResponse
import com.fatkhun.core.network.RetrofitInstance
import com.fatkhun.core.network.RetrofitRoutes
import com.fatkhun.core.ui.HistoryPagingSource
import com.fatkhun.core.ui.LostFoundPagingSource
import com.fatkhun.core.utils.Constant
import com.fatkhun.core.utils.Resource
import com.fatkhun.core.utils.isNotNull
import com.fatkhun.core.utils.isSuccessAndNotNull
import com.fatkhun.core.utils.logError
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
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

    fun getCategoryList(): MutableLiveData<Resource<CategoriesResponse>> {
        val dataResponse = MutableLiveData<Resource<CategoriesResponse>>()

        dataResponse.postValue(Resource.loading(null))
        if (networkHelper.isNetworkConnected()) {
            retrofitInstance.provideRetrofit(RetrofitInstance.DesClient.ETEMU)
                .getCategoryList().enqueue(object : Callback<CategoriesResponse> {
                    override fun onResponse(
                        call: Call<CategoriesResponse>,
                        response: Response<CategoriesResponse>
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
                                        val gson = Gson().fromJson(raw, CategoriesResponse::class.java)
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

                    override fun onFailure(call: Call<CategoriesResponse>, t: Throwable) {
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

    fun getDetailItem(
        itemId: String
    ): MutableLiveData<Resource<DetailItemResponse>> {
        val dataResponse = MutableLiveData<Resource<DetailItemResponse>>()

        dataResponse.postValue(Resource.loading(null))
        if (networkHelper.isNetworkConnected()) {
            retrofitInstance.provideRetrofit(RetrofitInstance.DesClient.ETEMU)
                .getDetailItem(itemId).enqueue(object : Callback<DetailItemResponse> {
                    override fun onResponse(
                        call: Call<DetailItemResponse>,
                        response: Response<DetailItemResponse>
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
                                        val gson = Gson().fromJson(raw, DetailItemResponse::class.java)
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

                    override fun onFailure(call: Call<DetailItemResponse>, t: Throwable) {
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
        data["category_id"] = form.category_id.toString()
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

    fun getHistoryListPaging(
        token: String,
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
                HistoryPagingSource(
                    service = { api },
                    token, form
                )
            }
        ).liveData
        emitSource(pagingLive)
    }

    fun updateStatusPostingItem(
        token: String,
        itemId: String,
        form: PostingUpdateStatusForm
    ): MutableLiveData<Resource<BaseResponse>> {
        val dataResponse = MutableLiveData<Resource<BaseResponse>>()
        dataResponse.postValue(Resource.loading(null))
        if (networkHelper.isNetworkConnected()) {
            retrofitInstance.provideRetrofit(RetrofitInstance.DesClient.ETEMU)
                .updateStatusPostingItem("Bearer $token", itemId, form).enqueue(object : Callback<BaseResponse> {
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
    }

    fun updatePostingItem(
        token: String,
        itemId: String,
        form: PostingUpdateForm
    ): MutableLiveData<Resource<BaseResponse>> {
        val dataResponse = MutableLiveData<Resource<BaseResponse>>()
        try {

            val parts = mutableMapOf<String, RequestBody>()

            // Prepare the file part
            val path = form.photo
            val filePart: MultipartBody.Part = try {
                val file = File(path)
                if (path.isNotEmpty() && file.exists()) {
                    val requestFile = file.asRequestBody("image/jpg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("photo", file.name, requestFile)
                } else {
                    throw IllegalArgumentException("File path is empty or file doesn't exist")
                }
            } catch (e: Exception) {
                // Fallback ke empty part
                val emptyRequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("photo", "", emptyRequestBody)
            }

            // Prepare other parts
            if (form.name.isNotEmpty()) parts["name"] = form.name.toRequestBody("text/plain".toMediaTypeOrNull())
            if (form.description.isNotEmpty()) parts["description"] = form.description.toRequestBody("text/plain".toMediaTypeOrNull())
            if (form.category_id != 0) parts["category_id"] = form.category_id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            if (form.contact_type.isNotEmpty()) parts["contact_type"] = form.contact_type.toRequestBody("text/plain".toMediaTypeOrNull())
            if (form.contact_value.isNotEmpty()) parts["contact_value"] = form.contact_value.toRequestBody("text/plain".toMediaTypeOrNull())
            if (form.status.isNotEmpty()) parts["status"] = form.status.toRequestBody()
            if (form.type.isNotEmpty()) parts["type"] = form.type.toRequestBody()
            if (form.owner_id != 0) parts["owner_id"] = form.owner_id.toString().toRequestBody()

            dataResponse.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                retrofitInstance.provideRetrofit(RetrofitInstance.DesClient.ETEMU)
                    .updatePostingItem("Bearer $token", itemId, parts,filePart).enqueue(object : Callback<BaseResponse> {
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
        }catch (_: Exception){
            return dataResponse
        }
        return dataResponse
    }

    fun postingItem(
        token: String,
        form: PostingItemForm
    ): MutableLiveData<Resource<BaseResponse>> {
        val dataResponse = MutableLiveData<Resource<BaseResponse>>()
        try {
            // Prepare the file part
            val path = form.photo_url
            val filePart: MultipartBody.Part = try {
                val file = File(path)
                if (path.isNotEmpty() && file.exists()) {
                    val requestFile = file.asRequestBody("image/jpg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("photo", file.name, requestFile)
                } else {
                    throw IllegalArgumentException("File path is empty or file doesn't exist")
                }
            } catch (e: Exception) {
                // Fallback ke empty part
                val emptyRequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("photo", "", emptyRequestBody)
            }

            // Prepare other parts
            val ownerId = form.user_id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val categoryId = form.id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val type = form.type.toRequestBody("text/plain".toMediaTypeOrNull())
            val name = form.name.toRequestBody("text/plain".toMediaTypeOrNull())
            val description = form.description.toRequestBody("text/plain".toMediaTypeOrNull())
            val contactType = form.contact_type.toRequestBody("text/plain".toMediaTypeOrNull())
            val contactValue = form.contact_value.toRequestBody("text/plain".toMediaTypeOrNull())

            dataResponse.postValue(Resource.loading(null))
            if (networkHelper.isNetworkConnected()) {
                retrofitInstance.provideRetrofit(RetrofitInstance.DesClient.ETEMU)
                    .postingItem("Bearer $token", categoryId, type, name, description, contactType, contactValue, ownerId,filePart).enqueue(object : Callback<BaseResponse> {
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
            logError("err $e")
            return dataResponse
        }
    }
}