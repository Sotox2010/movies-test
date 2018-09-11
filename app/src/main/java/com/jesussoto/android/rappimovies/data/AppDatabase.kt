package com.jesussoto.android.rappimovies.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.jesussoto.android.rappimovies.data.converter.DateConverter
import com.jesussoto.android.rappimovies.data.dao.MovieDao
import com.jesussoto.android.rappimovies.data.dao.TvSeriesDao
import com.jesussoto.android.rappimovies.data.entity.Movie
import com.jesussoto.android.rappimovies.data.entity.TvSeries

@Database(
    entities = [
        Movie::class,
        TvSeries::class
    ],
    version = 1
)
@TypeConverters(
    DateConverter::class
)
abstract class AppDatabase: RoomDatabase() {

    companion object {

        @Volatile
        private var sInstance: AppDatabase? = null

        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            if (sInstance == null) {
                sInstance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "rappi_movies.db")
                        .fallbackToDestructiveMigration()
                        .build()
            }
            return sInstance!!
        }
    }

    abstract fun movieDao(): MovieDao

    abstract fun tvSeriesDao(): TvSeriesDao

}
