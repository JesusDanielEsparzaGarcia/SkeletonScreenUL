package com.skeletonscreenul.shimmerlist



import android.animation.ValueAnimator
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.LinearGradient
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.graphics.ColorUtils


class ShimmerDrawable : Drawable() {
    var numberOfItems = 1
    private val rectForLoadingAnimation = Rect()
    private val shaderForLoadingAnimation: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val mUpdateListener = ValueAnimator.AnimatorUpdateListener { invalidateSelf() }

    private val mShimmerPaint = Paint()
    private val mDrawRect = Rect()
    private val mShaderMatrix = Matrix()

    private var mValueAnimator: ValueAnimator? = null

    private var mShimmer: Shimmer? = null

    fun isShimmerStarted(): Boolean{
        return mValueAnimator!= null && mValueAnimator?.isStarted == true
    }

    init {
        mShimmerPaint.setAntiAlias(true)
        shaderForLoadingAnimation.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }

    fun setShimmer(shimmer: Shimmer?) {
        mShimmer = shimmer
        if (mShimmer != null) {
            mShimmerPaint.setXfermode(
                PorterDuffXfermode(
                    if (mShimmer!!.alphaShimmer) PorterDuff.Mode.DST_IN else PorterDuff.Mode.SRC_IN
                )
            )
        }
        updateShader()
        updateMaskGradient()
        updateValueAnimator()
        invalidateSelf()
    }

    private fun updateMaskGradient(){
        val bounds = bounds
        val boundsHeight = bounds.height()
        val height = mShimmer!!.height(boundsHeight)

        val (positions,colors) = overlayOfOpacityOfLoadInLayers(numberOfItems,1f,mValueAnimator?.animatedFraction ?: 0f)
        val shader = LinearGradient(0f, 0f, 0f, height.toFloat(), colors, positions, Shader.TileMode.CLAMP)
        shaderForLoadingAnimation.shader = shader
    }

    private fun overlayOfOpacityOfLoadInLayers(numberOfItems: Int,animatePercent: Float,animatorValue: Float): Pair<FloatArray,IntArray>{
        val commonDifference = animatePercent/numberOfItems
        val aCent = animatePercent/100

        var fromHeroToZero = 0f

        if(animatorValue>0.5){
            fromHeroToZero = (animatorValue - 0.5f)/0.5f
        }else{
            fromHeroToZero = animatorValue/0.5f
        }

        val positions = mutableListOf<Float>()
        val colors = mutableListOf<Int>()

        for(i in 0 until numberOfItems){
            val position = i*commonDifference
            val position2 = (i+1)*commonDifference - aCent

            positions.add(position)
            positions.add(position2)

            if(animatorValue>0.5){
                val dif2 = 1f/numberOfItems
                val mdif = dif2*i
                var mdif2 = (fromHeroToZero - mdif)
                mdif2 = if(mdif2<=0) 0f else mdif2

                var nearo = mdif2 / dif2
                nearo = if(nearo>=1f) 1f else nearo

                colors.add(ColorUtils.setAlphaComponent(Color.BLACK,(nearo*255).toInt()))
                colors.add(ColorUtils.setAlphaComponent(Color.BLACK,(nearo*255).toInt()))
            }else{
                val dif2 = 1f/numberOfItems
                val mdif = dif2*i
                var mdif2 = (fromHeroToZero - mdif)
                mdif2 = if(mdif2<=0) 0f else mdif2

                var nearo = mdif2 / dif2
                nearo = if(nearo>=1f) 1f else nearo
                nearo = 1 - nearo

                colors.add(ColorUtils.setAlphaComponent(Color.BLACK,(nearo*255).toInt()))
                colors.add(ColorUtils.setAlphaComponent(Color.BLACK,(nearo*255).toInt()))

            }
        }
        return Pair(positions.toFloatArray(),colors.toIntArray())

    }

    /** Starts the shimmer animation.  */
    fun startShimmer() {
        if (mValueAnimator != null && !isShimmerStarted() && callback != null) {
            mValueAnimator!!.start()
        }
    }

    /** Stops the shimmer animation.  */
    fun stopShimmer() {
        if (mValueAnimator != null && isShimmerStarted()) {
            mValueAnimator!!.cancel()
        }
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        val width = bounds.width()
        val height = bounds.height()
        mDrawRect.set(0, 0, width, height)
        rectForLoadingAnimation.set(0,0,width,height)
        updateShader()
        updateMaskGradient()
        maybeStartShimmer()
    }

    override fun draw(@NonNull canvas: Canvas) {

        val moduleShimmer = 1f/numberOfItems

        if (mShimmer == null || mShimmerPaint.getShader() == null) {
            return
        }

        canvas.drawRect(rectForLoadingAnimation,shaderForLoadingAnimation)

        val tiltTan = Math.tan(Math.toRadians(mShimmer!!.tilt.toDouble())).toFloat()
        val translateHeight = mDrawRect.height() + tiltTan * mDrawRect.width()
        val translateWidth = mDrawRect.width() + tiltTan * mDrawRect.height()
        val dx: Float
        val dy: Float
        val animatedValue = if (mValueAnimator != null) mValueAnimator!!.animatedFraction else 0f
        //val animatedValue = if(mValueAnimator != null) (mValueAnimator!!.animatedFraction % moduleShimmer)/moduleShimmer else 0f
        when (mShimmer!!.direction) {
            Shimmer.Direction.LEFT_TO_RIGHT -> {
                dx = offset(-translateWidth, translateWidth, animatedValue)
                dy = 0f
            }
            Shimmer.Direction.RIGHT_TO_LEFT -> {
                dx = offset(translateWidth, -translateWidth, animatedValue)
                dy = 0f
            }
            Shimmer.Direction.TOP_TO_BOTTOM -> {
                dx = 0f
                dy = offset(-translateHeight, translateHeight, animatedValue)
            }
            Shimmer.Direction.BOTTOM_TO_TOP -> {
                dx = 0f
                dy = offset(translateHeight, -translateHeight, animatedValue)
            }
            else -> {
                dx = offset(-translateWidth, translateWidth, animatedValue)
                dy = 0f
            }
        }

        mShaderMatrix.reset()
        mShaderMatrix.setRotate(mShimmer!!.tilt, mDrawRect.width() / 2f, mDrawRect.height() / 2f)
        mShaderMatrix.postTranslate(dx, dy)
        mShimmerPaint.getShader().setLocalMatrix(mShaderMatrix)
        canvas.drawRect(mDrawRect, mShimmerPaint)
    }

    override fun setAlpha(alpha: Int) {
        // No-op, modify the Shimmer object you pass in instead
    }

    override fun setColorFilter(@Nullable colorFilter: ColorFilter?) {
        // No-op, modify the Shimmer object you pass in instead
    }

    override fun getOpacity(): Int {
        return if (mShimmer != null && (mShimmer!!.clipToChildren || mShimmer!!.alphaShimmer))
            PixelFormat.TRANSLUCENT
        else
            PixelFormat.OPAQUE
    }

    private fun offset(start: Float, end: Float, percent: Float): Float {
        return start + (end - start) * percent
    }

    private fun updateValueAnimator() {
        if (mShimmer == null) {
            return
        }

        val started: Boolean
        if (mValueAnimator != null) {
            started = mValueAnimator!!.isStarted
            mValueAnimator!!.cancel()
            mValueAnimator!!.removeAllUpdateListeners()
        } else {
            started = false
        }

        mValueAnimator = ValueAnimator.ofFloat(
            0f,
            1f + (mShimmer!!.repeatDelay / mShimmer!!.animationDuration).toFloat()
        )
        mValueAnimator!!.repeatMode = mShimmer!!.repeatMode
        mValueAnimator!!.repeatCount = mShimmer!!.repeatCount
        mValueAnimator!!.duration = mShimmer!!.animationDuration + mShimmer!!.repeatDelay
        mValueAnimator!!.addUpdateListener(mUpdateListener)
        if (started) {
            mValueAnimator!!.start()
        }
    }

    internal fun maybeStartShimmer() {
        if (mValueAnimator != null
            && !mValueAnimator!!.isStarted
            && mShimmer != null
            && mShimmer!!.autoStart
            && callback != null
        ) {
            mValueAnimator!!.start()
        }
    }

    private fun updateShader() {
        val bounds = bounds
        val boundsWidth = bounds.width()
        val boundsHeight = bounds.height()
        if (boundsWidth == 0 || boundsHeight == 0 || mShimmer == null) {
            return
        }
        val width = mShimmer!!.width(boundsWidth)
        val height = mShimmer!!.height(boundsHeight)

        val shader: Shader
        when (mShimmer!!.shape) {
            Shimmer.Shape.LINEAR -> {
                val vertical =
                    mShimmer!!.direction === Shimmer.Direction.TOP_TO_BOTTOM || mShimmer!!.direction === Shimmer.Direction.BOTTOM_TO_TOP
                val endX = if (vertical) 0 else width
                val endY = if (vertical) height else 0
                shader = LinearGradient(
                    0f, 0f, endX.toFloat(), endY.toFloat(), mShimmer!!.colors, mShimmer!!.positions, Shader.TileMode.CLAMP
                )
            }
            Shimmer.Shape.RADIAL -> shader = RadialGradient(
                width / 2f,
                height / 2f,
                (Math.max(width, height) / Math.sqrt(2.0)).toFloat(),
                mShimmer!!.colors,
                mShimmer!!.positions,
                Shader.TileMode.CLAMP
            )
            else -> {
                val vertical =
                    mShimmer!!.direction === Shimmer.Direction.TOP_TO_BOTTOM || mShimmer!!.direction === Shimmer.Direction.BOTTOM_TO_TOP
                val endX = if (vertical) 0 else width
                val endY = if (vertical) height else 0
                shader = LinearGradient(
                    0f,
                    0f,
                    endX.toFloat(),
                    endY.toFloat(),
                    mShimmer!!.colors,
                    mShimmer!!.positions,
                    Shader.TileMode.CLAMP
                )
            }
        }

        mShimmerPaint.setShader(shader)
    }
}