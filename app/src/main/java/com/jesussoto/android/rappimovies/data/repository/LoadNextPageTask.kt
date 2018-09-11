package com.jesussoto.android.rappimovies.data.repository

import android.arch.lifecycle.MutableLiveData
import com.jesussoto.android.rappimovies.api.Resource
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

/**
 * Task for loading the next page of content.
 */
abstract class LoadNextPageTask<T, ResponseType>(
        private val liveData: MutableLiveData<Resource<List<T>>>,
        private val offset: Int,
        private val pageSize: Int,
        private val itemsCache: ArrayList<T>
) : Runnable {

    abstract fun getItemsByPage(offset: Int, pageSize: Int): List<T>

    abstract fun getServiceSingleCall(): Single<ResponseType>

    abstract fun saveToDatabase(newItems: List<T>, shouldReplace: Boolean)

    abstract fun setCategory(item: T)

    abstract fun getResults(response: ResponseType): List<T>

    abstract fun shouldFetch(prefetchItems: List<T>): Boolean

    override fun run() {
        // Emit 'loading' status.
        liveData.postValue(Resource.loading(null))

        val items = getItemsByPage(offset, pageSize)
        val shouldFetch = shouldFetch(items)

        if (!items.isEmpty() && !shouldFetch) {
            itemsCache.addAll(items)
            liveData.postValue(Resource.success(items))
        } else {
            getServiceSingleCall()
                    .map(this::getResults)
                    .flatMapObservable { Observable.fromIterable(it) }
                    .doOnNext { item -> setCategory(item) }
                    .toList()
                    .doOnSuccess { saveToDatabase(it, shouldFetch) }
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.computation())
                    .subscribe(
                            // onSuccess
                            { liveData.postValue(Resource.success(it))
                                itemsCache.addAll(it)
                            },
                            // onError
                            { handleError(it, items, shouldFetch) }
                    )
        }
    }

    private fun handleError(t : Throwable, prefetchItems: List<T>, shouldFetch: Boolean) {
        if (shouldFetch && !prefetchItems.isEmpty()) {
            liveData.postValue(Resource.success(prefetchItems))
            return
        }

        liveData.postValue(Resource.error(t, null))
    }
}