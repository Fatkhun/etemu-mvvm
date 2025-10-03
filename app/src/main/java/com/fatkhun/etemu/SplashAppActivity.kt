package com.fatkhun.etemu

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.fatkhun.core.helper.StoreDataHelper
import com.fatkhun.core.ui.BaseActivity
import com.fatkhun.core.ui.DataStoreViewModel
import com.fatkhun.core.utils.PrefKey
import com.fatkhun.core.utils.logError
import com.fatkhun.etemu.databinding.ActivitySplashAppBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SplashAppActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashAppBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashAppBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        runBlocking {
            val idUser = storeDataHelper.getDataUser().id
            val isLogin = storeDataHelper.isLoginUser()
            delay(2000)
            logError("id_user $idUser __ ${isLogin}")
            if (idUser.isNotEmpty() && idUser != "0") {
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
        }
    }
}