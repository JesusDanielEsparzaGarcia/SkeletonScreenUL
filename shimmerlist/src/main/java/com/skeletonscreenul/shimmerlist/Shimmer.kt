package com.skeletonscreenul.shimmerlist

import android.content.res.TypedArray
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.RectF
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.Px


class Shimmer internal constructor() {

    internal val positions = FloatArray(COMPONENT_COUNT)
    internal val colors = IntArray(COMPONENT_COUNT)
    internal val bounds = RectF()

    internal var direction = Direction.LEFT_TO_RIGHT
    @ColorInt
    internal var highlightColor = Color.WHITE
    @ColorInt
    internal var baseColor = 0x4cffffff
    internal var shape = Shape.LINEAR
    internal var fixedWidth = 0
    internal var fixedHeight = 0

    internal var widthRatio = 1f
    internal var heightRatio = 1f
    internal var intensity = 0f
    internal var dropoff = 0.5f
    internal var tilt = 20f

    internal var clipToChildren = true
    internal var autoStart = true
    internal var alphaShimmer = true

    internal var repeatCount = ValueAnimator.INFINITE
    internal var repeatMode = ValueAnimator.RESTART
    internal var animationDuration = 1000L
    internal var repeatDelay: Long = 0

    /** The shape of the shimmer's highlight. By default LINEAR is used.  */
    enum class Shape(val value: Int){
        LINEAR(0),
        RADIAL(1);

        companion object {
            private val map = Shape.values().associateBy(Shape::value)
            fun fromIntToShape(type: Int) = map[type]
        }
    }

    /** Direction of the shimmer's sweep.  */
    enum class Direction(val value: Int){
        LEFT_TO_RIGHT(0),
        TOP_TO_BOTTOM(1),
        RIGHT_TO_LEFT(2),
        BOTTOM_TO_TOP(3);

        companion object {
            private val map = Direction.values().associateBy(Direction::value)
            fun fromIntToDirection(type: Int) = map[type]
        }
    }

    internal fun width(width: Int): Int {
        return if (fixedWidth > 0) fixedWidth else Math.round(widthRatio * width)
    }

    internal fun height(height: Int): Int {
        return if (fixedHeight > 0) fixedHeight else Math.round(heightRatio * height)
    }

    internal fun updateColors() {
        when (shape) {
            Shape.LINEAR -> {
                colors[0] = baseColor
                colors[1] = highlightColor
                colors[2] = highlightColor
                colors[3] = baseColor
            }
            Shape.RADIAL -> {
                colors[0] = highlightColor
                colors[1] = highlightColor
                colors[2] = baseColor
                colors[3] = baseColor
            }
            else -> {
                colors[0] = baseColor
                colors[1] = highlightColor
                colors[2] = highlightColor
                colors[3] = baseColor
            }
        }
    }

    internal fun updatePositions() {
        when (shape) {
            Shape.LINEAR -> {
                positions[0] = Math.max((1f - intensity - dropoff) / 2f, 0f)
                positions[1] = Math.max((1f - intensity - 0.001f) / 2f, 0f)
                positions[2] = Math.min((1f + intensity + 0.001f) / 2f, 1f)
                positions[3] = Math.min((1f + intensity + dropoff) / 2f, 1f)
            }
            Shape.RADIAL -> {
                positions[0] = 0f
                positions[1] = Math.min(intensity, 1f)
                positions[2] = Math.min(intensity + dropoff, 1f)
                positions[3] = 1f
            }
            else -> {
                positions[0] = Math.max((1f - intensity - dropoff) / 2f, 0f)
                positions[1] = Math.max((1f - intensity - 0.001f) / 2f, 0f)
                positions[2] = Math.min((1f + intensity + 0.001f) / 2f, 1f)
                positions[3] = Math.min((1f + intensity + dropoff) / 2f, 1f)
            }
        }
    }

    internal fun updateBounds(viewWidth: Int, viewHeight: Int) {
        val magnitude = Math.max(viewWidth, viewHeight)
        val rad = Math.PI / 2f - Math.toRadians((tilt % 90f).toDouble())
        val hyp = magnitude / Math.sin(rad)
        val padding = 3 * Math.round((hyp - magnitude).toFloat() / 2f)
        bounds.set(
            (-padding).toFloat(),
            (-padding).toFloat(),
            (width(viewWidth) + padding).toFloat(),
            (height(viewHeight) + padding).toFloat()
        )
    }

    abstract class Builder<T : Builder<T>> {
        internal val mShimmer = Shimmer()

        // Gets around unchecked cast
        protected abstract val `this`: T

        /** Applies all specified options from the [AttributeSet].  */
        fun consumeAttributes(context: Context, attrs: AttributeSet): T {
            val a = context.obtainStyledAttributes(attrs, R.styleable.ShimmerListFrameLayout, 0, 0)
            return consumeAttributes(a)
        }

        internal open fun consumeAttributes(a: TypedArray): T {
            if (a.hasValue(R.styleable.ShimmerListFrameLayout_shimmer_clip_to_children)) {
                setClipToChildren(
                    a.getBoolean(
                        R.styleable.ShimmerListFrameLayout_shimmer_clip_to_children,
                        mShimmer.clipToChildren
                    )
                )
            }
            if (a.hasValue(R.styleable.ShimmerListFrameLayout_shimmer_auto_start)) {
                setAutoStart(
                    a.getBoolean(
                        R.styleable.ShimmerListFrameLayout_shimmer_auto_start,
                        mShimmer.autoStart
                    )
                )
            }
            if (a.hasValue(R.styleable.ShimmerListFrameLayout_shimmer_base_alpha)) {
                setBaseAlpha(a.getFloat(R.styleable.ShimmerListFrameLayout_shimmer_base_alpha, 0.3f))
            }
            if (a.hasValue(R.styleable.ShimmerListFrameLayout_shimmer_highlight_alpha)) {
                setHighlightAlpha(
                    a.getFloat(
                        R.styleable.ShimmerListFrameLayout_shimmer_highlight_alpha,
                        1f
                    )
                )
            }
            if (a.hasValue(R.styleable.ShimmerListFrameLayout_shimmer_duration)) {
                setDuration(
                    a.getInt(
                        R.styleable.ShimmerListFrameLayout_shimmer_duration,
                        mShimmer.animationDuration.toInt()
                    ).toLong()
                )
            }
            if (a.hasValue(R.styleable.ShimmerListFrameLayout_shimmer_repeat_count)) {
                setRepeatCount(
                    a.getInt(
                        R.styleable.ShimmerListFrameLayout_shimmer_repeat_count,
                        mShimmer.repeatCount
                    )
                )
            }
            if (a.hasValue(R.styleable.ShimmerListFrameLayout_shimmer_repeat_delay)) {
                setRepeatDelay(
                    a.getInt(
                        R.styleable.ShimmerListFrameLayout_shimmer_repeat_delay,
                        mShimmer.repeatDelay.toInt()
                    ).toLong()
                )
            }
            if (a.hasValue(R.styleable.ShimmerListFrameLayout_shimmer_repeat_mode)) {
                setRepeatMode(
                    a.getInt(
                        R.styleable.ShimmerListFrameLayout_shimmer_repeat_mode,
                        mShimmer.repeatMode
                    )
                )
            }

            if (a.hasValue(R.styleable.ShimmerListFrameLayout_shimmer_direction)) {
                val direction =
                    a.getInt(R.styleable.ShimmerListFrameLayout_shimmer_direction, mShimmer.direction.value)
                setDirection(direction)
            }

            if (a.hasValue(R.styleable.ShimmerListFrameLayout_shimmer_shape)) {
                val shape = a.getInt(R.styleable.ShimmerListFrameLayout_shimmer_shape, mShimmer.shape.value)
                setShape(shape)
            }

            if (a.hasValue(R.styleable.ShimmerListFrameLayout_shimmer_dropoff)) {
                setDropoff(
                    a.getFloat(
                        R.styleable.ShimmerListFrameLayout_shimmer_dropoff,
                        mShimmer.dropoff
                    )
                )
            }
            if (a.hasValue(R.styleable.ShimmerListFrameLayout_shimmer_fixed_width)) {
                setFixedWidth(
                    a.getDimensionPixelSize(
                        R.styleable.ShimmerListFrameLayout_shimmer_fixed_width, mShimmer.fixedWidth
                    )
                )
            }
            if (a.hasValue(R.styleable.ShimmerListFrameLayout_shimmer_fixed_height)) {
                setFixedHeight(
                    a.getDimensionPixelSize(
                        R.styleable.ShimmerListFrameLayout_shimmer_fixed_height, mShimmer.fixedHeight
                    )
                )
            }
            if (a.hasValue(R.styleable.ShimmerListFrameLayout_shimmer_intensity)) {
                setIntensity(
                    a.getFloat(R.styleable.ShimmerListFrameLayout_shimmer_intensity, mShimmer.intensity)
                )
            }
            if (a.hasValue(R.styleable.ShimmerListFrameLayout_shimmer_width_ratio)) {
                setWidthRatio(
                    a.getFloat(
                        R.styleable.ShimmerListFrameLayout_shimmer_width_ratio,
                        mShimmer.widthRatio
                    )
                )
            }
            if (a.hasValue(R.styleable.ShimmerListFrameLayout_shimmer_height_ratio)) {
                setHeightRatio(
                    a.getFloat(
                        R.styleable.ShimmerListFrameLayout_shimmer_height_ratio,
                        mShimmer.heightRatio
                    )
                )
            }
            if (a.hasValue(R.styleable.ShimmerListFrameLayout_shimmer_tilt)) {
                setTilt(a.getFloat(R.styleable.ShimmerListFrameLayout_shimmer_tilt, mShimmer.tilt))
            }
            return `this`
        }

        /** Copies the configuration of an already built Shimmer to this builder  */
        fun copyFrom(other: Shimmer): T {
            setDirection(other.direction.value)
            setShape(other.shape.value)
            setFixedWidth(other.fixedWidth)
            setFixedHeight(other.fixedHeight)
            setWidthRatio(other.widthRatio)
            setHeightRatio(other.heightRatio)
            setIntensity(other.intensity)
            setDropoff(other.dropoff)
            setTilt(other.tilt)
            setClipToChildren(other.clipToChildren)
            setAutoStart(other.autoStart)
            setRepeatCount(other.repeatCount)
            setRepeatMode(other.repeatMode)
            setRepeatDelay(other.repeatDelay)
            setDuration(other.animationDuration)
            mShimmer.baseColor = other.baseColor
            mShimmer.highlightColor = other.highlightColor
            return `this`
        }

        /** Sets the direction of the shimmer's sweep. See [Direction].  */
        fun setDirection(direction: Int): T {
            mShimmer.direction = Direction.fromIntToDirection(direction)!!
            return `this`
        }

        /** Sets the shape of the shimmer. See [Shape].  */
        fun setShape(shape: Int): T {
            mShimmer.shape = Shape.fromIntToShape(shape)!!
            return `this`
        }

        /** Sets the fixed width of the shimmer, in pixels.  */
        fun setFixedWidth(@Px fixedWidth: Int): T {
            if (fixedWidth < 0) {
                throw IllegalArgumentException("Given invalid width: $fixedWidth")
            }
            mShimmer.fixedWidth = fixedWidth
            return `this`
        }

        /** Sets the fixed height of the shimmer, in pixels.  */
        fun setFixedHeight(@Px fixedHeight: Int): T {
            if (fixedHeight < 0) {
                throw IllegalArgumentException("Given invalid height: $fixedHeight")
            }
            mShimmer.fixedHeight = fixedHeight
            return `this`
        }

        /** Sets the width ratio of the shimmer, multiplied against the total width of the layout.  */
        fun setWidthRatio(widthRatio: Float): T {
            if (widthRatio < 0f) {
                throw IllegalArgumentException("Given invalid width ratio: $widthRatio")
            }
            mShimmer.widthRatio = widthRatio
            return `this`
        }

        /** Sets the height ratio of the shimmer, multiplied against the total height of the layout.  */
        fun setHeightRatio(heightRatio: Float): T {
            if (heightRatio < 0f) {
                throw IllegalArgumentException("Given invalid height ratio: $heightRatio")
            }
            mShimmer.heightRatio = heightRatio
            return `this`
        }

        /** Sets the intensity of the shimmer. A larger value causes the shimmer to be larger.  */
        fun setIntensity(intensity: Float): T {
            if (intensity < 0f) {
                throw IllegalArgumentException("Given invalid intensity value: $intensity")
            }
            mShimmer.intensity = intensity
            return `this`
        }

        /**
         * Sets how quickly the shimmer's gradient drops-off. A larger value causes a sharper drop-off.
         */
        fun setDropoff(dropoff: Float): T {
            if (dropoff < 0f) {
                throw IllegalArgumentException("Given invalid dropoff value: $dropoff")
            }
            mShimmer.dropoff = dropoff
            return `this`
        }

        /** Sets the tilt angle of the shimmer in degrees.  */
        fun setTilt(tilt: Float): T {
            mShimmer.tilt = tilt
            return `this`
        }

        /**
         * Sets the base alpha, which is the alpha of the underlying children, amount in the range [0,
         * 1].
         */
        fun setBaseAlpha(@FloatRange(from = 0.toDouble(), to = 1.toDouble()) alpha: Float): T {
            val intAlpha = (clamp(0f, 1f, alpha) * 255f).toInt()
            mShimmer.baseColor = intAlpha shl 24 or (mShimmer.baseColor and 0x00FFFFFF)
            return `this`
        }

        /** Sets the shimmer alpha amount in the range [0, 1].  */
        fun setHighlightAlpha(@FloatRange(from = 0.toDouble(), to = 1.toDouble()) alpha: Float): T {
            val intAlpha = (clamp(0f, 1f, alpha) * 255f).toInt()
            mShimmer.highlightColor = intAlpha shl 24 or (mShimmer.highlightColor and 0x00FFFFFF)
            return `this`
        }

        /**
         * Sets whether the shimmer will clip to the childrens' contents, or if it will opaquely draw on
         * top of the children.
         */
        fun setClipToChildren(status: Boolean): T {
            mShimmer.clipToChildren = status
            return `this`
        }

        /** Sets whether the shimmering animation will start automatically.  */
        fun setAutoStart(status: Boolean): T {
            mShimmer.autoStart = status
            return `this`
        }

        /**
         * Sets how often the shimmering animation will repeat. See [ ][android.animation.ValueAnimator.setRepeatCount].
         */
        fun setRepeatCount(repeatCount: Int): T {
            mShimmer.repeatCount = repeatCount
            return `this`
        }

        /**
         * Sets how the shimmering animation will repeat. See [ ][android.animation.ValueAnimator.setRepeatMode].
         */
        fun setRepeatMode(mode: Int): T {
            mShimmer.repeatMode = mode
            return `this`
        }

        /** Sets how long to wait in between repeats of the shimmering animation.  */
        fun setRepeatDelay(millis: Long): T {
            if (millis < 0) {
                throw IllegalArgumentException("Given a negative repeat delay: $millis")
            }
            mShimmer.repeatDelay = millis
            return `this`
        }

        /** Sets how long the shimmering animation takes to do one full sweep.  */
        fun setDuration(millis: Long): T {
            if (millis < 0) {
                throw IllegalArgumentException("Given a negative duration: $millis")
            }
            mShimmer.animationDuration = millis
            return `this`
        }

        fun build(): Shimmer {
            mShimmer.updateColors()
            mShimmer.updatePositions()
            return mShimmer
        }

        private fun clamp(min: Float, max: Float, value: Float): Float {
            return Math.min(max, Math.max(min, value))
        }
    }

    class AlphaHighlightBuilder : Builder<AlphaHighlightBuilder>() {

        override val `this`: AlphaHighlightBuilder
            get() = this

        init {
            mShimmer.alphaShimmer = true
        }
    }

    class ColorHighlightBuilder : Builder<ColorHighlightBuilder>() {

        override val `this`: ColorHighlightBuilder
            get() = this

        init {
            mShimmer.alphaShimmer = false
        }

        /** Sets the highlight color for the shimmer.  */
        fun setHighlightColor(@ColorInt color: Int): ColorHighlightBuilder {
            mShimmer.highlightColor = color
            return `this`
        }

        /** Sets the base color for the shimmer.  */
        fun setBaseColor(@ColorInt color: Int): ColorHighlightBuilder {
            mShimmer.baseColor = mShimmer.baseColor and -0x1000000 or (color and 0x00FFFFFF)
            return `this`
        }

        override fun consumeAttributes(a: TypedArray): ColorHighlightBuilder {
            super.consumeAttributes(a)
            if (a.hasValue(R.styleable.ShimmerListFrameLayout_shimmer_base_color)) {
                setBaseColor(
                    a.getColor(
                        R.styleable.ShimmerListFrameLayout_shimmer_base_color,
                        mShimmer.baseColor
                    )
                )
            }
            if (a.hasValue(R.styleable.ShimmerListFrameLayout_shimmer_highlight_color)) {
                setHighlightColor(
                    a.getColor(
                        R.styleable.ShimmerListFrameLayout_shimmer_highlight_color,
                        mShimmer.highlightColor
                    )
                )
            }
            return `this`
        }
    }

    companion object {
        private val COMPONENT_COUNT = 4
    }
}