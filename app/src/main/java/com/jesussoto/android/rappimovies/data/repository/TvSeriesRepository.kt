package com.jesussoto.android.rappimovies.data.repository

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.text.TextUtils
import android.util.LruCache
import com.jesussoto.android.rappimovies.api.Resource
import com.jesussoto.android.rappimovies.api.WebService
import com.jesussoto.android.rappimovies.api.WebServiceUtils
import com.jesussoto.android.rappimovies.api.model.Video
import com.jesussoto.android.rappimovies.api.response.TvSeriesResponse
import com.jesussoto.android.rappimovies.api.response.VideosResponse
import com.jesussoto.android.rappimovies.data.AppDatabase
import com.jesussoto.android.rappimovies.data.dao.TvSeriesDao
import com.jesussoto.android.rappimovies.data.entity.TvSeries
import com.jesussoto.android.rappimovies.movies.FilterType
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class TvSeriesRepository(
        private val service: WebService,
        private val database: AppDatabase,
        private val tvSeriesDao: TvSeriesDao) {

    // Simple in-memory cache for popular tv-series.
    private val popularSeriesLiveData: MutableLiveData<Resource<List<TvSeries>>> = MutableLiveData()

    // Simple in-memory cache for top rated tv-series.
    private val topRatedSeriesLiveData: MutableLiveData<Resource<List<TvSeries>>> = MutableLiveData()

    // Simple in-memory cache for videos.
    private val videosLiveData: MutableLiveData<Resource<List<Video>>> = MutableLiveData()

    // 5MiB cache for videos.
    private val videosCache = LruCache<Long, Resource<List<Video>>>(5 * 1024 * 1024)

    /**
     * Load top-rated tv-series from the network if no previous fetch occurred, or return the
     * cached tv-series.
     */
    fun loadPopularTvSeries(): LiveData<Resource<List<TvSeries>>> {
        Single.concat(tvSeriesDao.getPopularTvSeries(), fetchPopularTvSeries())
                .doOnSubscribe { popularSeriesLiveData.postValue(Resource.loading(null)) }
                .filter { !it.isEmpty() }
                .firstOrError()
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .subscribe(
                        // onSuccess
                        { popularSeriesLiveData.postValue(Resource.success(it)) },

                        //onError
                        { popularSeriesLiveData.postValue(Resource.error(it, null)) }
                )

        return popularSeriesLiveData
    }

    /**
     * Load top-rated tv-series from the network if no previous fetch occurred, or return the
     * cached tv-series.
     */
    fun loadTopRatedTvSeries(): LiveData<Resource<List<TvSeries>>> {
        Single.concat(tvSeriesDao.getTopRatedTvSeries(), fetchTopRatedTvSeries())
                .doOnSubscribe { topRatedSeriesLiveData.postValue(Resource.loading(null)) }
                .filter { !it.isEmpty() }
                .firstOrError()
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .subscribe(
                        // onSuccess
                        { topRatedSeriesLiveData.postValue(Resource.success(it)) },

                        //onError
                        { topRatedSeriesLiveData.postValue(Resource.error(it, null)) }
                )

        return topRatedSeriesLiveData
    }

    /**
     * Core method for fetch popular tv-series from the network using RxJava, placed on a separate
     * method for re-usability on first load or forced refresh.
     */
    private fun fetchPopularTvSeries(): Single<List<TvSeries>> {
        return service.getPopularTvSeries()
                .map(TvSeriesResponse::results)
                .flatMapObservable { Observable.fromIterable(it) }
                .doOnNext { it.category = "popular" }
                .toList()
                .flatMapCompletable(this::saveTvSeries)
                .andThen(tvSeriesDao.getPopularTvSeries())
    }

    /**
     * Core method for fetch top-rated tv-series from the network using RxJava, placed on a separate
     * method for re-usability on first load or forced refresh.
     *
     * @param resultData the [LiveData] to post the result to.
     */
    @SuppressLint("CheckResult")
    private fun fetchTopRatedTvSeries(): Single<List<TvSeries>> {
        return service.getTopRatedTvSeries()
                .map(TvSeriesResponse::results)
                .flatMapObservable { Observable.fromIterable(it) }
                .doOnNext { it.category = "top-rated" }
                .toList()
                .flatMapCompletable(this::saveTvSeries)
                .andThen(tvSeriesDao.getTopRatedTvSeries())
    }

    /**
     *
     */
    private fun saveTvSeries(tvSeriesList: List<TvSeries>): Completable {
        return Completable.fromAction {
            tvSeriesDao.insertTvSeries(tvSeriesList)
        }
    }

    /**
     *
     */
    fun searchByNameAndCategory(query: String, category: FilterType): Flowable<List<TvSeries>> {
        if (category == FilterType.UPCOMING) {
            return Flowable.just(ArrayList())
        }

        val categoryString = when (category) {
            FilterType.POPULAR -> "popular"
            FilterType.TOP_RATED -> "top-rated"
            else -> throw IllegalStateException()
        }

        if (TextUtils.isEmpty(query)) {
            return Flowable.just(ArrayList())
        }

        return tvSeriesDao.searchByNameAndCategory("%$query%", categoryString)
    }

    /**
     *
     */
    fun loadTvSeriesById(movieId: Long): Single<TvSeries> {
        return tvSeriesDao.getTvSeriesById(movieId)
    }

    /**
     *
     */
    fun searchRemotelyByName(query: String): Observable<List<TvSeries>> {
        return service.searchTvSeries(query)
                .map(TvSeriesResponse::results)
    }

    /**
     *
     */
    fun loadVideosByTvSeriesID(tvSeriesId: Long): LiveData<Resource<List<Video>>> {
        val cachedResource = videosCache.get(tvSeriesId)

        if (cachedResource != null) {
            videosLiveData.postValue(cachedResource)
        } else {
            service.getTvSeriesVideos(tvSeriesId)
                    .doOnSubscribe { videosLiveData.postValue(Resource.loading(null)) }
                    .map(VideosResponse::results)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.computation())
                    .subscribe(
                            // onSuccess
                            { videos -> videosLiveData.postValue(Resource.success(videos))
                                videosCache.put(tvSeriesId, Resource.success(videos))
                            },
                            // onError
                            { t -> videosLiveData.postValue(Resource.error(t, null)) }
                    )
        }

        return videosLiveData
    }

    companion object {

        @Volatile
        private var sInstance: TvSeriesRepository? = null

        /**
         * Get shared instance using the singleton pattern.
         *
         * @return the shared instance of [TvSeriesRepository].
         */
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): TvSeriesRepository {
            if (sInstance == null) {
                val database = AppDatabase.getInstance(context.applicationContext)
                sInstance = TvSeriesRepository(
                        WebServiceUtils.getWebService(),
                        database,
                        database.tvSeriesDao())
            }

            return sInstance!!
        }
    }
}
