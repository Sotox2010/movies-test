package com.jesussoto.android.rappimovies.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import java.util.*

object PrefUtils {

    private const val PREF_LAST_POPULAR_MOVIES_SYNC = "pref_last_popular_movies_sync"

    fun getLastPopularMoviesSync(context: Context): Long {
        val sp = getSharedPreferences(context.applicationContext)
        return sp.getLong(PREF_LAST_POPULAR_MOVIES_SYNC, 0L)
    }

    fun setLastPopularMoviesSyncToNow(context: Context) {
        val sp = getSharedPreferences(context.applicationContext)
        sp.edit().putLong(PREF_LAST_POPULAR_MOVIES_SYNC, Date().time).apply()
    }

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }
}