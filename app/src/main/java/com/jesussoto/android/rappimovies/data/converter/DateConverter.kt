package com.jesussoto.android.rappimovies.data.converter

import android.arch.persistence.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

object DateConverter {

    private const val DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
    private const val DATE_FORMAT = "yyyy-MM-dd"

    @TypeConverter
    @JvmStatic
    fun stringToDate(datetime: String?): Date? {
        return datetime?.let {
            val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.US)
            val datetimeFormat = SimpleDateFormat(DATETIME_FORMAT, Locale.US)

            var result: Date? = null

            try {
                result = datetimeFormat.parse(datetime)
            } catch (t: Throwable) {
                // ignored
            }

            if (result != null) {
                return result
            }

            try {
                result = dateFormat.parse(datetime)
            } catch (t: Throwable) {
                // ignored
            }

            return result
        }
    }

    @TypeConverter
    @JvmStatic
    fun stringFromDate(date: Date?): String? {
        val formatter = SimpleDateFormat(DATETIME_FORMAT, Locale.US)
        return date?.let(formatter::format)
    }
}