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
import com.fatkhun.etemu.databinding.ActivityCameraXactivityBinding
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraXActivity : AppCompatActivity() {

    companion object {
        const val KEY_RESULT_FILE = "KEY_RESULT_FILE"
        const val IS_FRONT = "is_front"
        private const val TAG = "CameraXBasic"
    }

    private lateinit var binding: ActivityCameraXactivityBinding

    private var imageCapture: ImageCapture? = null
    private var currentCamera: Camera? = null
    private var backCamera = true

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private val orientationEventListener by lazy {
        object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ImageHeaderParser.UNKNOWN_ORIENTATION) {
                    return
                }

                val rotation = when (orientation) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }

                imageCapture?.targetRotation = rotation
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
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
        val isFront = intent.getBooleanExtra(IS_FRONT, false)
        startCamera(if (isFront) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA)

        binding.cbFlashlight.setOnCheckedChangeListener { _, isChecked ->
            currentCamera?.cameraControl?.enableTorch(isChecked)
        }
        binding.btnCapture.setOnClickListener { takePhoto() }

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputDirectory).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(outputDirectory)
                    Log.d(TAG, "Photo capture succeeded: $savedUri")

                    val resultIntent = Intent().apply {
                        putExtra(KEY_RESULT_FILE, savedUri.path)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            })
    }

    private fun startCamera(cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                currentCamera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

                backCamera = cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA

                binding.ivChangeCamera.setOnClickListener {
                    if (backCamera) {
                        startCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                    } else {
                        startCamera()
                    }
                }

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
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
    }

}