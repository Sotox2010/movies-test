package com.jesussoto.android.rappimovies.offlinesearch

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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.jesussoto.android.rappimovies.R
import com.jesussoto.android.rappimovies.moviedetail.ItemDetailActivity
import com.jesussoto.android.rappimovies.movies.FilterType
import com.jesussoto.android.rappimovies.movies.ItemsAdapter
import com.jesussoto.android.rappimovies.movies.SourceType
import com.jesussoto.android.rappimovies.util.DisplayableItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_offline_search.*

class OfflineSearchActivity: AppCompatActivity() {

    private lateinit var emptyContainer: ViewGroup

    private lateinit var adapter: ItemsAdapter

    private lateinit var viewModel: OfflineSearchViewModel

    private var disposables: CompositeDisposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline_search)

        setupToolbar()
        setupToolbarSpinners()
        setupRecyclerView()
        setupEmptyView()

        // Restore the source filter from the previous saved state (if any).
        val sourceFilter = if (savedInstanceState == null) SourceType.MOVIES
                else SourceType.fromValue(savedInstanceState.getInt(STATE_SOURCE_FILTER))

        // Restore the category filter from the previous saved state (if any).
        val categoryFilter = if (savedInstanceState == null) FilterType.POPULAR
                else FilterType.fromValue(savedInstanceState.getInt(STATE_CATEGORY_FILTER))

        // Restore the search query from the previous saved state (if any).
        val searchQuery = savedInstanceState?.getString(STATE_SEARCH_QUERY)


        viewModel = ViewModelProviders.of(this).get(OfflineSearchViewModel::class.java)
        viewModel.setSourceFilter(sourceFilter)
        viewModel.setCategoryFilter(categoryFilter)
        viewModel.setSearchQuery(searchQuery ?: "")
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        viewModel.sourceFilter?.let {
            outState?.putInt(STATE_SOURCE_FILTER, it.value)
        }

        viewModel.categoryFilter?.let {
            outState?.putInt(STATE_CATEGORY_FILTER, it.value)
        }

        queryEditText.text?.let {
            outState?.putString(STATE_SEARCH_QUERY, it.toString())
        }
    }

    override fun onStart() {
        super.onStart()
        bindViewModel()
    }

    override fun onStop() {
        super.onStop()
        unbindViewModel()
    }

    private fun bindViewModel() {
        disposables = CompositeDisposable()
        disposables?.add(viewModel.getUiModel()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateView))
    }

    private fun unbindViewModel() {
        disposables?.dispose()
        disposables = null
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        toolbar.post { toolbar.title = null }
        queryEditText.addTextChangedListener(textChangedListener)
        clearButton.setOnClickListener { queryEditText.text = null }
    }

    private fun setupToolbarSpinners() {
        val sourceOptions = resources.getStringArray(R.array.source_filters)
        val categoryOptions = resources.getStringArray(R.array.category_filters_movie)

        val sourceSpinnerAdapter = ArrayAdapter(
                this, R.layout.spinner_item_search_filter, sourceOptions)

        val categorySpinnerAdapter = ArrayAdapter(
                this, R.layout.spinner_item_search_filter, categoryOptions)

        sourceSpinnerAdapter.setDropDownViewResource(R.layout.spinner_item_search_filter_dropdown)
        categorySpinnerAdapter.setDropDownViewResource(R.layout.spinner_item_search_filter_dropdown)

        sourceSpinner.adapter = sourceSpinnerAdapter
        categorySpinner.adapter = categorySpinnerAdapter

        sourceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val source = when (position) {
                    0 -> SourceType.MOVIES
                    1 -> SourceType.TV_SERIES
                    else -> throw IllegalArgumentException()
                }
                viewModel.setSourceFilter(source)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val category = when (position) {
                    0 -> FilterType.POPULAR
                    1 -> FilterType.TOP_RATED
                    2 -> FilterType.UPCOMING
                    else -> throw IllegalArgumentException()
                }
                viewModel.setCategoryFilter(category)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
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

    private fun setupEmptyView() {
        emptyContainer = findViewById(R.id.emptyContainer)
        emptyContainer.findViewById<Button>(R.id.emptyAction).visibility = View.GONE
    }

    private fun updateView(uiModel: SearchUiModel) {
        val results = uiModel.results
        val emptyViewVisibility = if (results == null || results.isEmpty())
            View.VISIBLE else View.GONE

        adapter.replaceData(results)
        emptyContainer.visibility = emptyViewVisibility
        emptyContainer.findViewById<TextView>(R.id.emptyText).text = uiModel.emptyMessage
    }

    private fun navigateToItemDetail(item: DisplayableItem) {
        ItemDetailActivity.start(this, item)
    }

    private val textChangedListener = object : TextWatcher {
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
        private const val STATE_CATEGORY_FILTER = "state_category_filter"
        private const val STATE_SEARCH_QUERY = "state_search_query"

        @JvmStatic
        fun start(launching: FragmentActivity) {
            launching.startActivity(Intent(launching, OfflineSearchActivity::class.java))
        }
    }
}
