package com.fatkhun.core.utils

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toolbar
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import java.lang.ref.WeakReference

object StatusBar {
    private const val TAG_FAKE_STATUS_BAR_VIEW = "statusBarView"
    private const val TAG_MARGIN_ADDED = "marginAdded"

    // customize as per your requirement ie. statusBarColor instead of drawable, view instead of activity.

    fun Activity.applyEdgeToEdgeInsets(useTop: Boolean = true, useBottom: Boolean = true) {
        // Add this condition if you only want to support edge to edge in Android 15+ devices else remove this.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val view = findViewById<View>(android.R.id.content)
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
                val bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime()
                )
                WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
                WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = true
                v.updatePadding(
                    left = bars.left,
                    top =  if (useTop) bars.top else 0,
                    right = bars.right,
                    bottom = if (useBottom) bars.bottom else 0,
                )
                windowInsets
            }
            ViewCompat.requestApplyInsets(view)
        }
    }

    //Get alpha color
    fun calculateStatusBarColor(color: Int, alpha: Int): Int {
        val a = 1 - alpha / 255f
        var red = color shr 16 and 0xff
        var green = color shr 8 and 0xff
        var blue = color and 0xff
        red = (red * a + 0.5).toInt()
        green = (green * a + 0.5).toInt()
        blue = (blue * a + 0.5).toInt()
        return 0xff shl 24 or (red shl 16) or (green shl 8) or blue
    }

    /**
     * set statusBarColor
     * @param statusColor color
     * @param alpha       0 - 255
     */
    fun setStatusBarColor(activity: Activity, @ColorInt statusColor: Int, alpha: Int) {
        setStatusBarColor(activity, calculateStatusBarColor(statusColor, alpha))
    }

    fun setStatusBarColor(activity: Activity, @ColorInt statusColor: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStatusBarColorLollipop(activity, statusColor)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setStatusBarColorKitkat(activity, statusColor)
        }
    }

    fun translucentStatusBar(activity: Activity) {
        translucentStatusBar(activity, false)
    }

    /**
     * change to full screen mode
     * @param hideStatusBarBackground hide status bar alpha Background when SDK > 21, true if hide it
     */
    fun translucentStatusBar(activity: Activity, hideStatusBarBackground: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            translucentStatusBarLollipop(activity, hideStatusBarBackground)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            translucentStatusBarKitkat(activity)
        }
    }

    fun setStatusBarColorForCollapsingToolbar(
        activity: Activity,
        appBarLayout: AppBarLayout,
        collapsingToolbarLayout: CollapsingToolbarLayout,
        toolbar: Toolbar,
        @ColorInt statusColor: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStatusBarColorForCollapsingToolbarLollipop(
                activity,
                appBarLayout,
                collapsingToolbarLayout,
                toolbar,
                statusColor
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setStatusBarColorForCollapsingToolbarKitkat(
                activity,
                appBarLayout,
                collapsingToolbarLayout,
                toolbar,
                statusColor
            )
        }
    }

    fun changeToLightStatusBar(activity: Activity, statusColor: Int, alpha: Int) {
        setStatusBarColor(activity, statusColor, alpha)
        changeToLightStatusBar(activity)
    }

    fun changeToLightStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }
        if (activity == null) {
            return
        }
        val window = activity.window ?: return
        val decorView = window.decorView ?: return
        decorView.systemUiVisibility =
            decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    fun cancelLightStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }
        if (activity == null) {
            return
        }
        val window = activity.window ?: return
        val decorView = window.decorView ?: return
        decorView.systemUiVisibility =
            decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
    }


    /// compat kitkat
    /// compat kitkat
    /**
     * return statusBar's Height in pixels
     */
    private fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resId > 0) {
            result = context.resources.getDimensionPixelOffset(resId)
        }
        return result
    }

    /**
     * 1. Add fake statusBarView.
     * 2. set tag to statusBarView.
     */
    private fun addFakeStatusBarView(
        activity: Activity,
        statusBarColor: Int,
        statusBarHeight: Int
    ): View {
        val window = activity.window
        val mDecorView = window.decorView as ViewGroup
        val mStatusBarView = View(activity)
        val layoutParams =
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight)
        layoutParams.gravity = Gravity.TOP
        mStatusBarView.layoutParams = layoutParams
        mStatusBarView.setBackgroundColor(statusBarColor)
        mStatusBarView.tag = TAG_FAKE_STATUS_BAR_VIEW
        mDecorView.addView(mStatusBarView)
        return mStatusBarView
    }

    /**
     * use reserved order to remove is more quickly.
     */
    private fun removeFakeStatusBarViewIfExist(activity: Activity) {
        val window = activity.window
        val mDecorView = window.decorView as ViewGroup
        val fakeView = mDecorView.findViewWithTag<View>(TAG_FAKE_STATUS_BAR_VIEW)
        if (fakeView != null) {
            mDecorView.removeView(fakeView)
        }
    }

    /**
     * add marginTop to simulate set FitsSystemWindow true
     */
    private fun addMarginTopToContentChild(mContentChild: View?, statusBarHeight: Int) {
        if (mContentChild == null) {
            return
        }
        if (TAG_MARGIN_ADDED != mContentChild.tag) {
            val lp = mContentChild.layoutParams as FrameLayout.LayoutParams
            lp.topMargin += statusBarHeight
            mContentChild.layoutParams = lp
            mContentChild.tag = TAG_MARGIN_ADDED
        }
    }

    /**
     * remove marginTop to simulate set FitsSystemWindow false
     */
    private fun removeMarginTopOfContentChild(mContentChild: View?, statusBarHeight: Int) {
        if (mContentChild == null) {
            return
        }
        if (TAG_MARGIN_ADDED == mContentChild.tag) {
            val lp = mContentChild.layoutParams as FrameLayout.LayoutParams
            lp.topMargin -= statusBarHeight
            mContentChild.layoutParams = lp
            mContentChild.tag = null
        }
    }

    /**
     * set StatusBarColor
     *
     * 1. set Window Flag : WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
     * 2. removeFakeStatusBarViewIfExist
     * 3. addFakeStatusBarView
     * 4. addMarginTopToContentChild
     * 5. cancel ContentChild's fitsSystemWindow
     */
    fun setStatusBarColorKitkat(activity: Activity, statusColor: Int) {
        val window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        val mContentView = window.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
        val mContentChild = mContentView.getChildAt(0)
        val statusBarHeight = getStatusBarHeight(activity)
        removeFakeStatusBarViewIfExist(activity)
        addFakeStatusBarView(activity, statusColor, statusBarHeight)
        addMarginTopToContentChild(mContentChild, statusBarHeight)
        if (mContentChild != null) {
            ViewCompat.setFitsSystemWindows(mContentChild, false)
        }
    }

    /**
     * translucentStatusBar
     *
     * 1. set Window Flag : WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
     * 2. removeFakeStatusBarViewIfExist
     * 3. removeMarginTopOfContentChild
     * 4. cancel ContentChild's fitsSystemWindow
     */
    fun translucentStatusBarKitkat(activity: Activity) {
        val window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        val mContentView = activity.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
        val mContentChild = mContentView.getChildAt(0)
        removeFakeStatusBarViewIfExist(activity)
        removeMarginTopOfContentChild(mContentChild, getStatusBarHeight(activity))
        if (mContentChild != null) {
            ViewCompat.setFitsSystemWindows(mContentChild, false)
        }
    }

    /**
     * compat for CollapsingToolbarLayout
     *
     * 1. set Window Flag : WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
     * 2. set FitsSystemWindows for views.
     * 3. add Toolbar's height, let it layout from top, then add paddingTop to layout normal.
     * 4. removeFakeStatusBarViewIfExist
     * 5. removeMarginTopOfContentChild
     * 6. add OnOffsetChangedListener to change statusBarView's alpha
     */
    fun setStatusBarColorForCollapsingToolbarKitkat(
        activity: Activity,
        appBarLayout: AppBarLayout,
        collapsingToolbarLayout: CollapsingToolbarLayout,
        toolbar: Toolbar,
        statusColor: Int
    ) {
        val window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        val mContentView = window.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
        val mContentChild = mContentView.getChildAt(0)
        mContentChild.fitsSystemWindows = false
        (appBarLayout.parent as View).fitsSystemWindows = false
        appBarLayout.fitsSystemWindows = false
        collapsingToolbarLayout.fitsSystemWindows = false
        collapsingToolbarLayout.getChildAt(0).fitsSystemWindows = false
        toolbar.fitsSystemWindows = false
        if (toolbar.tag == null) {
            val lp = toolbar.layoutParams as CollapsingToolbarLayout.LayoutParams
            val statusBarHeight = getStatusBarHeight(activity)
            lp.height += statusBarHeight
            toolbar.layoutParams = lp
            toolbar.setPadding(
                toolbar.paddingLeft,
                toolbar.paddingTop + statusBarHeight,
                toolbar.paddingRight,
                toolbar.paddingBottom
            )
            toolbar.tag = true
        }
        val statusBarHeight = getStatusBarHeight(activity)
        removeFakeStatusBarViewIfExist(activity)
        removeMarginTopOfContentChild(mContentChild, statusBarHeight)
        val statusView = addFakeStatusBarView(activity, statusColor, statusBarHeight)
        val behavior = (appBarLayout.layoutParams as CoordinatorLayout.LayoutParams).behavior
        if (behavior != null && behavior is AppBarLayout.Behavior) {
            val verticalOffset = behavior.topAndBottomOffset
            if (Math.abs(verticalOffset) > appBarLayout.height - collapsingToolbarLayout.scrimVisibleHeightTrigger) {
                statusView.alpha = 1f
            } else {
                statusView.alpha = 0f
            }
        } else {
            statusView.alpha = 0f
        }
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout1: AppBarLayout, verticalOffset: Int ->
            if (Math.abs(verticalOffset) > appBarLayout1.height - collapsingToolbarLayout.scrimVisibleHeightTrigger) {
                if (statusView.alpha == 0f) {
                    statusView.animate().cancel()
                    statusView.animate().alpha(1f)
                        .setDuration(collapsingToolbarLayout.scrimAnimationDuration).start()
                }
            } else {
                if (statusView.alpha == 1f) {
                    statusView.animate().cancel()
                    statusView.animate().alpha(0f)
                        .setDuration(collapsingToolbarLayout.scrimAnimationDuration).start()
                }
            }
        })
    }

    /// end kitkat

    /// lollipop

    /// end kitkat
    /// lollipop
    /**
     * return statusBar's Height in pixels
     */
    private fun getStatusBarHeightLollipop(context: Context): Int {
        var result = 0
        val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resId > 0) {
            result = context.resources.getDimensionPixelOffset(resId)
        }
        return result
    }

    /**
     * set StatusBarColor
     *
     * 1. set Flags to call setStatusBarColor
     * 2. call setSystemUiVisibility to clear translucentStatusBar's Flag.
     * 3. set FitsSystemWindows to false
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun setStatusBarColorLollipop(activity: Activity, statusColor: Int) {
        val window = activity.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = statusColor
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        val mContentView = window.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
        val mChildView = mContentView.getChildAt(0)
        if (mChildView != null) {
            ViewCompat.setFitsSystemWindows(mChildView, false)
            ViewCompat.requestApplyInsets(mChildView)
        }
    }

    /**
     * translucentStatusBar(full-screen)
     *
     * 1. set Flags to full-screen
     * 2. set FitsSystemWindows to false
     *
     * @param hideStatusBarBackground hide statusBar's shadow
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun translucentStatusBarLollipop(activity: Activity, hideStatusBarBackground: Boolean) {
        val window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (hideStatusBarBackground) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.TRANSPARENT
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
        val mContentView = window.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
        val mChildView = mContentView.getChildAt(0)
        if (mChildView != null) {
            ViewCompat.setFitsSystemWindows(mChildView, false)
            ViewCompat.requestApplyInsets(mChildView)
        }
    }

    /**
     * compat for CollapsingToolbarLayout
     *
     * 1. change to full-screen mode(like translucentStatusBar).
     * 2. cancel CollapsingToolbarLayout's WindowInsets, let it layout as normal(now setStatusBarScrimColor is useless).
     * 3. set View's FitsSystemWindow to false.
     * 4. add Toolbar's height, let it layout from top, then add paddingTop to layout normal.
     * 5. change statusBarColor by AppBarLayout's offset.
     * 6. add Listener to change statusBarColor
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun setStatusBarColorForCollapsingToolbarLollipop(
        activity: Activity,
        appBarLayout: AppBarLayout,
        collapsingToolbarLayout: CollapsingToolbarLayout,
        toolbar: Toolbar,
        statusColor: Int
    ) {
        val window = activity.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        ViewCompat.setOnApplyWindowInsetsListener(
            collapsingToolbarLayout
        ) { v: View?, insets: WindowInsetsCompat -> insets }
        val mContentView = window.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
        val mChildView = mContentView.getChildAt(0)
        if (mChildView != null) {
            ViewCompat.setFitsSystemWindows(mChildView, false)
            ViewCompat.requestApplyInsets(mChildView)
        }
        (appBarLayout.parent as View).fitsSystemWindows = false
        appBarLayout.fitsSystemWindows = false
        toolbar.fitsSystemWindows = false
        if (toolbar.tag == null) {
            val lp = toolbar.layoutParams as CollapsingToolbarLayout.LayoutParams
            val statusBarHeight = getStatusBarHeightLollipop(activity)
            lp.height += statusBarHeight
            toolbar.layoutParams = lp
            toolbar.setPadding(
                toolbar.paddingLeft,
                toolbar.paddingTop + statusBarHeight,
                toolbar.paddingRight,
                toolbar.paddingBottom
            )
            toolbar.tag = true
        }
        val behavior = (appBarLayout.layoutParams as CoordinatorLayout.LayoutParams).behavior
        if (behavior != null && behavior is AppBarLayout.Behavior) {
            val verticalOffset = behavior.topAndBottomOffset
            if (Math.abs(verticalOffset) > appBarLayout.height - collapsingToolbarLayout.scrimVisibleHeightTrigger) {
                window.statusBarColor = statusColor
            } else {
                window.statusBarColor = Color.TRANSPARENT
            }
        } else {
            window.statusBarColor = Color.TRANSPARENT
        }
        collapsingToolbarLayout.fitsSystemWindows = false
        val windowWeakReference = WeakReference(window)
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout1: AppBarLayout, verticalOffset: Int ->
            val weakWindow = windowWeakReference.get()
            if (weakWindow != null) {
                if (Math.abs(verticalOffset) > appBarLayout1.height - collapsingToolbarLayout.scrimVisibleHeightTrigger) {
                    if (weakWindow.statusBarColor != statusColor) {
                        startColorAnimation(
                            weakWindow.statusBarColor,
                            statusColor,
                            collapsingToolbarLayout.scrimAnimationDuration,
                            windowWeakReference
                        )
                    }
                } else {
                    if (weakWindow.statusBarColor != Color.TRANSPARENT) {
                        startColorAnimation(
                            weakWindow.statusBarColor,
                            Color.TRANSPARENT,
                            collapsingToolbarLayout.scrimAnimationDuration,
                            windowWeakReference
                        )
                    }
                }
            }
        })
        collapsingToolbarLayout.getChildAt(0).fitsSystemWindows = false
        collapsingToolbarLayout.setStatusBarScrimColor(statusColor)
    }

    /**
     * use ValueAnimator to change statusBarColor when using collapsingToolbarLayout
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun startColorAnimation(
        startColor: Int,
        endColor: Int,
        duration: Long,
        windowWeakReference: WeakReference<Window>
    ) {
        if (sAnimator != null) {
            sAnimator!!.cancel()
        }
        sAnimator = ValueAnimator.ofArgb(startColor, endColor)
            .setDuration(duration)
        sAnimator?.addUpdateListener(AnimatorUpdateListener { valueAnimator: ValueAnimator ->
            val window = windowWeakReference.get()
            if (window != null) {
                window.statusBarColor = (valueAnimator.animatedValue as Int)
            }
        })
        sAnimator?.start()
    }

    private var sAnimator: ValueAnimator? = null
    /// end lollipop
}