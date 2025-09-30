package com.fatkhun.core.utils

import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.text.Spanned
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.facebook.shimmer.ShimmerFrameLayout
import com.fatkhun.core.R
import com.fatkhun.core.databinding.WidgetCustomDialogBinding
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * @param act this
 * @param message error message
 * */
fun showSnackBar(act: Activity, message: String) {
    val views = act.findViewById<View>(android.R.id.content)
    val snackbar = Snackbar.make(views, message, Snackbar.LENGTH_LONG)
    snackbar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
    snackbar.show()
}

/**
 * @param act this
 * @param message error message
 */
fun showSnackBarTop(act: Activity, message: String) {
    val views = act.findViewById<View>(android.R.id.content)
    val snackbar = Snackbar.make(views, message, Snackbar.LENGTH_LONG)
    val layoutParams = FrameLayout.LayoutParams(snackbar.view.layoutParams)

    layoutParams.gravity = Gravity.TOP
    snackbar.view.setPadding(0, 10, 0, 0)
    snackbar.view.layoutParams = layoutParams
    snackbar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
    snackbar.show()
}

fun showToast(context: Context, message: String, length: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(context, message, length).show()

fun View.gone() {
    this.visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.enable() {
    this.isEnabled = true
}

fun View.disable() {
    this.isEnabled = false
}

fun ShimmerFrameLayout.stoped() {
    this.stopShimmer()
    this.visibility = View.GONE
}

fun ShimmerFrameLayout.showing() {
    this.startShimmer()
    this.visibility = View.VISIBLE
}

fun compatRegisterReceiver(
    context: Context, receiver: BroadcastReceiver, filter: IntentFilter, exported: Boolean
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.registerReceiver(
            receiver,
            filter,
            if (exported) Context.RECEIVER_EXPORTED else Context.RECEIVER_NOT_EXPORTED
        )
    } else {
        context.registerReceiver(receiver, filter)
    }
}

fun setCustomeTextHTML(html: String): Spanned {
    val result: Spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
    return result
}

fun Int.length() = when (this) {
    0 -> 1
    else -> Math.log10(Math.abs(toDouble())).toInt() + 1
}

fun regexOnlyNumber(str: String): String {
    return str.replace("[\\D]".toRegex(), "")
}

fun trimTrailingZero(value: String): String {
    return if (value.isNotEmpty()) {
        if (value.indexOf(".") < 0) {
            value
        } else {
            value.replace("0*$".toRegex(), "").replace("\\.$".toRegex(), "")
        }
    } else {
        value
    }
}

fun ImageView.loadCompress(context: Context, url: String?) {
    val cTheme = ContextThemeWrapper(context, R.style.Base_Theme_Etemu)
    val drawable = CircularProgressDrawable(cTheme)
    drawable.setColorSchemeColors(R.color.p_25, R.color.p_50, R.color.p_50)
    drawable.setColorFilter(
        ContextCompat.getColor(context, R.color.p_50),
        PorterDuff.Mode.SRC_ATOP
    )
    drawable.centerRadius = 30f
    drawable.strokeWidth = 5f
    drawable.start()

    // Handle null or empty URL
    if (url.isNullOrBlank()) {
        setImageResource(R.drawable.ic_placeholder_img_rect)
        return
    }

    Glide.with(this.context).load(url)
        .placeholder(drawable)
        .apply(RequestOptions.overrideOf(150))
        .error(R.drawable.ic_placeholder_img_rect)
        .timeout(30000)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .into(this)
}


fun ImageView.loadCircle(
    url: String?,
    drawPlaceholder: Int = R.drawable.ic_placeholder_img_rect
) {
    val cTheme = ContextThemeWrapper(this.context, R.style.Base_Theme_Etemu)
    val drawable = CircularProgressDrawable(cTheme)
    drawable.setColorSchemeColors(R.color.p_25, R.color.p_50, R.color.p_50)
    drawable.setColorFilter(
        ContextCompat.getColor(this.context, R.color.p_50),
        PorterDuff.Mode.SRC_ATOP
    )
    drawable.centerRadius = 30f
    drawable.strokeWidth = 5f
    drawable.start()

    // Handle null or empty URL
    if (url.isNullOrBlank()) {
        setImageResource(drawPlaceholder)
        return
    }

    Glide.with(this.context).load(url)
        .error(drawPlaceholder)
        .apply(
            RequestOptions.bitmapTransform(CircleCrop())
                .placeholder(drawable)
                .timeout(100000)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        )
        .into(object : CustomViewTarget<ImageView, Drawable>(this) {
            override fun onLoadFailed(errorDrawable: Drawable?) {
                setImageDrawable(errorDrawable)
            }

            override fun onResourceReady(
                resource: Drawable,
                transition: Transition<in Drawable>?
            ) {
                setImageDrawable(resource)
            }

            override fun onResourceCleared(placeholder: Drawable?) {

            }

        })
}

fun ImageView.loadRounded(
    url: String?, radius: Int,
    drawPlaceholder: Int,
    drawError: Int
) {
    // Handle null or empty URL
    if (url.isNullOrBlank()) {
        setImageResource(drawError)
        return
    }

    Glide.with(context).load(url)
        .error(drawError)
        .apply(
            RequestOptions.bitmapTransform(RoundedCorners(radius))
                .placeholder(drawPlaceholder)
                .timeout(100000)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
        )
        .into(object : CustomViewTarget<ImageView, Drawable>(this) {
            override fun onLoadFailed(errorDrawable: Drawable?) {
                setImageDrawable(errorDrawable)
            }

            override fun onResourceReady(
                resource: Drawable,
                transition: Transition<in Drawable>?
            ) {
                setImageDrawable(resource)
            }

            override fun onResourceCleared(placeholder: Drawable?) {

            }

        })
}

fun ImageView.load(
    context: Context, url: String?,
    drawPlaceholder: Int = R.drawable.ic_placeholder_img_rect,
    drawError: Int = R.drawable.ic_placeholder_img_rect,
    circleCrop: Boolean = false,
) {
    // Handle null or empty URL
    if (url.isNullOrBlank()) {
        setImageResource(drawError)
        return
    }

    val glideRequest = Glide.with(context).load(url)
        .placeholder(drawPlaceholder)
        .error(drawError)
        .timeout(30000)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

    if (circleCrop) {
        glideRequest.circleCrop()
    }

    glideRequest.into(object : CustomViewTarget<ImageView, Drawable>(this) {
        override fun onLoadFailed(errorDrawable: Drawable?) {
            setImageDrawable(errorDrawable ?: ContextCompat.getDrawable(context, drawError))
        }

        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
            setImageDrawable(resource)
        }

        override fun onResourceCleared(placeholder: Drawable?) {}
    })
}

fun ImageView.load(
    context: Context,
    url: String?,
    drawPlaceholder: Int = R.drawable.ic_placeholder_img_rect,
    drawError: Int = R.drawable.ic_placeholder_img_rect,
    circleCrop: Boolean = false,
    resourceReady: () -> Unit?
) {
    // Handle null or empty URL
    if (url.isNullOrBlank()) {
        setImageResource(drawError)
        return
    }

    val glideRequest = Glide.with(context).load(url)
        .placeholder(drawPlaceholder)
        .error(drawError)
        .timeout(30000)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

    if (circleCrop) {
        glideRequest.circleCrop()
    }

    glideRequest.into(object : CustomViewTarget<ImageView, Drawable>(this) {
        override fun onLoadFailed(errorDrawable: Drawable?) {
            setImageDrawable(errorDrawable ?: ContextCompat.getDrawable(context, drawError))
        }

        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
            setImageDrawable(resource)
            resourceReady.invoke()
        }

        override fun onResourceCleared(placeholder: Drawable?) {

        }

    })
}

fun ImageView.loadDrawable(draw: Drawable) {
    val cTheme = ContextThemeWrapper(this.context, R.style.Base_Theme_Etemu)
    val drawable = CircularProgressDrawable(cTheme)
    drawable.setColorSchemeColors(R.color.p_25, R.color.p_50, R.color.p_50)
    drawable.setColorFilter(
        ContextCompat.getColor(this.context, R.color.p_50),
        PorterDuff.Mode.SRC_ATOP
    )
    drawable.centerRadius = 30f
    drawable.strokeWidth = 5f
    drawable.start()

    Glide.with(this.context).load(draw)
        .placeholder(drawable)
        .error(R.drawable.ic_placeholder_img_rect)
        .timeout(30000)
        .into(this)
}

fun ImageView.loadCircleDrawable(draw: Drawable) {
    val cTheme = ContextThemeWrapper(this.context, R.style.Base_Theme_Etemu)
    val drawable = CircularProgressDrawable(cTheme)
    drawable.setColorSchemeColors(R.color.p_25, R.color.p_50, R.color.p_50)
    drawable.setColorFilter(
        ContextCompat.getColor(this.context, R.color.p_50),
        PorterDuff.Mode.SRC_ATOP
    )
    drawable.centerRadius = 30f
    drawable.strokeWidth = 5f
    drawable.start()

    Glide.with(this.context).load(draw)
        .placeholder(drawable)
        .error(R.drawable.ic_placeholder_img_rect)
        .timeout(30000)
        .circleCrop()
        .into(this)
}

/**
 * Hides the soft keyboard
 */
fun hideSoftKeyboard(context: Activity) {
    if (context.currentFocus != null) {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(context.currentFocus!!.windowToken, 0)
    }
}

/**
 * Shows the soft keyboard
 */
fun showSoftKeyboard(context: Activity, view: View) {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    view.requestFocus()
    inputMethodManager.showSoftInput(view, 0)
}

fun Context.changeLocale(language: String): Context {
    val locale = Locale(language)
    Locale.setDefault(locale)
    val config = this.resources.configuration
    config.setLocale(locale)
    return createConfigurationContext(config)
}

fun EditText.afterTextChangedDebounce(
    delayMillis: Long,
    beforeAction: () -> Unit = {},
    input: (String) -> Unit
) {
    val textFlow = MutableStateFlow("")
    val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Collect changes with debounce
    scope.launch {
        textFlow
            .debounce(delayMillis)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .collect { input(it) }
    }

    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            editable?.toString()?.let { newText ->
                if (newText.isNotBlank()) {
                    scope.launch { textFlow.emit(newText) }
                }
            }
        }

        override fun beforeTextChanged(cs: CharSequence?, start: Int, count: Int, after: Int) {
            beforeAction()
        }

        override fun onTextChanged(cs: CharSequence?, start: Int, before: Int, count: Int) {}
    })

    // Clean up when view is detached
    this.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {}
        override fun onViewDetachedFromWindow(v: View) {
            scope.cancel()
            this@afterTextChangedDebounce.removeOnAttachStateChangeListener(this)
        }
    })
}

fun isEmailValid(input: String): Boolean {
    return !TextUtils.isEmpty(input) && android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()
}

fun AppCompatTextView.setTextOrHide(value: String? = null) {
    if (!value.isNullOrBlank()) {
        text = value
        isVisible = true
    } else {
        isVisible = false
    }
}

interface AlertDialogInterface {
    fun onPositiveButtonClicked()
    fun onNegativeButtonClicked()
}

fun showCustomDialog(
    context: Context, title: String, message: String,
    positiveLabel: String = "OK",
    negativeLabel: String = "",
    isCancelable: Boolean = true,
    listener: AlertDialogInterface? = null
) {
    try {
        val binding = WidgetCustomDialogBinding.inflate(
            LayoutInflater.from(context), null, false
        )
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(binding.root)
        dialog.setCancelable(isCancelable)

        binding.layoutDialog.layoutParams.width =
            context.resources.displayMetrics.widthPixels - context.resources.getDimensionPixelSize(
                com.intuit.sdp.R.dimen._36sdp
            )
        binding.layoutDialog.requestLayout()
        binding.tvDialogTitle.setTextOrHide(title)
        binding.tvDialogMessage.setTextOrHide(message)

        binding.btnDialogNegative.apply {
            text = setCustomeTextHTML(negativeLabel)
            isVisible = negativeLabel.isNotEmpty()
            setOnClickListener {
                try {
                    listener?.onNegativeButtonClicked()
                } catch (e: Exception) {
                }
                try {
                    dialog.dismiss()
                } catch (e: Exception) {
                }
            }
        }

        binding.btnDialogPositive.apply {
            text = setCustomeTextHTML(positiveLabel)
            isVisible = positiveLabel.isNotEmpty()
            setOnClickListener {
                try {
                    listener?.onPositiveButtonClicked()
                } catch (e: Exception) {
                }
                try {
                    dialog.dismiss()
                } catch (e: Exception) {
                }
            }
        }
        try {
            dialog.show()
        } catch (e: Exception) {
        }
    } catch (e: Exception) {}
}

fun dialogAlertOneButton(
    context: Context,
    resId: Int,
    title: String,
    subtitle: String,
    textButton: String,
    cancelable: Boolean = true,
    callback: (BottomSheetDialog) -> Unit
) {
    val activity = context as? Activity ?: return
    if (activity.isFinishing || activity.isDestroyed) return

    BottomSheetDialog(context, R.style.BottomSheetDialogNoBorder).apply {
        setCancelable(cancelable)
        setContentView(layoutInflater.inflate(R.layout.dialog_popup_alert_one_button, null))
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        val iv_alert = findViewById<ImageView>(R.id.iv_alert)
        val tv_title_alert = findViewById<TextView>(R.id.tv_title_alert)
        val tv_subtitle_alert = findViewById<TextView>(R.id.tv_subtitle_alert)
        val mb_confirm_alert = findViewById<MaterialButton>(R.id.mb_confirm_alert)

        iv_alert?.setImageResource(resId)
        tv_title_alert?.text = setCustomeTextHTML(title)
        tv_subtitle_alert?.text = setCustomeTextHTML(subtitle)
        mb_confirm_alert?.text = textButton
        mb_confirm_alert?.setOnClickListener {
            callback.invoke(this@apply)
        }
        setOnCancelListener {
            it.dismiss()
        }
        show()
    }
}

// Function to rotate a Bitmap by a given angle
fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}

fun getScaledBitmap(filePath: String, reqWidth: Int, reqHeight: Int): Bitmap {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeFile(filePath, options)

    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
    options.inJustDecodeBounds = false

    return BitmapFactory.decodeFile(filePath, options)
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    // Check if height and width are greater than 0 to avoid divide by zero
    if (height > 0 && width > 0) {
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
    } else {
        // Log or handle the case where height or width is 0
        // e.g., log an error, use a default value, or throw an exception
        Log.e("TAG", "calculateInSampleSize: calculateInSampleSize Invalid image dimensions: width=$width, height=$height")
    }

    return inSampleSize
}

fun rotateImageIfRequired(activity: Activity, img: Bitmap, selectedImage: Uri): Bitmap {
    val inputStream = activity.contentResolver.openInputStream(selectedImage)
    val exifInterface = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        ExifInterface(inputStream!!)
    } else {
        ExifInterface(selectedImage.path!!)
    }
    val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(img, 90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(img, 180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(img, 270f)
        else -> img
    }
}

fun getVisibleBitmapFromPhotoView(photoView: PhotoView): Bitmap {
    val matrix = photoView.imageMatrix
    val drawable = photoView.drawable ?: return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    val values = FloatArray(9)
    matrix.getValues(values)

    val scaleX = values[Matrix.MSCALE_X]
    val scaleY = values[Matrix.MSCALE_Y]
    val translateX = values[Matrix.MTRANS_X]
    val translateY = values[Matrix.MTRANS_Y]

    val originalBitmap = (drawable as BitmapDrawable).bitmap
    val originalWidth = drawable.intrinsicWidth
    val originalHeight = drawable.intrinsicHeight

    // Calculate the visible area in the original bitmap
    val visibleLeft = (-translateX / scaleX).toInt()
    val visibleTop = (-translateY / scaleY).toInt()
    val visibleWidth = (photoView.width / scaleX).toInt()
    val visibleHeight = (photoView.height / scaleY).toInt()

    // Ensure the visible area doesn't go beyond the bitmap dimensions
    val clampedLeft = visibleLeft.coerceIn(0, originalWidth)
    val clampedTop = visibleTop.coerceIn(0, originalHeight)
    val clampedWidth = visibleWidth.coerceAtMost(originalWidth - clampedLeft).coerceAtLeast(1)
    val clampedHeight = visibleHeight.coerceAtMost(originalHeight - clampedTop).coerceAtLeast(1)

    // Create the bitmap of the visible portion
    return Bitmap.createBitmap(originalBitmap, clampedLeft, clampedTop, clampedWidth, clampedHeight)
}

fun resizeBitmapToFitFrame(bitmap: Bitmap, frameWidth: Int, frameHeight: Int): Bitmap {
    val scaleFactor =
        (frameWidth.toFloat() / bitmap.width).coerceAtMost(frameHeight.toFloat() / bitmap.height)

    val newWidth = (bitmap.width * scaleFactor).toInt()
    val newHeight = (bitmap.height * scaleFactor).toInt()

    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}

// Function to combine the image and frame
fun combinePhotoWithFrame(resizedPhoto: Bitmap, frameBitmap: Bitmap): Bitmap {
    val bitmapConfig: Bitmap.Config = frameBitmap.config ?: Bitmap.Config.ARGB_8888
    val combinedBitmap = Bitmap.createBitmap(frameBitmap.width, frameBitmap.height, bitmapConfig)
    val canvas = Canvas(combinedBitmap)

    // Calculate the position to center the photo on the frame
    val left = (frameBitmap.width - resizedPhoto.width) / 2f
    val top = (frameBitmap.height - resizedPhoto.height) / 2f

    // Draw the photo first
    canvas.drawBitmap(resizedPhoto, left, top, null)

    // Draw the frame on top
    canvas.drawBitmap(frameBitmap, 0f, 0f, null)

    return combinedBitmap
}

// Function to save bitmap to uri
fun saveBitmapToCache(context: Context, bitmap: Bitmap, appName: String): Uri? {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val filename = "${appName}_post_image_${timeStamp}"
    val cachePath = File(context.cacheDir, "images")
    if (!cachePath.exists()) {
        cachePath.mkdirs() // Create cache directory if it doesn't exist
    }
    val file = File(cachePath, "${filename}.jpg")
    try {
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.flush()
        stream.close()
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
    return FileProvider.getUriForFile(
        context,
        PostingFileProvider.getProviderName(context),
        file
    )
}

fun saveImageFromUriToGallery(context: Context, imageUri: Uri, imageFileName: String, quality: Int, fileName: (String) -> Unit) {
    val resolver = context.contentResolver
    val fos: OutputStream?

    // Step 1: Decode the image from URI into a Bitmap
    val bitmap = resolver.openInputStream(imageUri)?.use {
        BitmapFactory.decodeStream(it)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // For Android 10 and above, save the image to the MediaStore
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val outputUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        fos = resolver.openOutputStream(outputUri!!)
    } else {
        // For Android 9 and below, save the image to the Pictures directory
        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val imageFile = File(imagesDir, imageFileName)
        fos = FileOutputStream(imageFile)
    }

    // Step 2: Compress the Bitmap to reduce quality and Step 3: Save it
    try {
        fos?.use { output ->
            bitmap?.compress(Bitmap.CompressFormat.JPEG, quality, output)
        }
        fileName.invoke(imageFileName)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        fos?.close()
    }

    // Optional: Notify the MediaStore to scan the new file so it shows up in the gallery immediately
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        context.sendBroadcast(
            Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), imageFileName)))
        )
    }
}

fun getSavedImageUri(context: Context, imageFileName: String): Uri? {
    var uri: Uri? = null
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    val projection = arrayOf(MediaStore.Images.Media._ID)
    val selection = "${MediaStore.Images.Media.DISPLAY_NAME} = ?"
    val selectionArgs = arrayOf(imageFileName)

    context.contentResolver.query(
        collection,
        projection,
        selection,
        selectionArgs,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
            uri = Uri.withAppendedPath(collection, id.toString())
        } else {
            uri = null
        }
    }

    // If running on Android 9 or below and URI is null, try to rescan the media
    if (uri == null && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        val imageFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), imageFileName)
        if (imageFile.exists()) {
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)))
            uri = Uri.fromFile(imageFile)
        }
    }

    return uri
}

fun getFilePathFromUri(context: Context, uri: Uri): String? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // For Android 10 and above, copy the file to a temporary location and return the path
        copyUriToFile(context, uri)
    } else {
        // For Android 9 and below, retrieve the file path directly
        getFilePathFromUriBelowAPI29(context, uri)
    }
}

// For Android 10 and above: copy the file to a temporary location and return the path
private fun copyUriToFile(context: Context, uri: Uri): String? {
    val fileName = getFileName(context, uri)
    val tempFile = File(context.cacheDir, fileName)
    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                copyStream(inputStream, outputStream)
            }
        }
        return tempFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

// For Android 9 and below: retrieve the file path directly
private fun getFilePathFromUriBelowAPI29(context: Context, uri: Uri): String? {
    var filePath: String? = null
    val projection = arrayOf(MediaStore.Images.Media.DATA)

    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            filePath = cursor.getString(columnIndex)
        }
    }

    // Fallback mechanism
    if (filePath != null) {
        val file = File(filePath)
        if (file.exists()) {
            return filePath
        }
    }

    // Fallback to retrieving path from file Uri directly
    val alternativePath = getRealPathFromURI(context, uri)
    if (alternativePath != null && File(alternativePath).exists()) {
        return alternativePath
    }

    return null
}

fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
    var result: String? = null
    val proj = arrayOf(MediaStore.Images.Media.DATA)
    val cursor: Cursor? = context.contentResolver.query(contentUri, proj, null, null, null)
    if (cursor != null) {
        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            result = cursor.getString(columnIndex)
        }
        cursor.close()
    }
    if (result == null) {
        result = contentUri.path
    }
    return result
}

// Utility to copy the content of an InputStream to an OutputStream
private fun copyStream(input: InputStream, output: FileOutputStream) {
    val buffer = ByteArray(1024)
    var length: Int
    while (input.read(buffer).also { length = it } > 0) {
        output.write(buffer, 0, length)
    }
}

// Utility to get file name from URI
private fun getFileName(context: Context, uri: Uri): String {
    var name = "temp_file"
    val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            name = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
        }
    }
    return name
}