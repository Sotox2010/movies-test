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
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class MoviesRepository(
        private val service: WebService,
        private val database: AppDatabase,
        private val moviesDao: MovieDao) {

    // Simple in-memory cache for popular tvSeries.
    private val popularMoviesLiveData: MutableLiveData<Resource<List<Movie>>> = MutableLiveData()

    // Simple in-memory cache for top rated tvSeries.
    private val topRatedMoviesLiveData: MutableLiveData<Resource<List<Movie>>> = MutableLiveData()

    // Simple in-memory cache for upcoming tvSeries.
    private val upcomingMoviesLiveData: MutableLiveData<Resource<List<Movie>>> = MutableLiveData()

    // Simple in-memory cache for videos.
    private val videosLiveData: MutableLiveData<Resource<List<Video>>> = MutableLiveData()

    // 5MiB cache for videos.
    private val videosCache = LruCache<Long, Resource<List<Video>>>(5 * 1024 * 1024)

    /**
     * Load top-rated tvSeries from the network if no previous fetch occurred, or return the
     * cached tvSeries.
     *
     * @return [LiveData] with the result of the most popular tvSeries network fetch.
     */
    fun loadPopularMovies(): LiveData<Resource<List<Movie>>> {
        Single.concat(moviesDao.getPopularMovies(), fetchPopularMovies())
                .doOnSubscribe { popularMoviesLiveData.postValue(Resource.loading(null)) }
                .filter { !it.isEmpty() }
                .firstOrError()
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .subscribe(
                        // onSuccess
                        { popularMoviesLiveData.postValue(Resource.success(it)) },

                        //onError
                        { popularMoviesLiveData.postValue(Resource.error(it, null)) }
                )

        return popularMoviesLiveData
    }

    /**
     * Load top-rated tvSeries from the network if no previous fetch occurred, or return the
     * cached tvSeries.
     *
     * @return [LiveData] with the result of the top-rated tvSeries network fetch.
     */
    fun loadTopRatedMovies(): LiveData<Resource<List<Movie>>> {
        Single.concat(moviesDao.getTopRatedMovies(), fetchTopRatedMovies())
                .doOnSubscribe { topRatedMoviesLiveData.postValue(Resource.loading(null)) }
                .filter { !it.isEmpty() }
                .firstOrError()
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .subscribe(
                        // onSuccess
                        { topRatedMoviesLiveData.postValue(Resource.success(it)) },

                        //onError
                        { topRatedMoviesLiveData.postValue(Resource.error(it, null)) }
                )

        return topRatedMoviesLiveData
    }

    /**
     * Load top-rated tvSeries from the network if no previous fetch occurred, or return the
     * cached tvSeries.
     *
     * @return [LiveData] with the result of the top-rated tvSeries network fetch.
     */
    fun loadUpcomingMovies(): LiveData<Resource<List<Movie>>> {
        Single.concat(moviesDao.getUpcomingMovies(), fetchUpcomingMovies())
                .filter { !it.isEmpty() }
                .firstOrError()
                .doOnSubscribe { upcomingMoviesLiveData.postValue(Resource.loading(null)) }
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .subscribe(
                        // onSuccess
                        { upcomingMoviesLiveData.postValue(Resource.success(it)) },

                        //onError
                        { upcomingMoviesLiveData.postValue(Resource.error(it, null)) }
                )

        return upcomingMoviesLiveData
    }

    /**
     * Forces refresh of the popular tvSeries from network.
     */
    /*fun refreshPopularMovies() {
        fetchPopularMovies(popularMoviesLiveData)
    }

    /**
     * Forces refresh of the top-rated tvSeries from network.
     */
    fun refreshTopRatedMovies() {
        fetchTopRatedMovies(topRatedMoviesLiveData)
    }*/

    /**
     * Core method for fetch popular tvSeries from the network using RxJava, placed on a separate
     * method for re-usability on first load or forced refresh.
     */
    private fun fetchPopularMovies(): Single<List<Movie>> {
        return service.getPopularMovies()
                .map(MoviesResponse::results)
                .flatMapObservable { Observable.fromIterable(it) }
                .doOnNext { it.category = "popular" }
                .toList()
                .flatMapCompletable(this::saveMovies)
                .andThen(moviesDao.getPopularMovies())
    }

    /**
     * Core method for fetch top-rated tvSeries from the network using RxJava, placed on a separate
     * method for re-usability on first load or forced refresh.
     */
    private fun fetchTopRatedMovies(): Single<List<Movie>> {
        return service.getTopRatedMovies()
                .map(MoviesResponse::results)
                .flatMapObservable { Observable.fromIterable(it) }
                .doOnNext { it.category = "top-rated" }
                .toList()
                .flatMapCompletable(this::saveMovies)
                .andThen(moviesDao.getTopRatedMovies())
    }

    /**
     * Core method for fetch upcoming tvSeries from the network using RxJava, placed on a separate
     * method for re-usability on first load or forced refresh.
     */
    private fun fetchUpcomingMovies(): Single<List<Movie>> {
        return service.getUpcomingMovies()
                .map(MoviesResponse::results)
                .flatMapObservable { Observable.fromIterable(it) }
                .doOnNext { it.category = "upcoming" }
                .toList()
                .flatMapCompletable(this::saveMovies)
                .andThen(moviesDao.getUpcomingMovies())
    }

    /**
     * Persist the list of movies to the database so they are available offline.
     */
    private fun saveMovies(movies: List<Movie>): Completable {
        return Completable.fromAction {
//            database.beginTransaction()
//            try {
                moviesDao.insertMovies(movies)
//                database.setTransactionSuccessful()
//            } catch (ex: Exception) {
//                database.endTransaction()
//            }
        }
    }

    /**
     *
     */
    fun loadMovieById(movieId: Long): Single<Movie> {
        return moviesDao.getMovieById(movieId)
    }

    /**
     *
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
     *
     */
    fun searchRemotelyByName(query: String): Observable<List<Movie>> {
        return service.searchMovies(query)
                .map(MoviesResponse::results)
    }

    /**
     *
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
