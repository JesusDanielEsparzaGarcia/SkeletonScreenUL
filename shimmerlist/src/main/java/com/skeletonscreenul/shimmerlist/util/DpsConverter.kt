package com.skeletonscreenul.shimmerlist.util

import android.content.Context

/**
 * Creado por Jesus Daniel Esparza Garcia el 07/11/2017.
 */

fun Int.fromDps(ctx: Context) : Int {
    val scale = ctx.resources.displayMetrics.density
    val pixels = (this * scale + 0.5f).toInt()
    return pixels
}