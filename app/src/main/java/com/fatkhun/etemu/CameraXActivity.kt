package com.fatkhun.etemu

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.load.ImageHeaderParser
import com.fatkhun.core.utils.logError
import com.fatkhun.core.utils.showSnackBar
import com.fatkhun.core.utils.showToast
import com.fatkhun.etemu.databinding.ActivityCameraXactivityBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraXActivity : AppCompatActivity() {

    companion object {
        const val KEY_RESULT_FILE = "KEY_RESULT_FILE"
        const val IS_FRONT = "is_front"
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    private lateinit var binding: ActivityCameraXactivityBinding

    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var currentCameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var lensFacing = CameraSelector.LENS_FACING_BACK

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private var currentCamera: Camera? = null

    private val orientationEventListener by lazy {
        object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return

                val rotation = when (orientation) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }

                imageCapture?.targetRotation = rotation
                updateFlashlightAvailability()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCameraXactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViews()
        initializeCamera()

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun setupViews() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.cbFlashlight.setOnCheckedChangeListener { _, isChecked ->
            currentCamera?.cameraControl?.enableTorch(isChecked)
        }

        binding.btnCapture.setOnClickListener {
            takePhoto()
        }

        binding.ivChangeCamera.setOnClickListener {
            switchCamera()
        }
    }

    private fun initializeCamera() {
        val isFront = intent.getBooleanExtra(IS_FRONT, false)
        currentCameraSelector = if (isFront) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        lensFacing = if (isFront) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK

        startCamera()
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Ensure that the preview surface is properly configured and available
        if (binding.viewFinder.surfaceProvider == null) {
            logError("Surface provider is not available.")
            return
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputDirectory).build()

        // Disable capture button to prevent multiple captures
        binding.btnCapture.isEnabled = false

        // Set up image capture listener
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    runOnUiThread {
                        binding.btnCapture.isEnabled = true
                        showSnackBar(
                            this@CameraXActivity,
                            "Photo capture failed: ${exc.message}"
                        )
                    }
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(outputDirectory)
                    logError("Photo capture succeeded: $savedUri")
                    val cleanPath = if (savedUri.toString().startsWith("file://")) {
                        savedUri.toString().substring(7)
                    } else {
                        savedUri
                    }

                    val resultIntent = Intent().apply {
                        putExtra(KEY_RESULT_FILE, cleanPath.toString())
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            })
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            try {

                // Unbind use cases before rebinding
                cameraProvider?.unbindAll()

                // Select camera based on current selector
                val camera = cameraProvider?.bindToLifecycle(
                    this, currentCameraSelector, preview, imageCapture
                )

                currentCamera = camera
                updateFlashlightAvailability()

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
                showSnackBar(
                    this,
                    "Camera initialization failed"
                )
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun switchCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }

        currentCameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        startCamera()
    }

    private fun updateFlashlightAvailability() {
        val hasFlash = currentCamera?.cameraInfo?.hasFlashUnit() == true
        binding.cbFlashlight.isEnabled = hasFlash
        if (!hasFlash) {
            binding.cbFlashlight.isChecked = false
        }
    }

    private fun getOutputDirectory(): File {
        val privateTempDir = File(cacheDir, resources.getString(R.string.app_name))
        if (!privateTempDir.exists()) privateTempDir.mkdirs()

        return File.createTempFile(
            "${resources.getString(R.string.app_name).lowercase()}_tmp_" +
                    "${System.currentTimeMillis()}",
            ".jpg",
            privateTempDir
        )
    }

    override fun onStart() {
        super.onStart()
        orientationEventListener.enable()
    }

    override fun onStop() {
        super.onStop()
        orientationEventListener.disable()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        // Clean up camera provider
        cameraProvider?.unbindAll()
    }
}