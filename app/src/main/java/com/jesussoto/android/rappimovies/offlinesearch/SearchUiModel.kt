package com.jesussoto.android.rappimovies.offlinesearch

import com.jesussoto.android.rappimovies.util.DisplayableItem

data class SearchUiModel(

    val results: List<DisplayableItem>?,

    val emptyMessage: String? = null,

    val isLoadingVisible: Boolean = false

)
