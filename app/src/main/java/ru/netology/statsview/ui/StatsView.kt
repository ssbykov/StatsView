package ru.netology.statsview.ui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import kotlinx.coroutines.delay
import ru.netology.statsview.R
import ru.netology.statsview.utils.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(
    context,
    attributeSet,
    defStyleAttr,
    defStyleRes
) {
    private var textSize = AndroidUtils.dp(context, 20).toFloat()
    private var lineWidth = AndroidUtils.dp(context, 5).toFloat()
    private var animFormat = 0
    private var colors = emptyList<Int>()

    init {
        context.withStyledAttributes(attributeSet, R.styleable.StatsView) {
            textSize = getDimension(R.styleable.StatsView_textSize, textSize)
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            animFormat = getInteger(R.styleable.StatsView_animFormat, animFormat)
            colors = listOf(
                getColor(R.styleable.StatsView_color1, generateRandomColor()),
                getColor(R.styleable.StatsView_color2, generateRandomColor()),
                getColor(R.styleable.StatsView_color3, generateRandomColor()),
                getColor(R.styleable.StatsView_color4, generateRandomColor()),
                getColor(R.styleable.StatsView_color1, generateRandomColor()),
            )
        }
    }

    private var progress = 0F
    private var valueAnimator: ValueAnimator? = null
    var data = Pair(emptyList<Float>(), 0F)
        set(value) {
            field = value
            update()
        }
    private var radius = 0F
    private var center = PointF()
    private var oval = RectF()
    private val paint = Paint(
        Paint.ANTI_ALIAS_FLAG
    ).apply {
        strokeWidth = lineWidth.toFloat()
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private val textPaint = Paint(
        Paint.ANTI_ALIAS_FLAG
    ).apply {
        textSize = this@StatsView.textSize
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius,
        )
    }

    override fun onDraw(canvas: Canvas) {
        val total = data.first.sum() + data.second
        if (data.first.isNotEmpty()) {
            var startAngle = -90F
            data.first.forEachIndexed { index, datum ->
                val angle = datum / total * 360
                paint.color = colors.getOrElse(index) { generateRandomColor() }
                canvas.drawArc(
                    oval,
                    calcStartAngle(startAngle, angle, animFormat),
                    calcAngle(startAngle, angle, animFormat),
                    false,
                    paint
                )
                startAngle += angle
            }
            if (progress == 1F) {
                paint.color = colors.getOrElse(0) { generateRandomColor() }
                canvas.drawPoint(center.x, center.y - radius, paint)
            }
        }
        canvas.drawText(
            "%.2f%%".format((total - data.second) / total * 100),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint
        )
    }

    private fun calcStartAngle(startAngle: Float, angle: Float, animFormat: Int): Float {
        return when (animFormat) {
            3 -> startAngle + angle / 2f * (1 - progress)
            2 -> startAngle
            else -> startAngle + 360 * progress
        }
    }

    private fun calcAngle(startAngle: Float, angle: Float, animFormat: Int): Float {
        return if (animFormat == 2) {
            when (360 * progress - 90F) {
                in (startAngle..startAngle + angle) -> 360 * progress - (startAngle + 90F)
                in (-90F..startAngle) -> 0F
                else -> angle
            }
        } else angle * progress
    }

    private fun update() {
        valueAnimator?.let {
            it.removeAllListeners()
            it.cancel()
        }
        progress = 0F

        valueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener { anim ->
                progress = anim.animatedValue as Float
                invalidate()
            }
            duration = 2500
            interpolator = LinearInterpolator()
        }.also {
            it.start()
        }
    }

    private fun generateRandomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}