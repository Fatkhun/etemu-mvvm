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
import com.fatkhun.core.model.LostFoundItemList
import com.fatkhun.core.model.PostingItemForm
import com.fatkhun.core.model.PostingUpdateForm
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
import com.fatkhun.core.utils.length
import com.fatkhun.core.utils.load
import com.fatkhun.core.utils.logError
import com.fatkhun.core.utils.resizeBitmapToFitFrame
import com.fatkhun.core.utils.rotateBitmap
import com.fatkhun.core.utils.rotateImageIfRequired
import com.fatkhun.core.utils.saveBitmapToCache
import com.fatkhun.core.utils.saveImageFromUriToGallery
import com.fatkhun.core.utils.setCustomeTextHTML
import com.fatkhun.core.utils.showCustomDialog
import com.fatkhun.core.utils.showSnackBar
import com.fatkhun.core.utils.toJson
import com.fatkhun.core.utils.visible
import com.fatkhun.etemu.databinding.ActivityPostingBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.gson.Gson
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
    private var isEdit: Boolean? = false
    private var idCategory: Int = 0
    private var tipeBarang: String = ""
    private var tipeKontak: String = ""
    private var dataItem: LostFoundItemList = LostFoundItemList()
    override fun getLayoutId(): View {
        binding = ActivityPostingBinding.inflate(layoutInflater)
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
        val intent = intent.extras
        val detail = intent?.getString("detail", "")
        isEdit = intent?.getBoolean("is_edit", false)
        dataItem = try {
            Gson().fromJson(detail.toString(), LostFoundItemList::class.java)
        }catch (_: Exception){
            LostFoundItemList()
        }
        binding.tvTitleToolbar.text = if (isEdit == true) "Update Posting" else "Buat Posting"
        binding.mbNext.text = if (isEdit == true) "Update Sekarang" else "Posting Sekarang"
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
        binding.edtName.setText(dataItem.name)
        binding.tieDeskripsi.setText(dataItem.description)
        binding.pvImageView.load(this, dataItem.photo_url)
        tipeBarang = dataItem.type.lowercase()
        tipeKontak = dataItem.contact_type.lowercase()
        idCategory = dataItem.category_id.id
        logError("detail ${dataItem.photo_url}")
        when(dataItem.type.lowercase()) {
            "lost" -> {
                binding.rbLost.isChecked = true
                binding.rbFound.isChecked = false
            }
            "found" -> {
                binding.rbLost.isChecked = false
                binding.rbFound.isChecked = true
            }
            else -> {
                binding.rbLost.isChecked = false
                binding.rbFound.isChecked = false
            }
        }
        when(dataItem.contact_type.lowercase()) {
            "whatsapp" -> {
                binding.rbWhatsapp.isChecked = true
                binding.rbTelegram.isChecked = false
                binding.tilKontak.visible()
                binding.edtKontak.setText(dataItem.contact_value)

            }
            "telegram" -> {
                binding.rbWhatsapp.isChecked = false
                binding.rbTelegram.isChecked = true
                binding.tilKontak.visible()
                binding.edtKontak.setText(dataItem.contact_value)
            }
            else -> {
                binding.rbWhatsapp.isChecked = false
                binding.rbTelegram.isChecked = false
                binding.tilKontak.gone()
                binding.edtKontak.setText("")
            }
        }
        if (dataItem.name.isNotEmpty()) {
            val nama = binding.edtName.text.toString()
            val kontak = binding.edtKontak.text.toString()
            val deskripsi = binding.tieDeskripsi.text.toString()

            if (nama.isNotEmpty() && kontak.isNotEmpty() && deskripsi.isNotEmpty() &&
                idCategory > 0 && tipeBarang.isNotEmpty() && tipeKontak.isNotEmpty()) {
                binding.mbNext.apply {
                    enable()
                    setOnClickListener {
                        sendPosting()
                    }
                }
            } else {
                binding.mbNext.apply {
                    disable()
                    setOnClickListener(null)
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
                                if (it.data[index].id == dataItem.category_id.id) {
                                    chip.isChecked = true
                                    idCategory = it.data[index].id
                                }
                                chip.setOnCheckedChangeListener {  _, isChecked ->
                                    if (isChecked) {
                                        idCategory = it.data[index].id
                                        logError("kategori $idCategory")
                                    } else {
                                        idCategory = 0
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun sendPosting() {
        callToShare {
            it?.let {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val filenameExt = "${resources.getString(R.string.app_name)}${timeStamp}.jpg"
                saveImageFromUriToGallery(this@PostingActivity, it, filenameExt, 50) { imgFileName ->
                    getSavedImageUri(this@PostingActivity, imgFileName)?.let { imgUri ->
                        if (imgUri.isNotNull()) {
                            val nama = binding.edtName.text.toString()
                            val kontak = binding.edtKontak.text.toString()
                            val deskripsi = binding.tieDeskripsi.text.toString()

                            if (nama.isBlank() || kontak.isBlank() || deskripsi.isBlank() ||
                                idCategory == 0 || tipeBarang.isBlank() || tipeKontak.isBlank()) {
                                showSnackBar(this@PostingActivity, "Isi data dengan lengkap yaa")
                                return@saveImageFromUriToGallery
                            }

                            if (isEdit == true) {
                                val form = PostingUpdateForm(
                                    category_id = idCategory,
                                    owner_id = storeDataHelper.getDataUser().id,
                                    type = tipeBarang,
                                    name = nama,
                                    status = "open",
                                    description = deskripsi,
                                    contact_type = tipeKontak,
                                    contact_value = kontak,
                                    photo = getFilePathFromUri(this@PostingActivity, imgUri)!!
                                )
                                mainVM.updatePostingItem(storeDataHelper.getAuthToken(), dataItem.id.toString(), form).observe(this@PostingActivity) { responseBody ->
                                    handleApiCallback(
                                        this@PostingActivity,
                                        responseBody,
                                        true,
                                        object : RemoteCallback<String> {
                                            override fun do_callback(id: Int, t: String) {}
                                            override fun failed_callback(id: Int, t: String) {}
                                        }) { res, code ->
                                        if (code == RC().SUCCESS) {
                                            res?.let {
                                                showSnackBar(this@PostingActivity, "Berhasil update")
                                                onBackPressed()
                                            }
                                        } else {
                                            res?.let {
                                                dialogAlertOneButton(
                                                    this@PostingActivity,
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
                                return@saveImageFromUriToGallery
                            }

                            val form = PostingItemForm(
                                id = idCategory,
                                user_id = storeDataHelper.getDataUser().id,
                                type = tipeBarang,
                                name = nama,
                                description = deskripsi,
                                contact_type = tipeKontak,
                                contact_value = kontak,
                                photo_url = getFilePathFromUri(this@PostingActivity, imgUri)!!
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
                                                this@PostingActivity,
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
                        } else {
                            showSnackBar(this@PostingActivity, "Gambar tidak ditemukan")
                        }
                    }
                }
            }
        }
    }

    private fun callToShare(callback: (Uri?) -> Unit) {
        val visibleBitmap = getVisibleBitmapFromPhotoView(binding.pvImageView)
        val finalBitmap = createImage(visibleBitmap, binding.pvImageView.drawToBitmap())
        val uri = saveBitmapToCache(this@PostingActivity, finalBitmap, resources.getString(R.string.app_name))
        //logError("result uri $uri")
        callback.invoke(uri)
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