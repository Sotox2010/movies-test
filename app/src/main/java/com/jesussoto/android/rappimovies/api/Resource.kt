package com.jesussoto.android.rappimovies.api

/**
 * A wrapper class that holds a loading status along with its data.
 */
data class Resource<out T> constructor(val status: Status, val data: T?, val throwable: Throwable?) {

    enum class Status {
        LOADING, SUCCESS, ERROR
    }

    companion object {

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }

        fun <T> success(data: T): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(throwable: Throwable, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, throwable)
        }
    }
}
