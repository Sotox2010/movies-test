package com.jesussoto.android.rappimovies.moviedetail

import com.jesussoto.android.rappimovies.api.model.Video

data class ItemVideosUiModel(

    val videos: List<Video>?,

    val isProgressVisible: Boolean,

    val emptyMessage: String?

)