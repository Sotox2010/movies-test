package com.jesussoto.android.rappimovies.data.dao

import android.arch.persistence.room.*
import com.jesussoto.android.rappimovies.data.entity.Movie
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface MovieDao {

    @Query("SELECT * FROM movie;")
    fun getAllMovies(): Flowable<List<Movie>>

    @Query("SELECT * FROM movie WHERE id = :id;")
    fun getMovieById(id: Long): Single<Movie>

    @Query("SELECT * FROM movie WHERE category = 'popular' ORDER BY popularity DESC;")
    fun getPopularMovies(): Single<List<Movie>>

    @Query("""
        SELECT *
        FROM movie
        WHERE category = 'popular'
        ORDER BY popularity DESC
        LIMIT :pageSize
        OFFSET :offset;
    """)
    fun getPopularMoviesByPage(offset: Int, pageSize: Int): List<Movie>

    @Query("SELECT * FROM movie WHERE category = 'top-rated' ORDER BY vote_average DESC;")
    fun getTopRatedMovies(): Single<List<Movie>>

    @Query("""
        SELECT *
        FROM movie
        WHERE category = 'top-rated'
        ORDER BY popularity DESC
        LIMIT :pageSize
        OFFSET :offset;
    """)
    fun getTopRatedMoviesByPage(offset: Int, pageSize: Int): List<Movie>

    @Query("SELECT * FROM movie WHERE category = 'upcoming' ORDER BY id DESC;")
    fun getUpcomingMovies(): Single<List<Movie>>

    @Query("""
        SELECT *
        FROM movie
        WHERE category = 'upcoming'
        ORDER BY popularity DESC
        LIMIT :pageSize
        OFFSET :offset;
    """)
    fun getUpcomingMoviesByPage(offset: Int, pageSize: Int): List<Movie>

    @Query("SELECT * FROM movie WHERE title LIKE :query AND category = :category;")
    fun searchByNameAndCategory(query: String, category: String): Flowable<List<Movie>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMovies(vararg movies: Movie)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMovies(movies: List<Movie>)

    @Delete
    fun deleteMovie(movie: Movie)
}