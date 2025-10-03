package com.fatkhun.etemu

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.drawToBitmap
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import com.fatkhun.core.helper.PermissionHelper
import com.fatkhun.core.model.PostingItemForm
import com.fatkhun.core.ui.BaseActivity
import com.fatkhun.core.utils.AlertDialogInterface
import com.fatkhun.core.utils.RC
import com.fatkhun.core.utils.RemoteCallback
import com.fatkhun.core.utils.combinePhotoWithFrame
import com.fatkhun.core.utils.dialogAlertOneButton
import com.fatkhun.core.utils.disable
import com.fatkhun.core.utils.enable
import com.fatkhun.core.utils.getFilePathFromUri
import com.fatkhun.core.utils.getSavedImageUri
import com.fatkhun.core.utils.getScaledBitmap
import com.fatkhun.core.utils.getVisibleBitmapFromPhotoView
import com.fatkhun.core.utils.gone
import com.fatkhun.core.utils.handleApiCallback
import com.fatkhun.core.utils.isNotNull
import com.fatkhun.core.utils.logError
import com.fatkhun.core.utils.resizeBitmapToFitFrame
import com.fatkhun.core.utils.rotateBitmap
import com.fatkhun.core.utils.rotateImageIfRequired
import com.fatkhun.core.utils.saveBitmapToCache
import com.fatkhun.core.utils.saveImageFromUriToGallery
import com.fatkhun.core.utils.setCustomeTextHTML
import com.fatkhun.core.utils.showCustomDialog
import com.fatkhun.core.utils.showSnackBar
import com.fatkhun.core.utils.visible
import com.fatkhun.etemu.databinding.ActivityPostingBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.radiobutton.MaterialRadioButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostingActivity : BaseActivity() {

    companion object {
        private val CAMERA_REQUEST_CODE = 2
    }

    private lateinit var binding: ActivityPostingBinding
    private lateinit var originalBitmap: Bitmap
    private lateinit var photoUri: Uri
    private lateinit var photoFile: File
    private var idCategory: String = ""
    private var tipeBarang: String = ""
    private var tipeKontak: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPostingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.tvTitleToolbar.text = "Buat Posting"
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
        binding.mbTakePhoto.setOnClickListener {
            PermissionHelper(this).requestCameraPermission { isGrant ->
                if (isGrant) {
                    openCamera(this)
                } else {
                    PermissionHelper(this).openAppSetting()
                }
            }
        }
        binding.tvLabelNamaBarang.text = setCustomeTextHTML(resources.getString(com.fatkhun.core.R.string.app_text_nama_barang))
        binding.tvLabelTipe.text = setCustomeTextHTML(resources.getString(com.fatkhun.core.R.string.app_text_tipe_barang))
        binding.tvLabelKontak.text = setCustomeTextHTML(resources.getString(com.fatkhun.core.R.string.app_text_kontak))
        binding.tvLabelKategori.text = setCustomeTextHTML(resources.getString(com.fatkhun.core.R.string.app_text_kategori_barang))
        binding.tvDeskripsiBarang.text = setCustomeTextHTML(resources.getString(com.fatkhun.core.R.string.app_text_deskripsi))
        binding.mbRotateCamera.setOnClickListener {
            val rotatedBitmap = rotateBitmap(originalBitmap, 90f)
            originalBitmap = rotatedBitmap
            binding.pvImageView.setImageBitmap(rotatedBitmap)
        }
        binding.rgTipeBarang.setOnCheckedChangeListener { group, checkedId  ->
            val selectedRadio = group.findViewById<RadioButton>(checkedId)
            val selectedValue = when (checkedId) {
                R.id.rbLost -> "lost"
                R.id.rbFound -> "found"
                else -> ""
            }
            if (selectedRadio.isChecked) {
                tipeBarang = selectedValue
                logError("tipe $tipeBarang")
            } else {
                tipeBarang = ""
            }
        }
        binding.rgKontak.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadio = group.findViewById<RadioButton>(checkedId)
            val selectedValue = when (checkedId) {
                R.id.rbWhatsapp -> "whatsapp"
                R.id.rbTelegram -> "telegram"
                else -> ""
            }
            if (selectedRadio.isChecked) {
                tipeKontak = selectedValue
                binding.tilKontak.visible()
                binding.edtKontak.hint = if (tipeKontak.lowercase() == "whatsapp") "Masukkan nomor WA" else "Masukkan username Telegram"
                logError("tipe $tipeKontak")
            } else {
                tipeKontak = ""
                binding.tilKontak.gone()
            }
        }
        binding.edtName.doAfterTextChanged { fullName ->
            if (validName(fullName.toString()) &&
                validNomorHp(binding.edtKontak.text.toString()) &&
                validDesc(binding.tieDeskripsi.text.toString())
            ) {
                listenerRegisterEnable()
            } else {
                listenerRegisterDisable()
            }
        }
        binding.edtKontak.doAfterTextChanged { data ->
            if (validName(binding.edtName.toString()) &&
                validNomorHp(data.toString()) &&
                validDesc(binding.tieDeskripsi.text.toString())
            ) {
                listenerRegisterEnable()
            } else {
                listenerRegisterDisable()
            }
        }
        binding.tieDeskripsi.doAfterTextChanged { data ->
            if (validName(binding.edtName.toString()) &&
                validNomorHp(binding.edtKontak.toString()) &&
                validDesc(data.toString())
            ) {
                listenerRegisterEnable()
            } else {
                listenerRegisterDisable()
            }
        }
        initConfigPhotoView()
        getCategory()
    }

    private fun listenerRegisterDisable() {
        binding.mbNext.disable()
        binding.mbNext.setOnClickListener(null)
    }

    private fun listenerRegisterEnable() {
        binding.mbNext.enable()
        binding.mbNext.setOnClickListener{
            sendPosting()
        }
    }

    private fun validName(value: String): Boolean {
        if (value.isBlank() || value.isEmpty()) {
            binding.tvErrorName.visible()
            return false
        } else {
            binding.tvErrorName.gone()
            return true
        }
    }

    private fun validNomorHp(value: String): Boolean {
        if (value.isBlank() || value.isEmpty()) {
            binding.tvErrorKontak.visible()
            return false
        } else {
            binding.tvErrorKontak.gone()
            return true
        }
    }

    private fun validDesc(value: String): Boolean {
        if (value.isBlank() || value.isEmpty()) {
            binding.tvErrorDeskripsi.visible()
            return false
        } else {
            binding.tvErrorDeskripsi.gone()
            return true
        }
    }

    private fun openCamera(activity: Activity) {

        val intent = Intent(activity, CameraXActivity::class.java)
        intent.putExtra(CameraXActivity.IS_FRONT, true)
        startActivityForResult(intent,
            CAMERA_REQUEST_CODE
        )
    }

    private fun initConfigPhotoView() {
        binding.pvImageView.apply {
            maximumScale = 10f
            mediumScale = 5f
            minimumScale = 1f
        }
    }

    private fun isCanRotateCamera(bitmap: Bitmap) {
        if (bitmap.isNotNull()) {
            binding.mbRotateCamera.visible()
        } else {
            binding.mbRotateCamera.gone()
        }
    }

    private fun getCategory() {
        mainVM.getCategoryList().observe(this) { response ->
            handleApiCallback(
                this@PostingActivity,
                response,
                false,
                object : RemoteCallback<String> {
                    override fun do_callback(id: Int, t: String) {}
                    override fun failed_callback(id: Int, t: String) {}
                }) { res, code ->
                    if (code == RC().SUCCESS) {
                        res?.let {
                            binding.cgKategori.removeAllViews()
                            it.data.forEachIndexed { index, item ->
                                val chip = layoutInflater.inflate(
                                    R.layout.component_chip_category,
                                    binding.cgKategori,
                                    false
                                ) as Chip
                                chip.text = item.name
                                chip.isCheckable = true
                                chip.id = index + 1
                                binding.cgKategori.addView(chip)
                            }
                            binding.cgKategori.isSingleSelection = true
                            binding.cgKategori.children.forEachIndexed { index, view ->
                                val chip = view as Chip
                                chip.setOnCheckedChangeListener {  _, isChecked ->
                                    if (isChecked) {
                                        idCategory = it.data[index]._id
                                        logError("kategori $idCategory")
                                    } else {
                                        idCategory = ""
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun sendPosting() {
        val nama = binding.edtName.text.toString()
        val kontak = binding.edtKontak.text.toString()
        val deskripsi = binding.tieDeskripsi.text.toString()

        if (nama.isBlank() || kontak.isBlank() || deskripsi.isBlank() ||
            idCategory.isBlank() || tipeBarang.isBlank() || tipeKontak.isBlank()) {
            showSnackBar(this@PostingActivity, "Isi data dengan lengkap yaa")
            return
        }

        val form = PostingItemForm(
            categoryId = idCategory,
            type = tipeBarang,
            name = nama,
            description = deskripsi,
            contactType = tipeKontak,
            contactValue = kontak,
            file_evidence_path = ""
        )
        mainVM.postingItem(storeDataHelper.getAuthToken(), form).observe(this@PostingActivity) { responseBody ->
            handleApiCallback(
                this@PostingActivity,
                responseBody,
                true,
                object : RemoteCallback<String> {
                    override fun do_callback(id: Int, t: String) {}
                    override fun failed_callback(id: Int, t: String) {}
                }) { res, code ->
                    if (code == RC().CREATED) {
                        res?.let {
                            showSnackBar(this@PostingActivity, "Berhasil upload")
                            onBackPressed()
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
        /*callToShare(activity) {
            CoroutineScope(Dispatchers.Main).launch {
                it?.let {
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val filenameExt = "${resources.getString(R.string.app_name)}${timeStamp}.jpg"
                    saveImageFromUriToGallery(this@PostingActivity, it, filenameExt, 50) { imgFileName ->
                        getSavedImageUri(this@PostingActivity, imgFileName)?.let { imgUri ->
                            if (imgUri.isNotNull()) {
                                // todo api item
                                val nama = binding.edtName.text.toString()
                                val form = PostingItemForm(
                                    categoryId = "",
                                    type = "",
                                    name = "",
                                    description = "",
                                    contactType = "",
                                    contactValue = "",
                                    file_evidence_path = getFilePathFromUri(this@PostingActivity, imgUri)!!
                                )
                                mainVM.postingItem(storeDataHelper.getAuthToken(), form).observe(this@PostingActivity) { responseBody ->
                                    handleApiCallback(
                                        this@PostingActivity,
                                        responseBody,
                                        true,
                                        object : RemoteCallback<String> {
                                            override fun do_callback(id: Int, t: String) {}
                                            override fun failed_callback(id: Int, t: String) {}
                                        }) { res, code ->

                                    }
                                }
                            } else {
                                showSnackBar(this@PostingActivity, "Gambar tidak ditemukan")
                            }
                        }
                    }
                }
            }
        }*/
    }

    private fun callToShare(activity: Activity, callback: (Uri?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val visibleBitmap = getVisibleBitmapFromPhotoView(binding.pvImageView)
            val finalBitmap = createImage(visibleBitmap, binding.pvImageView.drawToBitmap())
            val uri = saveBitmapToCache(activity, finalBitmap, resources.getString(R.string.app_name))
            //logError("result uri $uri")
            callback.invoke(uri)
        }
    }

    fun createImage(photoBitmap: Bitmap, frameBitmap: Bitmap): Bitmap {
        // Resize photo to fit frame
        val resizedPhoto = resizeBitmapToFitFrame(photoBitmap, frameBitmap.width, frameBitmap.height)

        // Combine photo with frame
        return combinePhotoWithFrame(resizedPhoto, frameBitmap)
    }

    private fun isCanShare(activity: Activity, bitmap: Bitmap) {
        if (bitmap.isNotNull()) {
            binding.mbNext.apply {
                enable()
                setOnClickListener {
                    // cek android 14
                    showLoading()
                    if (PermissionHelper.allPermissionsGranted(this@PostingActivity, PermissionHelper.DOWNLOAD_REQUIRED_PERMISSIONS)) {
                        sendPosting()
                    } else {
                        PermissionHelper(activity).requestDownloadPermission { isGrant ->
                            if (isGrant) {
                                // Permission granted, proceed with the action
                                sendPosting()
                            } else {
                                showCustomDialog(activity,
                                    "Tidak Dapat Mengakses Penyimpanan",
                                    PermissionHelper.MESSAGE_NEED_ACCESS_STORAGE,
                                    "Izinkan",
                                    "Batal",
                                    false,
                                    object : AlertDialogInterface {
                                        override fun onPositiveButtonClicked() {
                                            PermissionHelper(activity).openAppSetting()
                                        }

                                        override fun onNegativeButtonClicked() {}
                                    })
                                dismissLoading()
                            }
                        }
                    }
                }
            }
        } else {
            binding.mbNext.apply {
                disable()
                setOnClickListener(null)
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val imagePath = data?.getStringExtra(CameraXActivity.KEY_RESULT_FILE)
                    if (imagePath != null) {
                        try {
                            val photoFile = File(imagePath)
                            val photoUri = photoFile.toUri()

                            val thumbnail = getScaledBitmap(photoFile.absolutePath, binding.pvImageView.width, binding.pvImageView.height)
                            val rotatedBitmap = rotateImageIfRequired(this, thumbnail, photoUri)
                            originalBitmap = rotatedBitmap
                            isCanRotateCamera(originalBitmap)
                            isCanShare(this, originalBitmap)
                            binding.pvImageView.setImageBitmap(originalBitmap)
                        } catch (e: Exception) {
                            showSnackBar(this, "Gagal mengambil gambar. Silahkan cek pengaturan kamera anda.")
                        }
                    } else {
                        showSnackBar(this, "Gagal mengambil gambar. Silahkan cek pengaturan kamera anda.")
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}