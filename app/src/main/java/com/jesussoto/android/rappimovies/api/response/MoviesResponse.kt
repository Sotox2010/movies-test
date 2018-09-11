package com.jesussoto.android.rappimovies.api.response

import com.google.gson.annotations.SerializedName
import com.jesussoto.android.rappimovies.data.entity.Movie

data class MoviesResponse(

    @field:SerializedName("page")
    val page: Int, // 1,

    @field:SerializedName("total_results")
    val totalResults: Int, // 19814,

    @field:SerializedName("total_pages")
    val totalPages: Int?, // 991,

    @field:SerializedName("results")
    val results: List<Movie>
)
