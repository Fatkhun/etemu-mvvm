package com.fatkhun.core.utils

import okhttp3.Request

data class Resource<T>(
    val status: Status,
    val data: T?,
    val message: String?,
    val code: Int?,
    val request: Request?
) {

    companion object {

        fun <T> success(data: T?, code: Int? = 0, request: Request?): Resource<T> {
            return Resource(Status.SUCCESS, data, null, code, request)
        }

        fun <T> error(msg: String, code: Int = 0, data: T?, request: Request?): Resource<T> {
            return Resource(Status.ERROR, data, msg, code, request)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null, 0, null)
        }

    }

}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}