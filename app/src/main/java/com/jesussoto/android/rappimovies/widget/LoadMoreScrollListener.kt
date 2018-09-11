package com.jesussoto.android.rappimovies.widget

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView

/**
 * Custom scroll listener to listen to events when the scroll hits the bottom.
 */
class LoadMoreScrollListener(private val onLoadMore: () -> Unit) :
        RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (recyclerView == null) return

        val layoutManager = recyclerView.layoutManager as GridLayoutManager
        val totalItemCount = layoutManager.itemCount
        val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
        if (totalItemCount <= (lastVisibleItem + 3)) {
            onLoadMore.invoke()
        }
    }
}