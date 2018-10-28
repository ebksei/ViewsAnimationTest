package com.example.ksei.customviewimagetest

import android.content.Context
import android.os.Handler
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout

class CustomLayout(context: Context, attributeSet: AttributeSet) : LinearLayout(context,attributeSet){

    private val viewsInScreen = mutableListOf(
        CustomImageView(context, 8),
        CustomImageView(context, 4),
        CustomImageView(context,2),
        CustomImageView(context, 1),
        CustomImageView(context, 1)
    )

    private val extraViews = mutableListOf(
        CustomImageView(context, 2),
        CustomImageView(context, 4),
        CustomImageView(context,1),
        CustomImageView(context, 8),
        CustomImageView(context, 1)
    )

    private var tempoInViews = viewsInScreen.sumBy { it.positionsLength }
    private val calcTime = 400
    private var viewSize = 0

    init{
        for(view in viewsInScreen){
            val drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_android_black_24dp, null)
            view.setImageDrawable(drawable)
            addView(view)
        }

    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        var leftPosition = l
        val length16thNote = (r-l)/16
        viewSize = (r-l)/16

        for(view in viewsInScreen){
            view.layout(leftPosition,t,leftPosition+viewSize ,t+viewSize)
            leftPosition += length16thNote*view.positionsLength
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var actualPosition = 0
        for(view in viewsInScreen){
            startAnimation(actualPosition, view)
            actualPosition+=view.positionsLength
        }
        startTempoListener()
        return true
    }

    private fun startTempoListener() {
        object : Runnable {
            override fun run() {
                tempoInViews -= 1

                if (tempoInViews < 16 && extraViews.size > 0) {
                    val view = extraViews.first()
                    extraViews.removeAt(0)
                    addAnimatedView(view)
                    tempoInViews += view.positionsLength
                }

                if (tempoInViews > 0) handler.postDelayed(this, calcTime.toLong())
            }
        }.run()
    }

    private fun addAnimatedView(view:CustomImageView) {
        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_arrow_back_black_24dp, null)
        view.setImageDrawable(drawable)
        view.layout(right - viewSize, top, right, top + viewSize)

        this.addView(view)
        viewsInScreen.add(view)

        startAnimation(view.x.toInt(), view)
    }

    private fun startAnimation(
        startingPosition: Int,
        view: CustomImageView
    ) {
        val targetPosition = (((this.right-this.left) - (this.right-view.x))*-1).toInt()
        val a = TranslateAnimation(
            Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, targetPosition.toFloat(),
            Animation.ABSOLUTE, view.y, Animation.ABSOLUTE, view.y
        )
        a.duration = (calcTime * startingPosition).toLong()
        a.interpolator = LinearInterpolator()
        a.fillAfter = true

        a.setAnimationListener(object : Animation.AnimationListener {
            val view = view
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                val runnable = Runnable {
                    val index = viewsInScreen.indexOf(view)
                    removeView(view)
                    viewsInScreen.drop(index)
                }
               Handler().postDelayed(runnable,((view.positionsLength - 1L)*calcTime))
            }
        })
        view.startAnimation(a)
    }

}