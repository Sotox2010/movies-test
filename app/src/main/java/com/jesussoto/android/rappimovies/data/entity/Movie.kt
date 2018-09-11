package com.jesussoto.android.rappimovies.data.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(
    tableName = "movie",
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["category"])
    ],
    primaryKeys = ["id"]
)
data class Movie(

    @SerializedName("id")
    @ColumnInfo(name = "id")
    val id: Long,

    @SerializedName("title")
    @ColumnInfo(name = "title")
    val title: String,

    @SerializedName("overview")
    @ColumnInfo(name = "overview")
    val overview: String,

    @SerializedName("release_date")
    @ColumnInfo(name = "release_date")
    val releaseDate: Date?,

    @field:SerializedName("vote_count")
    @ColumnInfo(name = "vote_count")
    val voteCount: Int,

    @SerializedName("video")
    @ColumnInfo(name = "video")
    val video: Boolean,

    @SerializedName("vote_average")
    @ColumnInfo(name = "vote_average")
    val voteAverage: Float,

    @SerializedName("popularity")
    @ColumnInfo(name = "popularity")
    val popularity: Float,

    @SerializedName("poster_path")
    @ColumnInfo(name = "poster_path")
    val posterPath: String?,

    @SerializedName("backdrop_path")
    @ColumnInfo(name = "backdrop_path")
    val backdropPath: String?,

    @SerializedName("original_language")
    @ColumnInfo(name = "original_language")
    val originalLanguage: String,

    @SerializedName("original_title")
    @ColumnInfo(name = "original_title")
    val originalTitle: String,

    var category: String = "popular"

)
