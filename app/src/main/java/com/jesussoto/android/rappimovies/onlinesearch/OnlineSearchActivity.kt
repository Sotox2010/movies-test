package com.jesussoto.android.rappimovies.onlinesearch

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.jesussoto.android.rappimovies.R
import com.jesussoto.android.rappimovies.moviedetail.ItemDetailActivity
import com.jesussoto.android.rappimovies.movies.ItemsAdapter
import com.jesussoto.android.rappimovies.movies.SourceType
import com.jesussoto.android.rappimovies.offlinesearch.SearchUiModel
import com.jesussoto.android.rappimovies.util.DisplayableItem
import kotlinx.android.synthetic.main.activity_online_search.*

class OnlineSearchActivity: AppCompatActivity() {

    private lateinit var emptyContainer: ViewGroup

    private lateinit var adapter: ItemsAdapter

    private lateinit var viewModel: OnlineSearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_search)

        // Restore the source filter from the previous saved state (if any).
        val sourceFilter = if (savedInstanceState == null) SourceType.MOVIES
                else SourceType.fromValue(savedInstanceState.getInt(STATE_SOURCE_FILTER))

        // Restore the search query from the previous saved state (if any).
        val searchQuery = savedInstanceState?.getString(STATE_SEARCH_QUERY)

        setupToolbar()
        setupToolbarFilters()
        setupRecyclerView()
        setupEmptyView()
        bindViewModel(sourceFilter, searchQuery)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        toolbar.post { toolbar.title = null }
        queryEditText.addTextChangedListener(searchTextChangedListener)
        clearButton.setOnClickListener { queryEditText.text = null }
    }

    private fun setupToolbarFilters() {
        filterGroup.setOnCheckedChangeListener { _, checkedId ->
            viewModel.setSourceFilter(if (checkedId == R.id.radioFilterMovies)
                SourceType.MOVIES else SourceType.TV_SERIES)
        }
    }

    private fun setupEmptyView() {
        emptyContainer = findViewById(R.id.emptyContainer)
        emptyContainer.visibility = View.VISIBLE
        emptyContainer.findViewById<TextView>(R.id.emptyText).text =
                "You haven't typed anything! Start typing to search online."
        emptyContainer.findViewById<Button>(R.id.emptyAction).visibility = View.GONE
    }

    /**
     * Set up the recycler view with grid layout.
     */
    private fun setupRecyclerView() {
        val spanCount = resources.getInteger(R.integer.movie_grid_span_count)

        adapter = ItemsAdapter(null)
        adapter.setOnItemTappedListener(this::navigateToItemDetail)

        searchRecyclerView.layoutManager = GridLayoutManager(this, spanCount)
        searchRecyclerView.adapter = adapter
    }

    private fun bindViewModel(source: SourceType, query: String?) {
        viewModel = ViewModelProviders.of(this).get(OnlineSearchViewModel::class.java)
        viewModel.setSourceFilter(source)
        viewModel.setSearchQuery(query ?: "")
        viewModel.uiModel.observe(this, Observer(this::updateView))
        viewModel.isOnlineSubject.observe(this, Observer(this::onlineStatusChanged))
    }

    private fun updateView(uiModel: SearchUiModel?) {
        if (uiModel == null) {
            return
        }

        val loadingVisibility = if (uiModel.isLoadingVisible) View.VISIBLE else View.GONE
        val gridVisibility = if (uiModel.results == null || uiModel.results.isEmpty())
                View.GONE else View.VISIBLE
        val emptyViewVisibility = if (gridVisibility == View.VISIBLE) View.GONE  else View.VISIBLE

        adapter.replaceData(uiModel.results)
        searchRecyclerView.visibility = gridVisibility
        progressIndicator.visibility = loadingVisibility

        emptyContainer.visibility = emptyViewVisibility
        emptyContainer.findViewById<TextView>(R.id.emptyText).text = uiModel.emptyMessage

    }

    /**
     * Show/hide message view when network connectivity changes.
     */
    private fun onlineStatusChanged(isOnline: Boolean?) {
        if (isOnline == null) {
            return
        }

        onlineWarningView.visibility = if (isOnline) View.GONE else View.VISIBLE
    }

    private fun navigateToItemDetail(item: DisplayableItem) {
        ItemDetailActivity.startWithNetworkItem(this, item)
    }

    private val searchTextChangedListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            viewModel.setSearchQuery(s.toString())
            clearButton.visibility = if (TextUtils.isEmpty(s)) View.GONE else View.VISIBLE
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }
    }

    companion object {
        private const val STATE_SOURCE_FILTER = "state_source_filter"
        private const val STATE_SEARCH_QUERY = "state_search_query"

        @JvmStatic
        fun start(launching: FragmentActivity) {
            launching.startActivity(Intent(launching, OnlineSearchActivity::class.java))
        }
    }
}