package com.fatkhun.core.helper

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import com.fatkhun.core.model.ProviderConnection
import com.fatkhun.core.utils.compatRegisterReceiver
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class PermissionHelper(val context: Context) : LiveData<ProviderConnection>() {
    private var firstConnect = 0

    companion object {
        const val REQUEST_CODE_GPS_SETTINGS = 1000
        const val REQUEST_SENDER_CONTACT_SETTINGS = 1001
        const val REQUEST_RECIPIENT_CONTACT_SETTINGS = 1002
        const val NetworkMode = 2
        const val GPSMode = 1

        const val MESSAGE_NEED_ACCESS_STORAGE = "Izinkan aplikasi mengakses " +
                "penyimpanan perangkat untuk menggunakan fitur ini."
        const val MESSAGE_NEED_ACCESS_CAMERA = "Izinkan aplikasi mengakses kamera, " +
                "perekam suara, dan penyimpanan perangkat untuk menggunakan fitur ini."
        const val MESSAGE_NEED_ACCESS_LOCATION = "Izinkan aplikasi mengakses lokasi Anda " +
                "untuk menggunakan fitur ini."
        const val MESSAGE_NEED_CAMERA = "Izinkan aplikasi mengakses kamera Anda " +
                "untuk menggunakan fitur ini."
        const val MESSAGE_NEDD_READ_CONTACT = "Izinkan aplikasi mengakses kontak Anda " +
                "untuk menggunakan fitur ini."
        const val MESSAGE_NEED_NOTIFICATION = "Izinkan aplikasi mengirim notifikasi ke Anda."

        val CAMERA_REQUIRED_PERMISSIONS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO
                )
            } else {
                arrayOf(
                    Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }

        val CAMERA_STORAGE_REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )

        val IMAGE_VIDEO_REQUIRED_PERMISSIONS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO
                )
            } else {
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }

        val DOWNLOAD_REQUIRED_PERMISSIONS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            } else {
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }

        fun allPermissionsGranted(context: Context, requiredPermission: Array<String>) =
            requiredPermission.all {
                ContextCompat.checkSelfPermission(
                    context, it
                ) == PackageManager.PERMISSION_GRANTED
            }
    }

    override fun onActive() {
        super.onActive()
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        compatRegisterReceiver(context, providerReceiver, filter, true)
    }

    override fun onInactive() {
        super.onInactive()
        context.unregisterReceiver(providerReceiver)
    }

    private val providerReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            try {
                val lm = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val isGPSConnected = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val isNetConnected = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                val locationGPSMode = Settings.Secure.isLocationProviderEnabled(
                    context.contentResolver,
                    LocationManager.GPS_PROVIDER
                )
                val locationNetMode = Settings.Secure.isLocationProviderEnabled(
                    context.contentResolver,
                    LocationManager.NETWORK_PROVIDER
                )
                Log.e(
                    "TAG",
                    "onReceiveGPS: $isGPSConnected | $isNetConnected | $locationGPSMode | $locationNetMode"
                )
                if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
                    if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.action)) {
                        firstConnect += 1
                        if (firstConnect > 1) {
                            if (isGPSConnected && isNetConnected) {
                                postValue(ProviderConnection(GPSMode, isGPSConnected))
                                firstConnect = 0
                            } else {
                                postValue(ProviderConnection(GPSMode, false))
                                firstConnect = 0
                            }
                        }
                    }
                } else {
                    if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.action)) {
                        firstConnect += 1
                        if (firstConnect > 1) {
                            if (locationGPSMode && locationNetMode) {
                                postValue(ProviderConnection(GPSMode, locationGPSMode))
                                firstConnect = 0
                            } else {
                                postValue(ProviderConnection(GPSMode, false))
                                firstConnect = 0
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun requestCameraPermission(callback: (Boolean) -> Unit) {
        Dexter.withContext(context)
            .withPermissions(
                Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    Log.d(
                        "TAG",
                        "onPermissionsCameraChecked: " + report.isAnyPermissionPermanentlyDenied + " " + report.areAllPermissionsGranted()
                    )
                    if (report.areAllPermissionsGranted()) {
                        callback.invoke(true)
                    } else if (report.isAnyPermissionPermanentlyDenied) {
                        callback.invoke(false)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>, token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            })
            .onSameThread()
            .check()
    }

    fun requestDownloadPermission(callback: (Boolean) -> Unit) {
        Dexter.withContext(context)
            .withPermissions(
                DOWNLOAD_REQUIRED_PERMISSIONS.toList()
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    Log.d(
                        "TAG",
                        "onPermissionsCameraChecked: " + report.isAnyPermissionPermanentlyDenied + " " + report.areAllPermissionsGranted()
                    )
                    if (report.areAllPermissionsGranted()) {
                        callback.invoke(true)
                    } else if (report.isAnyPermissionPermanentlyDenied) {
                        callback.invoke(false)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>, token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            })
            .onSameThread()
            .check()
    }

    fun openAppSetting() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", "com.fatkhun.etemu", null)
        intent.data = uri
        startActivity(context, intent, null)
    }
}