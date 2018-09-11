package com.jesussoto.android.rappimovies.util

import android.content.Context
import android.net.ConnectivityManager

object NetwotkUtils {

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(
                Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo.isConnectedOrConnecting
    }
}