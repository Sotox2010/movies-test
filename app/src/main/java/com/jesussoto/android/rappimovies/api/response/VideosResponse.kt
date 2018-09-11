package com.jesussoto.android.rappimovies.api.response

import com.google.gson.annotations.SerializedName
import com.jesussoto.android.rappimovies.api.model.Video

data class VideosResponse(

    @SerializedName("id")
    val id: Int,

    @SerializedName("results")
    val results: List<Video>
)