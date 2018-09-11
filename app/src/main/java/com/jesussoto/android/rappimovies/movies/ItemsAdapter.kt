package com.jesussoto.android.rappimovies.movies

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.jesussoto.android.rappimovies.R
import com.jesussoto.android.rappimovies.api.WebServiceUtils
import com.jesussoto.android.rappimovies.util.DisplayableItem
import com.squareup.picasso.Picasso
import java.util.*

internal class ItemsAdapter(
        private var items: List<DisplayableItem>?): RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {

    private var itemTappedListener: ((item: DisplayableItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_movie, parent, false)

        val holder = ViewHolder(itemView)

        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                itemTappedListener?.invoke(items!![position])
            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items?.get(position) ?: return
        holder.bindMovie(item)
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    fun replaceData(items: List<DisplayableItem>?) {
        this.items = items
        notifyDataSetChanged()
    }

    fun setOnItemTappedListener(listener: ((item: DisplayableItem) -> Unit)?) {
        itemTappedListener = listener
    }

    internal class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val posterView: ImageView = itemView.findViewById(R.id.movie_poster_view)
        private val titleView: TextView = itemView.findViewById(R.id.movie_title_view)
        private val ratingView: TextView = itemView.findViewById(R.id.movie_rating_view)

        fun bindMovie(item: DisplayableItem) {
            if (item.posterPath != null) {
                Picasso.with(itemView.context)
                        .load(WebServiceUtils.buildMoviePosterUri(item.posterPath))
                        .placeholder(R.drawable.image_placeholder)
                        .into(posterView)
            }

            titleView.text = item.title
            ratingView.text = String.format(Locale.US, "%.1f", item.voteAverage)
        }
    }
}
