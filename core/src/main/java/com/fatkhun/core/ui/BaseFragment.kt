package com.fatkhun.core.ui

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import com.fatkhun.core.helper.NetworkHelper
import com.fatkhun.core.helper.StoreDataHelper
import com.fatkhun.core.utils.StatusBar.applyEdgeToEdgeInsets
import org.koin.android.ext.android.inject
import kotlin.getValue

open class BaseFragment : Fragment(), View.OnClickListener {

    val mainVM by inject<MainViewModel>()
    val preferenceVM by inject<DataStoreViewModel>()
    val firebaseVM by inject<FirebaseRemoteConfigViewModel>()
    val network by inject<NetworkHelper>()
    val storeDataHelper by inject<StoreDataHelper>()
    private var loadingDialog: LoadingDialog? = null
    private var baseActivity: BaseActivity? = null
    var mIsVisibleToUser: Boolean = false
    var useTopEdge: Boolean = true
    var useBottomEdge: Boolean = true
    var useCompatibility: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BaseActivity) {
            val activity = context as BaseActivity?
            this.baseActivity = activity
            loadingDialog = LoadingDialog(context)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mIsVisibleToUser = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getBaseActivity()?.applyEdgeToEdgeInsets(useTopEdge, useBottomEdge)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM && useCompatibility) {
            view.fitsSystemWindows = true
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        mIsVisibleToUser = false
        dismissLoading()
        super.onDestroyView()
    }

    override fun onClick(v: View?) {}

    override fun onDetach() {
        baseActivity = null
        super.onDetach()
    }

    fun getBaseActivity() = baseActivity

    fun setBaseActivity(activity: BaseActivity?) {
        baseActivity = activity
    }

    @Keep
    fun showLoading(title: String = "Sedang diproses") {
        getBaseActivity()?.let { act ->
            loadingDialog?.let { load ->
                if (!load.isShowing && !act.isFinishing) {
                    load.show()
                    load.setTitleLoading(title)
                }
            }
        }
    }

    @Keep
    fun dismissLoading() {
        getBaseActivity()?.let { act ->
            loadingDialog?.let { load ->
                if (load.isShowing && !act.isFinishing) {
                    load.dismiss()
                }
            }
        }
    }
}