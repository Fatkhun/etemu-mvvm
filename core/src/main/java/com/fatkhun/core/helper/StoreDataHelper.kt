package com.fatkhun.core.helper

import android.content.Context
import com.fatkhun.core.model.UserItem
import com.fatkhun.core.ui.DataStoreViewModel
import com.fatkhun.core.utils.PrefKey
import com.google.gson.Gson

class StoreDataHelper(
    val context: Context,
    val dataStoreViewModel: DataStoreViewModel,
) {

    fun isLoginUser(): String {
        var isLogin = ""
        dataStoreViewModel.getSecureDataValue(PrefKey.IS_LOGIN, "0") {
            isLogin = it
        }
        return isLogin
    }

    fun getDataUser(): UserItem {
        var user = UserItem()
        dataStoreViewModel.getSecureDataValue(PrefKey.DATA_USER, "") {
            user = try {
                Gson().fromJson(it, UserItem::class.java)
            } catch (e: Exception) {
                UserItem()
            }
        }
        return user
    }
}