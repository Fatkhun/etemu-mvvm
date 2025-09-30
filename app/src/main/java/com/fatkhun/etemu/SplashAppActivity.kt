package com.fatkhun.etemu

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fatkhun.core.helper.StoreDataHelper
import com.fatkhun.core.ui.BaseActivity
import com.fatkhun.core.ui.DataStoreViewModel
import com.fatkhun.core.utils.PrefKey
import com.fatkhun.core.utils.logError
import com.fatkhun.etemu.databinding.ActivitySplashAppBinding

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
        val idUser = storeDataHelper.getDataUser().id
        logError("id_user $idUser")
        if (idUser.isNotEmpty() && idUser != "0") {
            if (storeDataHelper.isLoginUser() == "1") {
                preferenceVM.setSecureDataValue(PrefKey.IS_LOGIN, "1")
            } else {
                preferenceVM.setSecureDataValue(PrefKey.IS_LOGIN, "0")
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}