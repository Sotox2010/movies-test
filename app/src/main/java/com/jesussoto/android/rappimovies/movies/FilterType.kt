package com.jesussoto.android.rappimovies.movies

/**
 * Used with the filter spinner in the tvSeries/tv-series grid.
 */
enum class FilterType constructor(val value: Int) {
    /**
     * Filters by popular tvSeries/tv-series in decreasing order.
     */
    POPULAR(0),

    /**
     * Filters by top-rated tvSeries/tv-series in decreasing order.
     */
    TOP_RATED(1),

    /**
     * Filters by upcoming tvSeries/tv-series in decreasing order.
     */
    UPCOMING(2);


    companion object {

        @JvmStatic
        fun fromValue(value: Int): FilterType {
            return when (value) {
                0 -> POPULAR
                1 -> TOP_RATED
                2 -> UPCOMING
                else -> throw IllegalArgumentException("Value out of range: $value")
            }
        }
    }
}
