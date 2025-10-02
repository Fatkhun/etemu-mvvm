package com.fatkhun.core.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
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
import com.fatkhun.core.model.RegisterForm
import com.fatkhun.core.model.RegisterResponse
import com.fatkhun.core.repository.DataStoreRepository
import com.fatkhun.core.repository.MainRepository
import com.fatkhun.core.utils.Resource
import okhttp3.MultipartBody

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

    fun getCategoryList(): MutableLiveData<Resource<CategoriesResponse>> {
        return mainRepository.getCategoryList()
    }

    fun getDetailItem(
        itemId: String
    ): MutableLiveData<Resource<DetailItemResponse>> {
        return mainRepository.getDetailItem(itemId)
    }

    fun getLostFoundList(
        form: LostFoundForm
    ): MutableLiveData<Resource<LostFoundResponse>> {
        return mainRepository.getLostFoundList(form)
    }

    // Buat key sederhana agar memicu query hanya saat kamu set explicit
    data class KeyLostFound(
        val form: LostFoundForm
    )
    private val keyLostFoundLive = MutableLiveData<KeyLostFound>()
    // Stream Paging; tidak buat Pager baru kecuali key diganti
    val pagingLostFound: LiveData<PagingData<LostFoundItemList>> =
        keyLostFoundLive.switchMap { k ->
            mainRepository.getLostFoundPaging(k.form)
                .cachedIn(viewModelScope) // cache di scope VM
        }
    fun submitKeyLostFound(form: LostFoundForm) {
        keyLostFoundLive.value = KeyLostFound(form)
    }

    // Buat key sederhana agar memicu query hanya saat kamu set explicit
    data class KeyHistory(
        val token: String,
        val form: LostFoundForm
    )
    private val keyHistoryLive = MutableLiveData<KeyHistory>()
    // Stream Paging; tidak buat Pager baru kecuali key diganti
    val pagingHistory: LiveData<PagingData<LostFoundItemList>> =
        keyHistoryLive.switchMap { k ->
            mainRepository.getHistoryListPaging(k.token,k.form)
                .cachedIn(viewModelScope) // cache di scope VM
        }
    fun submitKeyHistory(token: String, form: LostFoundForm) {
        keyHistoryLive.value = KeyHistory(token, form)
    }

    fun postingItem(
        token: String, form: PostingItemForm
    ): MutableLiveData<Resource<BaseResponse>> {
        return mainRepository.postingItem(token, form)
    }

    fun updatePostingItem(
        token: String, itemId: String, form: PostingUpdateForm
    ): MutableLiveData<Resource<BaseResponse>> {
        return mainRepository.updatePostingItem(token, itemId, form)
    }

}