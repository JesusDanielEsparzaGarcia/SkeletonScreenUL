package com.skeletonscreenul.shimmerlist

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import android.graphics.Shader
import android.graphics.LinearGradient
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.core.graphics.ColorUtils
import com.skeletonscreenul.shimmerlist.util.fromDps
import kotlin.math.max

class ShimmerList: View, ValueAnimator.AnimatorUpdateListener  {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var listItemPattern: Bitmap? = null
    private val vSpacing: Float = 8.fromDps(context).toFloat()
    private val hSpacing: Float = 8.fromDps(context).toFloat()
    private val lineHeight: Float = 15.fromDps(context).toFloat()
    private val imageSize: Float = 68.fromDps(context).toFloat()
    private val cornerRadius: Float = 2.fromDps(context).toFloat()
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mShaderMatrix = Matrix()


    private val shaderPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shaderFade2: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var shaderRect = Rect()
    var shaderRect2 = Rect()

    var numbersOfItems = 1

    var percentOfScreenUsed = 1f

    init {
        shaderPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        shaderFade2.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        setLayerType(View.LAYER_TYPE_HARDWARE, paint)
    }

    private val shaderColors: IntArray = intArrayOf(
        0x4cffffff,
        Color.WHITE,
        Color.WHITE,
        0x4cffffff
    )

    private val animator: ValueAnimator = ValueAnimator.ofFloat(-1f, 2f).apply {
        duration = 1000L
        interpolator = LinearInterpolator()
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener(this@ShimmerList)
    }


    override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
        if (isAttachedToWindow) {
            updateShader(width.toFloat(), height.toFloat())
            updateFade2(width.toFloat(),height.toFloat(),valueAnimator.animatedFraction)
            invalidate()
        } else {
            animator.cancel()
        }
    }



    override fun onDraw(canvas: Canvas) {

        // draw shape
        listItemPattern?.let {
            val moduleShimmer = 1f/numbersOfItems


            // draw list item pattern
            canvas.drawBitmap(it, 0f, 0f, paint)
            val tiltTan = Math.tan(Math.toRadians(20.00)).toFloat()
            val translateWidth = shaderRect.width() + tiltTan * shaderRect.height()
            //val animatedValue = animator.animatedFraction
            val animatedValue = (animator.animatedFraction % moduleShimmer)/moduleShimmer

            val dx = offset(-translateWidth, translateWidth, animatedValue)
            val dy: Float = 0f

            canvas.drawRect(shaderRect2,shaderFade2)

            mShaderMatrix.reset()
            mShaderMatrix.setRotate(20f, shaderRect.width() / 2f, shaderRect.height() / 2f)
            mShaderMatrix.postTranslate(dx, dy)
            shaderPaint.shader.setLocalMatrix(mShaderMatrix)
            canvas.drawRect(shaderRect,shaderPaint)


        }
    }

    private fun offset(start: Float, end: Float, percent: Float): Float {
        return start + (end - start) * percent
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        when (visibility) {
            VISIBLE -> animator.start()
            INVISIBLE, GONE -> animator.cancel()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        shaderRect.set(0,0,w,h)
        shaderRect2.set(0,0,w,h)
        updateShader(w.toFloat(), h.toFloat())

        if (h > 0 && w > 0) {
            preDrawItemPattern(w, h)
        }
    }

    private fun updateShader(w: Float,height: Float) {
        val shader = LinearGradient(0f, 0f, w, 0f, shaderColors, floatArrayOf(0.25f, .4995f, 0.5005f,0.75f), Shader.TileMode.CLAMP)
        shaderPaint.shader = shader
    }

    private fun updateFade2(w: Float,height: Float,animvalue: Float){
        val (positions,colors) = overlayOfOpacityOfLoadInLayers(numbersOfItems,percentOfScreenUsed,animvalue)
        val shader = LinearGradient(0f, 0f, 0f, height, colors, positions, Shader.TileMode.CLAMP)
        shaderFade2.shader = shader

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


    private fun preDrawItemPattern(w: Int, h: Int) {
        listItemPattern = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {

            // draw list items into the bitmap
            val canvas = Canvas(this)

            var top = 0
            var itemCount = 1
            var cantidadAproximadaItems = 1
            var sheight = 1

            do {

                var itemColor : Int = 1
                if(cantidadAproximadaItems!=1){
                    val progression = 5.toDouble()/cantidadAproximadaItems
                    itemColor = (1 + (itemCount-1)*progression).toInt()
                    //itemColor = Math.ceil(itemCount.toDouble()*5 / cantidadAproximadaItems.toDouble()).toInt()
                }


                val color = when(itemColor) {
                    1->ContextCompat.getColor(context,R.color.colorBlankData500)
                    2->ContextCompat.getColor(context,R.color.colorBlankData400)
                    3->ContextCompat.getColor(context,R.color.colorBlankData300)
                    4->ContextCompat.getColor(context,R.color.colorBlankData200)
                    5->ContextCompat.getColor(context,R.color.colorBlankData100)
                    else ->ContextCompat.getColor(context,R.color.colorBlankData50)
                }

                itemCount++


                val item = getItemBitmap(w,color)
                cantidadAproximadaItems = canvas.height /(item.height + 8.fromDps(context))
                canvas.drawBitmap(item, 0f, top.toFloat(), paint);
                //top = top + item.getHeight();
                top += item.height + 8.fromDps(context)
                sheight = item.height + 8.fromDps(context)
            } while(top + item.height < canvas.getHeight());

            numbersOfItems = cantidadAproximadaItems
            animator.setDuration(1000L * numbersOfItems)
            percentOfScreenUsed = sheight.toFloat()*numbersOfItems/canvas.height
        }
    }

    private fun getItemBitmap(w : Int,color : Int): Bitmap {
        val h = calculatePatternHeight(3)

        val item = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(item)

        val itemPaint = Paint()
        itemPaint.isAntiAlias = true
        itemPaint.color = Color.WHITE
        itemPaint.style = Paint.Style.FILL

        val placeholderPaint = Paint()
        placeholderPaint.isAntiAlias = true
        placeholderPaint.color = color
        placeholderPaint.style = Paint.Style.FILL

        //container
        val rectContainer = RectF(0F,0F,w.toFloat(),h.toFloat())
        canvas.drawRoundRect(rectContainer,cornerRadius,cornerRadius,itemPaint)

        // avatar
        val rectF = RectF(vSpacing, hSpacing, vSpacing + imageSize, hSpacing + imageSize)
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, placeholderPaint)

        val textLeft = rectF.right + hSpacing
        val textRight = canvas.width - vSpacing

        // title line
        val titleWidth = ((textRight - textLeft) * 0.5).toFloat()
        rectF.set(textLeft, hSpacing, textLeft + titleWidth, hSpacing + lineHeight)
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, placeholderPaint)

        // timestamp
        val timeWidth = ((textRight - textLeft) * 0.2).toFloat()
        rectF.set(textRight - timeWidth, hSpacing, textRight, hSpacing + lineHeight)
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, placeholderPaint)

        // text lines
        var line = 2
        while (line > 0) {
            val lineTop = rectF.bottom + hSpacing
            rectF.set(textLeft, lineTop, textRight, lineTop + lineHeight)
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, placeholderPaint)
            line--
        }
        return item

    }

    private fun calculatePatternHeight(lines : Int): Int {
        val minHeight = imageSize + vSpacing * 2
        val linesHeight = (lines * lineHeight + vSpacing * (lines + 1))
        return max(minHeight, linesHeight).toInt()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        listItemPattern?.recycle()
        animator.cancel()
    }
}