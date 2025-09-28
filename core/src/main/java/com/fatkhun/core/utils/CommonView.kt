package com.fatkhun.core.utils

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.widget.CircularProgressDrawable
import android.text.Editable
import android.text.Spanned
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.fatkhun.core.R
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