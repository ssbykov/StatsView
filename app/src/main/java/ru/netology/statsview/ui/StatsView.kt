package ru.netology.statsview.ui

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import ru.netology.statsview.utils.AndroidUtils
import kotlin.math.min

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
    private var radius = 0F
    private var center = PointF()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - AndroidUtils.dp(context, 5)
        center = PointF(w / 2F, h / 2F)
    }
}