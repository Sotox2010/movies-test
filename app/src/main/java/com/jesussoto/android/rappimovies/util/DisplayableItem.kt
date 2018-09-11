package com.jesussoto.android.rappimovies.util

import android.os.Parcel
import android.os.Parcelable
import com.jesussoto.android.rappimovies.data.entity.Movie
import com.jesussoto.android.rappimovies.data.entity.TvSeries
import com.jesussoto.android.rappimovies.movies.SourceType
import java.util.*

/**
 * Handy class to display detailed information either for a [Movie] or a [TvSeries]
 */
data class DisplayableItem(

    val id: Long,

    val title: String,

    val originalTitle: String,

    val overview: String,

    val releaseDate: Date?,

    val voteAverage: Float,

    val posterPath: String? = null,

    val backdropPath: String? = null,

    val source: SourceType

): Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readLong().let { if (it != -1L) Date(it) else null},
            parcel.readFloat(),
            parcel.readString(),
            parcel.readString(),
            SourceType.fromValue(parcel.readInt()))

    constructor(movie: Movie): this(
        id = movie.id,
        title = movie.title,
        originalTitle = movie.originalTitle,
        overview = movie.overview,
        releaseDate = movie.releaseDate,
        voteAverage = movie.voteAverage,
        posterPath = movie.posterPath,
        backdropPath = movie.backdropPath,
        source = SourceType.MOVIES
    )

    constructor(tvSeries: TvSeries): this(
        id = tvSeries.id,
        title = tvSeries.name,
        originalTitle = tvSeries.originalName,
        overview = tvSeries.overview,
        releaseDate = tvSeries.firstAirDate,
        voteAverage = tvSeries.voteAverage,
        posterPath = tvSeries.posterPath,
        backdropPath = tvSeries.backdropPath,
        source = SourceType.TV_SERIES
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(originalTitle)
        parcel.writeString(overview)
        parcel.writeLong(releaseDate?.time ?: -1L)
        parcel.writeFloat(voteAverage)
        parcel.writeString(posterPath)
        parcel.writeString(backdropPath)
        parcel.writeInt(source.value)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DisplayableItem> {
        override fun createFromParcel(parcel: Parcel): DisplayableItem {
            return DisplayableItem(parcel)
        }

        override fun newArray(size: Int): Array<DisplayableItem?> {
            return arrayOfNulls(size)
        }
    }
}
