package com.fatkhun.core.utils

import android.content.Context
import androidx.core.content.FileProvider

class PostingFileProvider : FileProvider() {
    companion object {
        fun getProviderName(context: Context): String {
            return "${context.applicationContext.packageName}.file-provider-posting"
        }
    }
}