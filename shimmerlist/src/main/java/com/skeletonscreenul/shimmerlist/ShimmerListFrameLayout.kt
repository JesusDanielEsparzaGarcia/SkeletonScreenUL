package com.skeletonscreenul.shimmerlist

import android.graphics.drawable.Drawable
import android.os.Build
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout


class ShimmerListFrameLayout : FrameLayout {
    private val mContentPaint = Paint()
    private val mShimmerDrawable = ShimmerDrawable()

    /** Return whether the shimmer drawable is visible.  */
    var isShimmerVisible = true
        private set

    /** Return whether the shimmer animation has been started.  */
    val isShimmerStarted: Boolean
        get() = mShimmerDrawable.isShimmerStarted()

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private fun init(context: Context,attrs: AttributeSet?) {
        setWillNotDraw(false)
        mShimmerDrawable.setCallback(this)

        if (attrs == null) {
            setShimmer(Shimmer.AlphaHighlightBuilder().build())
            return
        }

        val a = context.obtainStyledAttributes(attrs, R.styleable.ShimmerListFrameLayout, 0, 0)
        try {
            val shimmerBuilder =
                if (a.hasValue(R.styleable.ShimmerListFrameLayout_shimmer_colored) && a.getBoolean(
                        R.styleable.ShimmerListFrameLayout_shimmer_colored,
                        false
                    )
                )
                    Shimmer.ColorHighlightBuilder()
                else
                    Shimmer.AlphaHighlightBuilder()
            setShimmer(shimmerBuilder.consumeAttributes(a).build())

            val num = a.getInt(R.styleable.ShimmerListFrameLayout_shimmer_number_of_items,1)
            mShimmerDrawable.numberOfItems = num

        } finally {
            a.recycle()
        }
    }

    fun setShimmer(shimmer: Shimmer?): ShimmerListFrameLayout {
        mShimmerDrawable.setShimmer(shimmer)
        if (shimmer != null && shimmer.clipToChildren) {
            setLayerType(View.LAYER_TYPE_HARDWARE, mContentPaint)
        } else {
            setLayerType(View.LAYER_TYPE_NONE, null)
        }

        return this
    }

    /** Starts the shimmer animation.  */
    fun startShimmer() {
        mShimmerDrawable.startShimmer()
    }

    /** Stops the shimmer animation.  */
    fun stopShimmer() {
        mShimmerDrawable.stopShimmer()
    }

    /**
     * Sets the ShimmerDrawable to be visible.
     *
     * @param startShimmer Whether to start the shimmer again.
     */
    fun showShimmer(startShimmer: Boolean) {
        if (isShimmerVisible) {
            return
        }
        isShimmerVisible = true
        if (startShimmer) {
            startShimmer()
        }
    }

    /** Sets the ShimmerDrawable to be invisible, stopping it in the process.  */
    fun hideShimmer() {
        if (!isShimmerVisible) {
            return
        }

        stopShimmer()
        isShimmerVisible = false
        invalidate()
    }

    public override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val width = width
        val height = height
        mShimmerDrawable.setBounds(0, 0, width, height)
    }

    public override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mShimmerDrawable.maybeStartShimmer()
    }

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopShimmer()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (isShimmerVisible) {
            mShimmerDrawable.draw(canvas)
        }
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who === mShimmerDrawable
    }
}