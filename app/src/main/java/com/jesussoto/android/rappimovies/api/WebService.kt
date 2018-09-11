package com.jesussoto.android.rappimovies.api

import com.jesussoto.android.rappimovies.api.response.MoviesResponse
import com.jesussoto.android.rappimovies.api.response.TvSeriesResponse
import com.jesussoto.android.rappimovies.api.response.VideosResponse
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WebService {

    @GET(PATH_MOVIE_POPULAR)
    fun getPopularMovies(@Query(PARAM_PAGE) page: Int = 1): Single<MoviesResponse>

    @GET(PATH_MOVIE_TOP_RATED)
    fun getTopRatedMovies(@Query(PARAM_PAGE) page: Int = 1): Single<MoviesResponse>

    @GET(PATH_MOVIE_UPCOMING)
    fun getUpcomingMovies(@Query(PARAM_PAGE) page: Int = 1): Single<MoviesResponse>

    @GET(PATH_TV_POPULAR)
    fun getPopularTvSeries(@Query(PARAM_PAGE) page: Int = 1): Single<TvSeriesResponse>

    @GET(PATH_TV_TOP_RATED)
    fun getTopRatedTvSeries(@Query(PARAM_PAGE) page: Int = 1): Single<TvSeriesResponse>

    @GET(PATH_SEARCH_MOVIES)
    fun searchMovies(@Query(PARAM_QUERY) query: String): Observable<MoviesResponse>

    @GET(PATH_SEARCH_TV)
    fun searchTvSeries(@Query(PARAM_QUERY) query: String): Observable<TvSeriesResponse>

    @GET(PATH_MOVIE_VIDEOS)
    fun getMovieVideos(@Path(PARAM_MOVIE_ID) movieId: Long): Single<VideosResponse>

    @GET(PATH_TV_VIDEOS)
    fun getTvSeriesVideos(@Path(PARAM_TV_ID) tvSeriesId: Long): Single<VideosResponse>

    companion object {
        /**
         * Paths for TheMovieDB web service.
         */
        const val PATH_MOVIE_POPULAR = "movie/popular"
        const val PATH_MOVIE_TOP_RATED = "movie/top_rated"
        const val PATH_MOVIE_UPCOMING = "movie/upcoming"
        const val PATH_TV_POPULAR = "tv/popular"
        const val PATH_TV_TOP_RATED = "tv/top_rated"
        const val PATH_POSTER_SIZE = "w185"
        const val PATH_BACKDROP_SIZE = "w500"
        const val PATH_SEARCH_MOVIES = "search/movie"
        const val PATH_SEARCH_TV = "search/tv"
        const val PATH_MOVIE_VIDEOS= "movie/{movie_id}/videos"
        const val PATH_TV_VIDEOS = "tv/{tv_id}/videos"

        /**
         * Query params for the web service.
         */
        const val PARAM_API_KEY = "api_key"
        const val PARAM_QUERY = "query"
        const val PARAM_MOVIE_ID = "movie_id"
        const val PARAM_TV_ID = "tv_id"
        const val PARAM_PAGE = "page"
    }
}
