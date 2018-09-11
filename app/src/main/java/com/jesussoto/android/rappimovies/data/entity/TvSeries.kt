package com.jesussoto.android.rappimovies.data.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(
    tableName = "tv_series",
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["category"])
    ],
    primaryKeys = ["id"]
)
data class TvSeries(

    @field:SerializedName("id")
    @ColumnInfo(name = "id")
    val id: Long,

    @field:SerializedName("name")
    @ColumnInfo(name = "name")
    val name: String,

    @field:SerializedName("original_name")
    @ColumnInfo(name = "original_name")
    val originalName: String,

    @field:SerializedName("overview")
    @ColumnInfo(name = "overview")
    val overview: String,

    @field:SerializedName("poster_path")
    @ColumnInfo(name = "poster_path")
    val posterPath: String? = null,

    @field:SerializedName("backdrop_path")
    @ColumnInfo(name = "backdrop_path")
    val backdropPath: String? = null,

    @field:SerializedName("popularity")
    @ColumnInfo(name = "popularity")
    val popularity: Float,

    @field:SerializedName("vote_average")
    @ColumnInfo(name = "vote_average")
    val voteAverage: Float,

    @field:SerializedName("vote_count")
    @ColumnInfo(name = "vote_count")
    val voteCount: Int,

    @field:SerializedName("first_air_date")
    @ColumnInfo(name = "first_air_date")
    val firstAirDate: Date?,

    @field:SerializedName("original_language")
    @ColumnInfo(name = "original_language")
    val originalLanguage: String,

    var category: String = "popular"
)
