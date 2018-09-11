package com.jesussoto.android.rappimovies.offlinesearch

import com.jesussoto.android.rappimovies.movies.FilterType
import com.jesussoto.android.rappimovies.movies.SourceType

data class SearchParams(
    val source: SourceType,

    val category: FilterType,

    val query: String
)