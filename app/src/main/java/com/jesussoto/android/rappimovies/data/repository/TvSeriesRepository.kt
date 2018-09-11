package com.jesussoto.android.rappimovies.data.repository

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
import com.jesussoto.android.rappimovies.util.Utils
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.Executors

class TvSeriesRepository(
        private val service: WebService,
        private val database: AppDatabase,
        private val tvSeriesDao: TvSeriesDao) {

    // Simple in-memory cache for videos.
    private val videosLiveData: MutableLiveData<Resource<List<Video>>> = MutableLiveData()

    // 5MiB cache for videos.
    private val videosCache = LruCache<Long, Resource<List<Video>>>(5 * 1024 * 1024)

    // Executor to move off the Ui thread.
    private val ioExecutor = Executors.newFixedThreadPool(3)

    /**
     * Load most popular tv-series from the network if no previous fetch occurred, or return the
     * cached tv-series.
     */
    fun loadPopularTvSeriesByPage(page: Int, listCache: ArrayList<TvSeries>): LiveData<Resource<List<TvSeries>>> {
        val liveData = MutableLiveData<Resource<List<TvSeries>>>()
        val offset = (page - 1) * PAGE_SIZE
        val category = "popular"

        val loadNextPageTask = object : LoadNextPageTask<TvSeries, TvSeriesResponse>(
                liveData, offset, PAGE_SIZE, listCache) {

            override fun getItemsByPage(offset: Int, pageSize: Int): List<TvSeries> {
                return tvSeriesDao.getPopularTvSeriesByPage(offset, pageSize)
            }

            override fun getServiceSingleCall(): Single<TvSeriesResponse> {
                return service.getPopularTvSeries(page)
            }

            override fun shouldFetch(prefetchItems: List<TvSeries>): Boolean {
                var shouldFetch = false
                if (page == 1 && !prefetchItems.isEmpty()) {
                    val lastFreshFetch = prefetchItems[0].createdAt
                    shouldFetch = Utils.isOlderThanOneHour(lastFreshFetch)
                }
                return shouldFetch
            }

            override fun saveToDatabase(newItems: List<TvSeries>, shouldReplace: Boolean) {
                saveTvSeriesToDb(newItems, category, shouldReplace)
            }

            override fun setCategory(item: TvSeries) {
                item.createdAt = Date()
                item.category = category
            }

            override fun getResults(response: TvSeriesResponse): List<TvSeries> = response.results
        }

        ioExecutor.execute(loadNextPageTask)
        return liveData
    }

    /**
     * Load top-rated tv-series from the network if no previous fetch occurred, or return the
     * cached tv-series.
     */
    fun loadTopRatedTvSeriesByPage(page: Int, listCache: ArrayList<TvSeries>): LiveData<Resource<List<TvSeries>>> {
        val liveData = MutableLiveData<Resource<List<TvSeries>>>()
        val offset = (page - 1) * PAGE_SIZE
        val category = "top-rated"

        val loadNextPageTask = object : LoadNextPageTask<TvSeries, TvSeriesResponse>(
                liveData, offset, PAGE_SIZE, listCache) {

            override fun getItemsByPage(offset: Int, pageSize: Int): List<TvSeries> {
                return tvSeriesDao.getTopRatedTvSeriesByPage(offset, pageSize)
            }

            override fun getServiceSingleCall(): Single<TvSeriesResponse> {
                return service.getTopRatedTvSeries(page)
            }

            override fun shouldFetch(prefetchItems: List<TvSeries>): Boolean {
                var shouldFetch = false
                if (page == 1 && !prefetchItems.isEmpty()) {
                    val lastFreshFetch = prefetchItems[0].createdAt
                    shouldFetch = Utils.isOlderThanOneHour(lastFreshFetch)
                }
                return shouldFetch
            }

            override fun saveToDatabase(newItems: List<TvSeries>, shouldReplace: Boolean) {
                saveTvSeriesToDb(newItems, category, shouldReplace)
            }

            override fun setCategory(item: TvSeries) {
                item.createdAt = Date()
                item.category = category
            }

            override fun getResults(response: TvSeriesResponse): List<TvSeries> = response.results
        }

        ioExecutor.execute(loadNextPageTask)
        return liveData
    }


    /**
     * Persist tv-series in the database as a caching mechanism.
     */
    private fun saveTvSeriesToDb(tvSeries: List<TvSeries>, category: String, shouldReplace: Boolean) {
        database.runInTransaction {
            database.runInTransaction {
                if (shouldReplace) {
                    tvSeriesDao.deleteTvSeriesByCategory(category)
                }
                tvSeriesDao.insertTvSeries(tvSeries)
            }
        }
    }

    /**
     * Search in the local database for matching tv-series by title.
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
     * Load a single movie by its id.
     */
    fun loadTvSeriesById(movieId: Long): Single<TvSeries> {
        return tvSeriesDao.getTvSeriesById(movieId)
    }

    /**
     * Search tv-series from the network using TheMovieDB web service, filtering them by title.
     */
    fun searchRemotelyByName(query: String): Observable<List<TvSeries>> {
        return service.searchTvSeries(query)
                .map(TvSeriesResponse::results)
    }

    /**
     * Fetch videos for a specific tv-series by id.
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
        private const val PAGE_SIZE = 20

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
