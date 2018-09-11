package com.jesussoto.android.rappimovies.util

import java.util.*
import java.util.concurrent.TimeUnit

object Utils {

    private const val REFRESH_THRESHOLD_MINUTES = 60

    fun isOlderThanOneHour(date: Date): Boolean {
        val diff = Math.abs(Date().time - date.time)
        return TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS).toInt() > REFRESH_THRESHOLD_MINUTES
    }
}
