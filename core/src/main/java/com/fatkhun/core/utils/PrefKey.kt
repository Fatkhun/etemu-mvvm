package com.fatkhun.core.utils

import androidx.datastore.preferences.core.stringPreferencesKey

object PrefKey {
    val BASE_URL_ETEMU = stringPreferencesKey("base_url_etemu")
    val IS_LOGIN = stringPreferencesKey("is_login")
    val AUTH_TOKEN = stringPreferencesKey("auth_token")
    val DATA_USER = stringPreferencesKey("data_user")
}