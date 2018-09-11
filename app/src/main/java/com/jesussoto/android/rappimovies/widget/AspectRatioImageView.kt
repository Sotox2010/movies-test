package com.jesussoto.android.rappimovies.widget

import android.content.Context
import android.support.annotation.FloatRange
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.View

import com.jesussoto.android.rappimovies.R

/**
 * ImageView that allows to define a custom aspect ratio like 4:3, 16:9 any other.
 */
class AspectRatioImageView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        AppCompatImageView(context, attrs, defStyleAttr) {

    private var _aspectRatio = 0f

    var aspectRatio: Float
        get() = _aspectRatio
        set(@FloatRange(from = 0.0) aspectRatio) {
            if (_aspectRatio != aspectRatio) {
                _aspectRatio = aspectRatio
                requestLayout()
            }
        }

    init {

        if (attrs != null) {
            val array = context.obtainStyledAttributes(attrs,
                    R.styleable.AspectRatioImageView, defStyleAttr, 0)

            _aspectRatio = array.getFloat(R.styleable.AspectRatioImageView_aspectRatio, 0f)

            array.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (_aspectRatio <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val height = (width / _aspectRatio).toInt()
        val heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

        super.onMeasure(widthMeasureSpec, heightSpec)
    }
}
