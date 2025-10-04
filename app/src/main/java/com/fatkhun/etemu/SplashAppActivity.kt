package com.fatkhun.etemu

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.fatkhun.core.helper.StoreDataHelper
import com.fatkhun.core.model.UserItem
import com.fatkhun.core.ui.BaseActivity
import com.fatkhun.core.ui.DataStoreViewModel
import com.fatkhun.core.utils.PrefKey
import com.fatkhun.core.utils.isNotNull
import com.fatkhun.core.utils.logError
import com.fatkhun.etemu.databinding.ActivitySplashAppBinding
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SplashAppActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashAppBinding
    override fun getLayoutId(): View {
        binding = ActivitySplashAppBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        mainVM.getViewDataUser().observe(this) {
            try {
                val idUser = Gson().fromJson(it, UserItem::class.java)
                logError("id_user $idUser")
                if (idUser.isNotNull() && idUser.id != 0) {
                    if (storeDataHelper.isLoginUser() == "1") {
                        preferenceVM.setSecureDataValue(PrefKey.IS_LOGIN, "1")
                    } else {
                        preferenceVM.setSecureDataValue(PrefKey.IS_LOGIN, "0")
                    }
                    val intent = Intent(this@SplashAppActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this@SplashAppActivity, AuthActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }catch (_: Exception){
                val intent = Intent(this@SplashAppActivity, AuthActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}