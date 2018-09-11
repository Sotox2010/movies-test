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

    abstract fun saveToDatabase(items: List<T>)

    abstract fun setCategory(item: T)

    abstract fun getResults(response: ResponseType): List<T>

    override fun run() {
        val items = getItemsByPage(offset, pageSize)

        if (items.size == pageSize) {
            itemsCache.addAll(items)
            liveData.postValue(Resource.success(items))
        } else {
            // service.getPopularMovies(page)
            getServiceSingleCall()
                    .doOnSubscribe { liveData.postValue(Resource.loading(null)) }
                    .map(this::getResults)
                    .flatMapObservable { Observable.fromIterable(it) }
                    .doOnNext { item -> setCategory(item) }
                    .toList()
                    .doOnSuccess(this::saveToDatabase)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.computation())
                    .subscribe(
                            // onSuccess
                            { liveData.postValue(Resource.success(it))
                                itemsCache.addAll(it)
                            },
                            // onError
                            { liveData.postValue(Resource.error(it, null)) }
                    )
        }
    }
}