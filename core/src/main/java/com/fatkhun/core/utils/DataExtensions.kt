package com.fatkhun.core.utils

import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

fun Any?.isNull(): Boolean = this == null

fun Any?.isNotNull(): Boolean = !this.isNull()

fun Any?.isNull(
    block: () -> Unit
): Boolean {

    if (this == null) {
        block.invoke()
        return true
    }

    return false
}

fun Any?.isNotNull(
    block: () -> Unit
): Boolean {
    if (this != null) {
        block.invoke()
        return true
    }

    return false
}

fun Any.logError(message: String) {
    Log.e(this::class.java.name, message)
}

fun String.toRequestBodyText(): RequestBody =
    this.toRequestBody("text/plain".toMediaTypeOrNull())

fun File.toMultipartBodyImage(name: String): MultipartBody.Part {
    return MultipartBody.Part.createFormData(
        name, this.name, this.asRequestBody("image/*".toMediaTypeOrNull())
    )
}

fun File.toMultipartBodyPdf(name: String): MultipartBody.Part {
    return MultipartBody.Part.createFormData(
        name, this.name, this.asRequestBody("application/pdf".toMediaTypeOrNull())
    )
}