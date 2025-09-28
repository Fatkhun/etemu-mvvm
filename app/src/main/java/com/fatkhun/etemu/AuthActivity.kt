package com.fatkhun.etemu

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.fatkhun.core.model.LoginForm
import com.fatkhun.core.model.RegisterForm
import com.fatkhun.core.ui.BaseActivity
import com.fatkhun.core.utils.PrefKey
import com.fatkhun.core.utils.RC
import com.fatkhun.core.utils.RemoteCallback
import com.fatkhun.core.utils.afterTextChangedDebounce
import com.fatkhun.core.utils.dialogAlertOneButton
import com.fatkhun.core.utils.disable
import com.fatkhun.core.utils.enable
import com.fatkhun.core.utils.gone
import com.fatkhun.core.utils.handleApiCallback
import com.fatkhun.core.utils.isEmailValid
import com.fatkhun.core.utils.setCustomeTextHTML
import com.fatkhun.core.utils.toJson
import com.fatkhun.core.utils.visible
import com.fatkhun.etemu.databinding.ActivityAuthBinding
import com.google.android.material.textfield.TextInputLayout

class AuthActivity : BaseActivity() {

    lateinit var binding: ActivityAuthBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.tvLoginPass.text =
            setCustomeTextHTML(resources.getString(com.fatkhun.core.R.string.app_text_register_pass))
        binding.tvLoginEmail.text =
            setCustomeTextHTML(resources.getString(com.fatkhun.core.R.string.app_text_register_email))
        binding.edtEmail.afterTextChangedDebounce(1000, {
            listenerRegisterEnable()
            binding.tilEmail.apply {
                endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
                setEndIconTintList(ContextCompat.getColorStateList(this@AuthActivity, com.fatkhun.core.R.color.b_100))
            }
        }) { userEmail ->
            if (userEmail.length >= 4) {
                if (validUserPassword(binding.edtPassword.text.toString()) &&
                    validEmail(userEmail)
                ) {
                    listenerRegisterEnable()
                } else {
                    listenerRegisterDisable()
                }
            } else {
                listenerRegisterDisable()
                binding.tvErrorEmail.text = "email anda tidak valid"
                binding.tvErrorEmail.visible()
                binding.tilEmail.apply {
                    endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
                    setEndIconTintList(ContextCompat.getColorStateList(this@AuthActivity, com.fatkhun.core.R.color.b_50))
                }
            }
        }
        binding.edtPassword.afterTextChangedDebounce(1000, {
            listenerRegisterDisable()
            binding.tilPassword.apply {
                endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                setEndIconTintList(ContextCompat.getColorStateList(this@AuthActivity, com.fatkhun.core.R.color.b_100))
            }
        }) { value ->
            if (value.length >= 6) {
                if (validUserPassword(value) &&
                    validEmail(binding.edtEmail.text.toString())
                ) {
                    listenerRegisterEnable()
                } else {
                    listenerRegisterDisable()
                }
            } else {
                listenerRegisterDisable()
                binding.tvErrorPassword.text =
                    "password tidak valid, minimal 6 karakter"
                binding.tvErrorPassword.visible()
                binding.tilPassword.apply {
                    endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                    setEndIconTintList(ContextCompat.getColorStateList(this@AuthActivity, com.fatkhun.core.R.color.b_50))
                }
            }
        }
    }

    private fun listenerRegisterDisable() {
        binding.mbNext.disable()
        binding.mbNext.setOnClickListener(null)
    }

    private fun listenerRegisterEnable() {
        binding.mbNext.enable()
        binding.mbNext.setOnClickListener{
            val form = LoginForm(
                email = binding.edtEmail.text.toString(),
                password = binding.edtPassword.text.toString()
            )
            mainVM.loginUser(form).observe(this) { responseBody ->
                handleApiCallback(this, responseBody, true, object : RemoteCallback<String> {
                    override fun do_callback(id: Int, t: String) {}
                    override fun failed_callback(id: Int, t: String) {}
                }) { res, code ->
                    if (code == RC().SUCCESS) {
                        res?.let {
                            preferenceVM.setSecureDataValue(PrefKey.DATA_USER, it.data.user.toJson())
                            preferenceVM.setSecureDataValue(PrefKey.IS_LOGIN, "1")
                            preferenceVM.setSecureDataValue(PrefKey.AUTH_TOKEN, it.data.token)
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    } else {
                        res?.let {
                            dialogAlertOneButton(
                                this,
                                com.fatkhun.core.R.drawable.ic_ilus_general_warning,
                                it.message,
                                "",
                                "Mengerti"
                            ) {
                                it.dismiss()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun validEmail(value: String): Boolean {
        return if (value.isBlank() || value.isEmpty()) {
            binding.tvErrorEmail.visible()
            binding.tvErrorEmail.text = "Isi email dulu, yaa."
            false
        } else {
            if (isEmailValid(value)) {
                binding.tvErrorEmail.gone()
                true
            } else {
                binding.tvErrorEmail.visible()
                binding.tvErrorEmail.text = "Email anda tidak valid"
                false
            }
        }
    }

    private fun validUserPassword(value: String): Boolean {
        return !(value.isBlank() || value.isEmpty())
    }
}