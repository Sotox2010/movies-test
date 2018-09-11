package com.jesussoto.android.rappimovies.data.dao

import android.arch.persistence.room.*
import com.jesussoto.android.rappimovies.data.entity.TvSeries
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface TvSeriesDao {

    @Query("SELECT * FROM tv_series;")
    fun getAllTvSeries(): Flowable<List<TvSeries>>

    @Query("SELECT * FROM tv_series WHERE id = :id;")
    fun getTvSeriesById(id: Long): Single<TvSeries>

    @Query("SELECT * FROM tv_series WHERE category = 'popular' ORDER BY popularity DESC;")
    fun getPopularTvSeries(): Single<List<TvSeries>>

    @Query("""
        SELECT *
        FROM tv_series
        WHERE category = 'popular'
        ORDER BY popularity DESC
        LIMIT :pageSize
        OFFSET :offset;
    """)
    fun getPopularTvSeriesByPage(offset: Int, pageSize: Int): List<TvSeries>

    @Query("SELECT * FROM tv_series WHERE category = 'top-rated' ORDER BY vote_average DESC;")
    fun getTopRatedTvSeries(): Single<List<TvSeries>>

    @Query("""
        SELECT *
        FROM tv_series
        WHERE category = 'top-rated'
        ORDER BY popularity DESC
        LIMIT :pageSize
        OFFSET :offset;
    """)
    fun getTopRatedTvSeriesByPage(offset: Int, pageSize: Int): List<TvSeries>


    @Query("SELECT * FROM tv_series WHERE name LIKE :query AND category = :category;")
    fun searchByNameAndCategory(query: String, category: String): Flowable<List<TvSeries>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTvSeries(vararg tvSeriesList: TvSeries)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTvSeries(tvSeriesList: List<TvSeries>)

    @Delete
    fun deleteTvSeries(tvSeries: TvSeries)
}
