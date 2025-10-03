package com.fatkhun.etemu

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fatkhun.core.model.LostFoundItemList
import com.fatkhun.core.model.PostingUpdateForm
import com.fatkhun.core.ui.BaseActivity
import com.fatkhun.core.utils.AlertDialogInterface
import com.fatkhun.core.utils.FormatDateTime
import com.fatkhun.core.utils.RC
import com.fatkhun.core.utils.RemoteCallback
import com.fatkhun.core.utils.dialogAlertOneButton
import com.fatkhun.core.utils.gone
import com.fatkhun.core.utils.handleApiCallback
import com.fatkhun.core.utils.isNumber
import com.fatkhun.core.utils.isPackageInstalled
import com.fatkhun.core.utils.load
import com.fatkhun.core.utils.logError
import com.fatkhun.core.utils.normalizationPhonePrefix62
import com.fatkhun.core.utils.openTelegramToUsername
import com.fatkhun.core.utils.sendingMsgWA
import com.fatkhun.core.utils.setCustomeTextHTML
import com.fatkhun.core.utils.shareToTelegram
import com.fatkhun.core.utils.showCustomDialog
import com.fatkhun.core.utils.showSnackBar
import com.fatkhun.core.utils.toJson
import com.fatkhun.core.utils.visible
import com.fatkhun.etemu.databinding.ActivityDetailPostingBinding
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailPostingActivity : BaseActivity() {

    private lateinit var binding: ActivityDetailPostingBinding
    private var dataItem: LostFoundItemList = LostFoundItemList()
    private var isClaimed: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailPostingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val intent = intent.extras
        val detail = intent?.getString("detail", "")
        val isClaimed = intent?.getInt("is_claimed", 0)
        val isHistory = intent?.getBoolean("is_history")
        dataItem = try {
            Gson().fromJson(detail.toString(), LostFoundItemList::class.java)
        }catch (_: Exception){
            LostFoundItemList()
        }
        logError("detail ${dataItem.toJson()} __ $isClaimed")
        if (isHistory == true) {
            if (isClaimed == 1) {
                binding.mbDone.gone()
            } else {
                binding.mbDone.visible()
            }
        } else {
            binding.mbDone.gone()
        }
        if (dataItem.contact.type.contains("whatsapp")) {
            binding.mbContact.apply {
                text = "Whatsapp"
                icon = ContextCompat.getDrawable(this@DetailPostingActivity, com.fatkhun.core.R.drawable.ic_call_24)
            }
        } else if (dataItem.contact.type.contains("telegram")) {
            binding.mbContact.apply {
                text = "Telegram"
                icon = ContextCompat.getDrawable(this@DetailPostingActivity, com.fatkhun.core.R.drawable.ic_supervised_user_circle_24)
            }
        } else {
            binding.mbContact.gone()
        }
        binding.mbContact.setOnClickListener {
            if (dataItem.contact.type.contains("whatsapp")) {
                sendingMsgWA(this, normalizationPhonePrefix62(dataItem.contact.value),"")
            } else {
                if (isNumber(dataItem.contact.value)) {
                    shareToTelegram(this, "")
                } else {
                    openTelegramToUsername(this,dataItem.contact.value)
                }
            }
        }
        binding.mbDone.setOnClickListener {
            showCustomDialog(this,
                "Apakah ingin menyelesaikan laporan?",
                "Ubah status menjadi complete untuk menyelesaikan laporan",
                "Iya, Lanjutkan",
                "Batal",
                false,
                object : AlertDialogInterface {
                    override fun onPositiveButtonClicked() {
                        mainVM.updatePostingItem(storeDataHelper.getAuthToken(), dataItem._id,
                            PostingUpdateForm(status = "claimed")).observe(this@DetailPostingActivity) { response ->
                            handleApiCallback(
                                this@DetailPostingActivity,
                                response,
                                true,
                                object : RemoteCallback<String> {
                                    override fun do_callback(id: Int, t: String) {}
                                    override fun failed_callback(id: Int, t: String) {}
                                }) { res, code ->
                                if (code == RC().SUCCESS) {
                                    onBackPressed()
                                } else {
                                    res?.let {
                                        dialogAlertOneButton(
                                            this@DetailPostingActivity,
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

                    override fun onNegativeButtonClicked() {}
                })
        }
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
        getDetailData(dataItem._id)
    }

    private fun getDetailData(id: String) {
        mainVM.getDetailItem(id).observe(this) { response ->
            handleApiCallback(
                this,
                response,
                true,
                object : RemoteCallback<String> {
                    override fun do_callback(id: Int, t: String) {}
                    override fun failed_callback(id: Int, t: String) {}
                }) { res, code ->
                    if(code == RC().SUCCESS) {
                        res?.data?.let {
                            binding.ivBarang.load(this, it.photoUrl)
                            binding.tvNamaBarang.text = setCustomeTextHTML(it.name)
                            binding.tvDate.text = FormatDateTime.parse(it.updatedAt,FormatDateTime.FORMAT_DATE_TIME_YMDTHMSZ,
                                FormatDateTime.FORMAT_DATE_TIME_DMYHM_LONG_MONTH_NO_SEPARATOR)
                            binding.tvCategory.text = it.category.name
                            binding.tvNamaPelapor.text = it.owner.name
                            binding.tvStatus.text = if (it.status.lowercase() == "claimed") "complete".uppercase() else it.status.uppercase()
                            binding.tvDeskripsi.text = setCustomeTextHTML(it.description)
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
                                onBackPressed()
                                it.dismiss()
                            }
                        }
                    }
                }
        }
    }
}