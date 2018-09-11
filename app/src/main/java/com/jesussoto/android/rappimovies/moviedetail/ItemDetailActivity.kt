package com.jesussoto.android.rappimovies.moviedetail

import android.animation.ObjectAnimator
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.graphics.Palette
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.jesussoto.android.rappimovies.R
import com.jesussoto.android.rappimovies.api.WebServiceUtils
import com.jesussoto.android.rappimovies.api.model.Video
import com.jesussoto.android.rappimovies.movies.SourceType
import com.jesussoto.android.rappimovies.util.DisplayableItem
import com.jesussoto.android.rappimovies.widget.AlwaysEnterToolbarScrollListener
import com.jesussoto.android.rappimovies.widget.SynchronizedScrollView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.activity_movie_detail.*
import java.text.SimpleDateFormat
import java.util.*

class ItemDetailActivity : AppCompatActivity(), SynchronizedScrollView.OnScrollListener {

    @ColorInt
    private var toolbarColor = Color.TRANSPARENT

    private lateinit var toolbarColorAnimator: ObjectAnimator

    private var toolbarColored = false

    private lateinit var viewModel: ItemDetailViewModel

    private var item: DisplayableItem? = null
    private var itemId: Long? = null
    private var itemSourceValue: SourceType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)

        if (intent.hasExtra(EXTRA_ITEM)) {
            item = intent.getParcelableExtra(EXTRA_ITEM)
        } else {
            itemId = intent.getLongExtra(EXTRA_ITEM_ID, -1)
            itemSourceValue = intent.getSerializableExtra(EXTRA_ITEM_SOURCE) as? SourceType
        }

        if (item == null && (itemId == -1L || itemSourceValue == null)) {
            Toast.makeText(this, "No item id or source provided, exiting...",
                    Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Title bar position is calculated at runtime depending on the backdrop height.
        backdropView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            titleView.post {
                val lp = titleContainer.layoutParams as LinearLayout.LayoutParams
                lp.topMargin = backdropView.height
                titleContainer.layoutParams = lp
            }
        }

        setupToolbar()
        scrollView.addOnScrollListener(AlwaysEnterToolbarScrollListener(toolbar))
        scrollView.addOnScrollListener(this)

        bindViewModel(item, itemId, itemSourceValue)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        scrollView.post {
            val listeners = scrollView.onScrollListeners

            if (listeners != null) {
                for (listener in listeners) {
                    listener.onScrollChanged(0, 0, scrollView.scrollX, scrollView.scrollY)
                }
            }
        }
    }

    /**
     * Bind to the ViewModel to listen for data changes.
     */
    private fun bindViewModel(item: DisplayableItem?, itemId: Long?, itemSource: SourceType?) {
        viewModel = ViewModelProviders.of(this).get(ItemDetailViewModel::class.java)

        if (item != null) {
            viewModel.setDisplayableItem(item)
        } else if (itemId != null && itemSource != null) {
            viewModel.setIdAndSource(itemId, itemSource)
        }

        viewModel.uiModel.observe(this, Observer(this::updateView))
        viewModel.loadVideosForItemId(itemId ?: item!!.id, itemSource ?: item!!.source)
                .observe(this, Observer(this::updateVideosView))
    }

    /**
     * Setup toolbar and its color animator.
     */
    private fun setupToolbar() {
        if (toolbarColor == Color.TRANSPARENT) {
            toolbarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        }

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            toolbar.post { toolbar.title = null }
        }

        // Init the color animator for the toolbar.
        toolbarColorAnimator = ObjectAnimator.ofArgb(toolbar, "backgroundColor", 0)
        toolbarColorAnimator.interpolator = LinearInterpolator()
        toolbarColorAnimator.setAutoCancel(true)
    }

    /**
     * Updates the view to reflect the item details.
     */
    private fun updateView(uiModel: ItemDetailUiModel?) {
        if (uiModel == null) {
            return
        }

        val item = uiModel.item
        val dateFormatter = SimpleDateFormat("MMM YYYY", Locale.getDefault())

        if (item.posterPath != null) {
            Picasso.with(this)
                    .load(WebServiceUtils.buildMoviePosterUri(item.posterPath))
                    .placeholder(R.drawable.poster_image_placeholder)
                    .into(posterTarget)
        }

        if (item.backdropPath != null) {
            Picasso.with(this)
                    .load(WebServiceUtils.buildMovieBackdropUri(item.backdropPath))
                    .placeholder(R.drawable.image_placeholder)
                    .into(backdropView)
        }

        titleView.text = item.title
        originalTitleView.text = item.originalTitle
        overviewView.text = item.overview
        ratingView.text = getString(R.string.rating_format, item.voteAverage)
        releaseDateView.text = item.releaseDate?.let(dateFormatter::format)
    }

    /**
     * Called each time a videos event happens, loading, success, error. This method updates
     * the views accordingly.
     */
    private fun updateVideosView(uiModel: ItemVideosUiModel?) {
        if (uiModel == null) {
            return
        }

        val progressVisibility = if (uiModel.isProgressVisible) View.VISIBLE else View.GONE
        val emptyViewVisibility = if (uiModel.emptyMessage == null) View.GONE else View.VISIBLE

        videosProgressBar.visibility = progressVisibility
        videosContainer.removeAllViews()

        emptyContainer.visibility = emptyViewVisibility
        emptyContainer.findViewById<TextView>(R.id.emptyText).text = uiModel.emptyMessage
        emptyContainer.findViewById<TextView>(R.id.emptyAction).visibility = View.GONE


        if (uiModel.videos != null && !uiModel.videos.isEmpty()) {
            val inflater = LayoutInflater.from(this)
            uiModel.videos.forEach { video ->
                if (video.site == "YouTube") {
                    val itemView = inflater.inflate(R.layout.list_item_video, videosContainer, false)
                    val holder = VideoItemViewHolder(itemView)
                    holder.bind(video)
                    holder.onVideoTapped = this::openYoutubeVideo
                    videosContainer.addView(holder.itemView)
                }
            }
        }
    }

    /**
     * Open YouTube video in the YouTube App or in the browser as a fallback.
     */
    private fun openYoutubeVideo(video: Video) {
        val appIntent = Intent(Intent.ACTION_VIEW,
                Uri.parse("vnd.youtube:${video.key}"))

        val browserIntent = Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=${video.key}"))

        if (appIntent.resolveActivity(packageManager) != null) {
            startActivity(appIntent)
            return
        }

        if (browserIntent.resolveActivity(packageManager) != null) {
            startActivity(browserIntent)
            return
        }

        Toast.makeText(this, "There is no app to open the video", Toast.LENGTH_SHORT).show()
    }

    /**
     * Callback invoked when the poster bitmap palette has been processed.
     * Here we tweak the UI based on the palette for a more immersive experience.
     *
     * @param palette The processed [Palette]
     */
    private fun onPaletteGenerated(palette: Palette) {
        val darkMuted = palette.darkMutedSwatch
        if (darkMuted != null) {
            titleContainer.setBackgroundColor(darkMuted.rgb)
            toolbarColor = darkMuted.rgb
            if (toolbarColored) {
                toolbar.post { toolbar.setBackgroundColor(toolbarColor) }
            }
        }

        val lightVibrant = palette.lightVibrantSwatch
        val vibrant = palette.vibrantSwatch
        if (lightVibrant != null || vibrant != null) {
            overviewLabel.setTextColor(lightVibrant?.rgb ?: vibrant!!.rgb)
            videosLabel.setTextColor(lightVibrant?.rgb ?: vibrant!!.rgb)
        }
    }

    /**
     * Listen to the scroll view offset to animate the toolbar accordingly.
     */
    override fun onScrollChanged(left: Int, top: Int, deltaX: Int, deltaY: Int) {
        // Set to half to create a nice parallax effect!
        backdropView.translationY = (top / 2).toFloat()

        val computedHeight = (backdropView.height.toFloat()
                - toolbar.height.toFloat()
                - toolbar.translationY)

        val shouldColorize = top >= computedHeight
        animateToolbar(shouldColorize)
    }

    /**
     * Animate toolbar background color depending on the vertical scroll position.
     *
     * @param show whether to show oh hide the toolbar.
     */
    private fun animateToolbar(show: Boolean) {
        if (show != toolbarColored) {
            val startColor = if (show) Color.TRANSPARENT else toolbarColor
            val endColor = if (show) toolbarColor else Color.TRANSPARENT
            val duration = if (show) 0L else ANIMATION_DURATION

            toolbarColorAnimator.setIntValues(startColor, endColor)
            toolbarColorAnimator.duration = duration
            toolbarColorAnimator.start()
            toolbarColored = show
        }
    }

    // Custom Picasso target to be able to receive the poster bitmap for further color processing
    // using the Palette API.
    private val posterTarget = object : Target {
        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            if (bitmap != null) {
                posterView.setImageBitmap(bitmap)
                Palette.from(bitmap).generate(this@ItemDetailActivity::onPaletteGenerated)
            }
        }

        override fun onBitmapFailed(errorDrawable: Drawable?) {

        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            posterView.setImageDrawable(placeHolderDrawable)
        }
    }


        /**
     * Wrapper class around a video list item view to abstract its methods.
     */
    internal class VideoItemViewHolder(val itemView: View) {
        private val thumbnailView: ImageView = itemView.findViewById(R.id.videoThumb)
        private val titleView: TextView = itemView.findViewById(R.id.videoTitle)
        var onVideoTapped: ((Video) -> Unit)? = null

        fun bind(video: Video) {
            val videoUri = WebServiceUtils.buildYoutubeThumbUri(video.key)
            Picasso.with(itemView.context)
                    .load(videoUri)
                    .placeholder(R.drawable.image_placeholder)
                    .into(thumbnailView)

            titleView.text = video.name

            itemView.setOnClickListener { onVideoTapped?.invoke(video) }
        }
    }

    companion object {

        private const val EXTRA_ITEM_ID = "extra_item_id"

        private const val EXTRA_ITEM_SOURCE = "extra_item_source"

        private const val EXTRA_ITEM = "extra_item"

        private const val ANIMATION_DURATION = 200L

        fun start(launching: FragmentActivity, item: DisplayableItem) {
            val movieDetailIntent = Intent(launching, ItemDetailActivity::class.java).apply {
                putExtra(EXTRA_ITEM_ID, item.id)
                putExtra(EXTRA_ITEM_SOURCE, item.source)
            }
            launching.startActivity(movieDetailIntent)
        }

        /**
         * Use this when need to show details for a item view that is not in the local database.
         */
        fun startWithNetworkItem(launching: FragmentActivity, item: DisplayableItem) {
            val movieDetailIntent = Intent(launching, ItemDetailActivity::class.java)
            movieDetailIntent.putExtra(EXTRA_ITEM, item)
            launching.startActivity(movieDetailIntent)
        }
    }
}
