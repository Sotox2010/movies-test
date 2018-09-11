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
import com.jesussoto.android.rappimovies.api.response.MoviesResponse
import com.jesussoto.android.rappimovies.api.response.VideosResponse
import com.jesussoto.android.rappimovies.data.AppDatabase
import com.jesussoto.android.rappimovies.data.dao.MovieDao
import com.jesussoto.android.rappimovies.data.entity.Movie
import com.jesussoto.android.rappimovies.movies.FilterType
import com.jesussoto.android.rappimovies.util.Utils
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.Executors

class MoviesRepository(
        private val service: WebService,
        private val database: AppDatabase,
        private val moviesDao: MovieDao) {

    // Simple in-memory cache for videos.
    private val videosLiveData: MutableLiveData<Resource<List<Video>>> = MutableLiveData()

    // 5MiB cache for videos.
    private val videosCache = LruCache<Long, Resource<List<Video>>>(5 * 1024 * 1024)

    // Executor to move off the Ui thread.
    private val ioExecutor = Executors.newFixedThreadPool(3)

    /**
     * Load most popular movies from the network if no previous fetch occurred, or return the
     * cached tvSeries.
     *
     * @return [LiveData] with the result of the most popular movies network fetch by page.
     */
    fun loadPopularMoviesByPage(page: Int, listCache: ArrayList<Movie>): LiveData<Resource<List<Movie>>> {
        val liveData = MutableLiveData<Resource<List<Movie>>>()
        val offset = (page - 1) * PAGE_SIZE
        val category = "popular"

        val loadNextPageTask = object : LoadNextPageTask<Movie, MoviesResponse>(
                liveData, offset, PAGE_SIZE, listCache) {

            override fun getItemsByPage(offset: Int, pageSize: Int): List<Movie> {
                return moviesDao.getPopularMoviesByPage(offset, pageSize)
            }

            override fun getServiceSingleCall(): Single<MoviesResponse> {
                return service.getPopularMovies(page)
            }

            override fun saveToDatabase(newItems: List<Movie>, shouldReplace: Boolean) {
                saveMoviesToDb(newItems, category, shouldReplace)
            }

            override fun shouldFetch(prefetchItems: List<Movie>): Boolean {
                var shouldFetch = false
                if (page == 1 && !prefetchItems.isEmpty()) {
                    val lastFreshFetch = prefetchItems[0].createdAt
                    shouldFetch = Utils.isOlderThanOneHour(lastFreshFetch)
                }
                return shouldFetch
            }

            override fun setCategory(item: Movie) {
                item.createdAt = Date()
                item.category = category
            }

            override fun getResults(response: MoviesResponse): List<Movie> = response.results
        }

        ioExecutor.execute(loadNextPageTask)
        return liveData
    }

    /**
     * Load top-rated movies from the network if no previous fetch occurred, or return the
     * cached tvSeries.
     *
     * @return [LiveData] with the result of the top-rated movies network fetch by page.
     */
    fun loadTopRatedMoviesByPage(page: Int, listCache: ArrayList<Movie>): LiveData<Resource<List<Movie>>> {
        val liveData = MutableLiveData<Resource<List<Movie>>>()
        val offset = (page - 1) * PAGE_SIZE
        val category = "top-rated"

        val loadNextPageTask = object : LoadNextPageTask<Movie, MoviesResponse>(
                liveData, offset, PAGE_SIZE, listCache) {

            override fun getItemsByPage(offset: Int, pageSize: Int): List<Movie> {
                return moviesDao.getTopRatedMoviesByPage(offset, pageSize)
            }

            override fun getServiceSingleCall(): Single<MoviesResponse> {
                return service.getTopRatedMovies(page)
            }

            override fun saveToDatabase(newItems: List<Movie>, shouldReplace: Boolean) {
                saveMoviesToDb(newItems, category, shouldReplace)
            }

            override fun shouldFetch(prefetchItems: List<Movie>): Boolean {
                var shouldFetch = false
                if (page == 1 && !prefetchItems.isEmpty()) {
                    val lastFreshFetch = prefetchItems[0].createdAt
                    shouldFetch = Utils.isOlderThanOneHour(lastFreshFetch)
                }
                return shouldFetch
            }

            override fun setCategory(item: Movie) {
                item.createdAt = Date()
                item.category = category
            }

            override fun getResults(response: MoviesResponse): List<Movie> = response.results
        }

        ioExecutor.execute(loadNextPageTask)
        return liveData
    }

    /**
     * Load upcoming movies from the network if no previous fetch occurred, or return the
     * cached movies.
     *
     * @return [LiveData] with the result of the upcoming movies network fetch by page.
     */
    fun loadUpcomingMoviesByPage(page: Int, listCache: ArrayList<Movie>): LiveData<Resource<List<Movie>>> {
        val liveData = MutableLiveData<Resource<List<Movie>>>()
        val offset = (page - 1) * PAGE_SIZE
        val category = "upcoming"

        val loadNextPageTask = object : LoadNextPageTask<Movie, MoviesResponse>(
                liveData, offset, PAGE_SIZE, listCache) {

            override fun getItemsByPage(offset: Int, pageSize: Int): List<Movie> {
                return moviesDao.getUpcomingMoviesByPage(offset, pageSize)
            }

            override fun getServiceSingleCall(): Single<MoviesResponse> {
                return service.getUpcomingMovies(page)
            }

            override fun saveToDatabase(newItems: List<Movie>, shouldReplace: Boolean) {
                saveMoviesToDb(newItems, category, shouldReplace)
            }

            override fun shouldFetch(prefetchItems: List<Movie>): Boolean {
                var shouldFetch = false
                if (page == 1 && !prefetchItems.isEmpty()) {
                    val lastFreshFetch = prefetchItems[0].createdAt
                    shouldFetch = Utils.isOlderThanOneHour(lastFreshFetch)
                }
                return shouldFetch
            }

            override fun setCategory(item: Movie) {
                item.createdAt = Date()
                item.category = category
            }

            override fun getResults(response: MoviesResponse): List<Movie> = response.results
        }

        ioExecutor.execute(loadNextPageTask)
        return liveData
    }

   /**
    * Persist the list of movies to the database so they are available offline.
    */
    private fun saveMoviesToDb(movies: List<Movie>, category: String, shouldReplace: Boolean) {
        database.runInTransaction {
            if (shouldReplace) {
                moviesDao.deleteMovieByCategory(category)
            }
            moviesDao.insertMovies(movies)
        }
    }

    /**
     * Load a single movie by its id.
     */
    fun loadMovieById(movieId: Long): Single<Movie> {
        return moviesDao.getMovieById(movieId)
    }

    /**
     * Search in the local database for matching movies by title.
     */
    fun searchByNameAndCategory(query: String, category: FilterType): Flowable<List<Movie>> {
        val categoryString = when (category) {
            FilterType.POPULAR -> "popular"
            FilterType.TOP_RATED -> "top-rated"
            FilterType.UPCOMING -> "upcoming"
        }

        if (TextUtils.isEmpty(query)) {
            return Flowable.just(ArrayList())
        }

        return moviesDao.searchByNameAndCategory("%$query%", categoryString)
    }

    /**
     * Search movies from the network using TheMovieDB web service, filtering them by title.
     */
    fun searchRemotelyByName(query: String): Observable<List<Movie>> {
        return service.searchMovies(query)
                .map(MoviesResponse::results)
    }

    /**
     * Fetch videos for a specific movie by id.
     */
    fun loadVideosByMovieId(movieId: Long): LiveData<Resource<List<Video>>>{
        val cachedResource = videosCache.get(movieId)

        if (cachedResource != null) {
            videosLiveData.postValue(cachedResource)
        } else {
            service.getMovieVideos(movieId)
                    .doOnSubscribe { videosLiveData.postValue(Resource.loading(null)) }
                    .map(VideosResponse::results)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.computation())
                    .subscribe(
                            // onSuccess
                            { videos -> videosLiveData.postValue(Resource.success(videos))
                                videosCache.put(movieId, Resource.success(videos))
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
        private var sInstance: MoviesRepository? = null

        /**
         * Get shared instance using the singleton pattern.
         *
         * @return the shared instance of [MoviesRepository].
         */
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): MoviesRepository {
            if (sInstance == null) {
                val database = AppDatabase.getInstance(context.applicationContext)
                sInstance = MoviesRepository(
                        WebServiceUtils.getWebService(),
                        database,
                        database.movieDao())
            }

            return sInstance!!
        }
    }
}
