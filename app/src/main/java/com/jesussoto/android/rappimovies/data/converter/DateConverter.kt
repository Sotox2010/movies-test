package com.jesussoto.android.rappimovies.data.converter

import android.arch.persistence.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

object DateConverter {

    @TypeConverter
    @JvmStatic
    fun stringToDate(datetime: String?): Date? {
        return datetime?.let {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return try {
                formatter.parse(datetime)
            } catch (t: Throwable) {
                t.printStackTrace()
                null
            }
        }
    }

    @TypeConverter
    @JvmStatic
    fun stringFromDate(date: Date?): String? {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return date?.let(formatter::format)
    }
}