package com.jesussoto.android.rappimovies.movies

/**
 * Used with the filter spinner in the tvSeries/tv-series grid.
 */
enum class SourceType constructor(val value: Int) {
    /**
     * Filters by tvSeries.
     */
    MOVIES(0),

    /**
     * Filters by tv-series.
     */
    TV_SERIES(1);

    companion object {

        @JvmStatic
        fun fromValue(value: Int): SourceType {
            return when (value) {
                0 -> SourceType.MOVIES
                1 -> SourceType.TV_SERIES
                else -> throw IllegalArgumentException("Value out of range: $value")
            }
        }
    }
}
