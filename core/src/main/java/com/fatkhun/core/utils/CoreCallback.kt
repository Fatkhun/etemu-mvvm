package com.fatkhun.core.utils

import android.content.Context
import androidx.fragment.app.Fragment
import com.fatkhun.core.R
import com.fatkhun.core.ui.BaseActivity

interface RemoteCallback<T> {
    fun do_callback(id: Int, t: T)
    fun failed_callback(id: Int, t: T)
}

fun <T> BaseActivity.handleApiCallback(
    context: Context,
    status: Resource<T>,
    loading: Boolean = false,
    refreshToken: RemoteCallback<String>,
    callback: (respone: T?, code: Int) -> Unit
) {
    logError(
        "TAGLOGED5 handleApiCallback: Status " + status.status + " || Message : " + status.message + " || Url : " + status.request?.url
    )
    when (status.status) {
        Status.LOADING -> {
            if (loading) {
                showLoading()
            } else {
                dismissLoading()
            }
        }

        Status.SUCCESS -> {
            when (status.code) {
                500 -> {
                    dialogAlertOneButton(
                        context,
                        R.drawable.ic_ilus_general_warning,
                        "Lagi Terjadi kesalahan teknis nih, coba lagi atau kembali nanti ya.",
                        "",
                        "OK"
                    ) {
                        it.dismiss()
                    }
                }

                else -> {
                    if (status.code != null) {
                        callback.invoke(status.data, status.code)
                    }
                }
            }
            dismissLoading()
        }

        Status.ERROR -> {
            when (status.code) {
                500 -> {
                    dialogAlertOneButton(
                        context,
                        R.drawable.ic_ilus_general_warning,
                        "Lagi Terjadi kesalahan teknis nih, coba lagi atau kembali nanti ya.",
                        "",
                        "OK"
                    ) {
                        it.dismiss()
                    }
                }

                503, 504 -> {
                    dialogAlertOneButton(
                        context,
                        R.drawable.ic_ilus_general_warning,
                        "Yah, internetnya mati.. Cek Koneksi WiFi atau kuota internetmu dan coba lagi ya",
                        "",
                        "OK"
                    ) {
                        it.dismiss()
                        status.code.let { code ->
                            refreshToken.failed_callback(code, "")
                        }
                    }
                }

                else -> {
                    if (status.message != null) {
                        logError("Exception ${status.message}")
                        if (status.message.contains("exception", true) ||
                            status.message.contains("timeout", true)) {
                            dialogAlertOneButton(
                                context,
                                R.drawable.ic_ilus_general_warning,
                                "Terjadi kesalahan proses data. Coba lagi ya.",
                                "",
                                "OK"
                            ) {
                                dismissLoading()
                                it.dismiss()
                                status.code?.let { code ->
                                    refreshToken.failed_callback(code, status.message)
                                }
                            }
                        }
                    }
                    if (status.code != null) {
                        callback.invoke(status.data, status.code)
                    }
                }
            }
            dismissLoading()
        }
    }
}

fun <T> Fragment.handleApiCallback(
    context: Context,
    status: Resource<T>,
    loading: Boolean = false,
    refreshToken: RemoteCallback<String>,
    callback: (respone: T?, code: Int) -> Unit
) {
    try {
        if (context is BaseActivity) {
            context.handleApiCallback(
                context,
                status,
                loading,
                refreshToken,
                callback
            )
        } else {
            when (status.status) {
                Status.LOADING -> {
                    // opsional: kalau bukan BaseActivity, loading bisa di-skip
                    logError("Loading state (non-BaseActivity)")
                }
                Status.SUCCESS -> {
                    when(status.code) {
                        500 -> {
                            dialogAlertOneButton(
                                context,
                                R.drawable.ic_ilus_general_warning,
                                "Lagi Terjadi kesalahan teknis nih, coba lagi atau kembali nanti ya.",
                                "",
                                "OK"
                            ) {
                                it.dismiss()
                            }
                        }

                        else -> {
                            if (status.code != null) {
                                callback.invoke(status.data, status.code)
                            }
                        }
                    }
                }
                Status.ERROR -> {
                    when(status.code) {
                        500 -> {
                            dialogAlertOneButton(
                                context,
                                R.drawable.ic_ilus_general_warning,
                                "Lagi Terjadi kesalahan teknis nih, coba lagi atau kembali nanti ya.",
                                "",
                                "OK"
                            ) {
                                it.dismiss()
                            }
                        }

                        503, 504 -> {
                            dialogAlertOneButton(
                                context,
                                R.drawable.ic_ilus_general_warning,
                                "Yah, internetnya mati.. Cek Koneksi WiFi atau kuota internetmu dan coba lagi ya",
                                "",
                                "OK"
                            ) {
                                it.dismiss()
                                status.code.let { code ->
                                    refreshToken.failed_callback(code, "")
                                }
                            }
                        }

                        else -> {
                            if (status.message != null) {
                                logError("Exception ${status.message}")
                                if (status.message.contains("exception", true) ||
                                    status.message.contains("timeout", true)) {
                                    dialogAlertOneButton(
                                        context,
                                        R.drawable.ic_ilus_general_warning,
                                        "Terjadi kesalahan proses data. Coba lagi ya.",
                                        "",
                                        "OK"
                                    ) {
                                        it.dismiss()
                                        status.code?.let { code ->
                                            refreshToken.failed_callback(code, status.message)
                                        }
                                    }
                                }
                            }
                            if (status.code != null) {
                                callback.invoke(status.data, status.code)
                            }
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}