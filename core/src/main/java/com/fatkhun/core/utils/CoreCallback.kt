package com.fatkhun.core.utils

interface RemoteCallback<T> {
    fun do_callback(id: Int, t: T)
    fun failed_callback(id: Int, t: T)
}

