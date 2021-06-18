package com.udacity

import android.animation.*
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

const val TAG = "LoadingButton"

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var animatedWidthSize = 0f

    private var buttonBgColor = 0
    private var buttonTextColor = 0
    private var buttonDownloadColor = context.getColor(R.color.colorPrimaryDark)
    private var circleColor = context.getColor(R.color.colorAccent)

    lateinit var circleRectF: RectF
    var circleDegree = 0f

    private var buttonText = context.getString(R.string.button_name)

    private lateinit var valueAnimator: ValueAnimator

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        if(new == ButtonState.Clicked){
            startAnimation(2000)
        } else if(new == ButtonState.Completed){
            if(::valueAnimator.isInitialized)
                valueAnimator.cancel()
            startAnimation(500)
        } else if(new == ButtonState.Loading) {
            startAnimation(3000)
        }
    }


    init {
        context.withStyledAttributes(attrs, R.styleable.LoadingButton){
            buttonBgColor = getColor(R.styleable.LoadingButton_buttonBgColor, 0)
            buttonTextColor = getColor(R.styleable.LoadingButton_buttonTextColor, 0)
        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawColor(buttonBgColor)

        paint.color = buttonDownloadColor
        canvas?.drawRect(0f, 0f, animatedWidthSize, heightSize.toFloat(), paint)

        paint.color = buttonTextColor
        val textHeight = paint.fontMetrics.descent - paint.fontMetrics.ascent
        val textPositionY = (height / 2 + textHeight / 2 - paint.fontMetrics.descent)
        // set text's position based on view's height & text's height (referred on : https://www.programmersought.com/article/88124662451/)
        canvas?.drawText(buttonText, (width / 2).toFloat(),textPositionY , paint)

        if(!::circleRectF.isInitialized)
            circleRectF = RectF(width / 4f * 3f - 40f, height / 2f - 40f, width / 4f * 3f + 40f, height / 2f + 40f)

        paint.color = circleColor
        canvas?.drawArc(circleRectF, 0f, circleDegree, true, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)

        Log.i(TAG, "circleCenterX: ${w}")
        Log.i(TAG, "circleCenterY: ${h}")

    }

    private fun startAnimation(duration: Long) {
        buttonText = context.getString(R.string.button_loading)
        isClickable = false
        valueAnimator = ValueAnimator.ofFloat(animatedWidthSize, widthSize.toFloat()).apply {
            this.duration = duration
            if(buttonState == ButtonState.Loading)
                interpolator = DecelerateInterpolator()
            addUpdateListener {
                animatedWidthSize = it.animatedValue as Float
                circleDegree = animatedWidthSize / widthSize.toFloat() * 360f
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator?) {
                    if(animatedWidthSize == widthSize.toFloat()) {
                        buttonText = context.getString(R.string.button_name)
                        animatedWidthSize = 0f
                        circleDegree = 0f
                        invalidate()
                        isClickable = true
                    }
                }
            })
        }
        valueAnimator.start()
    }

    fun downloadStarted() {
        buttonState = ButtonState.Clicked
    }

    fun downloadCompleted() {
        buttonState = ButtonState.Completed
    }

    fun downloadFailed() {
        buttonState = ButtonState.Loading
    }
}