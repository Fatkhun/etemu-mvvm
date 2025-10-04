package com.fatkhun.core.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import com.fatkhun.core.helper.NetworkHelper
import com.fatkhun.core.helper.StoreDataHelper
import com.fatkhun.core.utils.StatusBar.applyEdgeToEdgeInsets
import com.fatkhun.core.utils.changeLocale
import org.koin.android.ext.android.inject
import kotlin.getValue

abstract class BaseActivity : AppCompatActivity(), View.OnClickListener {

    val mainVM by inject<MainViewModel>()
    val preferenceVM by inject<DataStoreViewModel>()
    val firebaseVM by inject<FirebaseRemoteConfigViewModel>()
    val network by inject<NetworkHelper>()
    val storeDataHelper by inject<StoreDataHelper>()
    private var loadingDialog: LoadingDialog? = null

    abstract fun getLayoutId(): View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        loadingDialog = LoadingDialog(this)

        network.observe(this) {
            when (it?.isConnected) {
                true -> {
                    when (it.type) {
                        NetworkHelper.WifiData, NetworkHelper.MobileData, NetworkHelper.VpnData -> {
                            network.checkConnection(this, it.isConnected)
                        }
                    }
                }

                else -> {
                    network.checkConnection(this, it.isConnected)
                }
            }
        }
    }



    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.changeLocale("id"))
    }

    override fun onClick(v: View?) {
        onBackPressed()
    }

    @Keep
    fun showLoading(title: String = "Sedang diproses") {
        loadingDialog?.let {
            if (!it.isShowing && !isFinishing) {
                it.show()
                it.setTitleLoading(title)
            }
        }
    }

    @Keep
    fun dismissLoading() {
        loadingDialog?.let {
            if (it.isShowing && !isFinishing) {
                it.dismiss()
            }
        }
    }
}