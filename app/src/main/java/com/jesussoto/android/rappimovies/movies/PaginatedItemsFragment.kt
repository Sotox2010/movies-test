package com.jesussoto.android.rappimovies.movies

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jesussoto.android.rappimovies.R
import com.jesussoto.android.rappimovies.moviedetail.ItemDetailActivity
import com.jesussoto.android.rappimovies.util.DisplayableItem
import com.jesussoto.android.rappimovies.widget.LoadMoreScrollListener
import kotlinx.android.synthetic.main.fragment_movies.*

class PaginatedItemsFragment : Fragment() {

    private lateinit var adapter: PaginatedItemsAdapter

    private lateinit var viewModel: MainViewModel

    private lateinit var source: SourceType

    private lateinit var category: FilterType

    private var loading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args == null || !args.containsKey(ARG_SOURCE) || !args.containsKey(ARG_CATEGORY)) {
            throw IllegalStateException("ItemsFragment needs a source type and category argument.")
        }

        source = SourceType.fromValue(args.getInt(ARG_SOURCE))
        category = FilterType.fromValue(args.getInt(ARG_CATEGORY))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_movies, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        //emptyAction.setOnClickListener { viewModel.refreshMovies() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindViewModel(savedInstanceState == null)
        loading = savedInstanceState?.getBoolean(STATE_LOADING, false) ?: false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_LOADING, loading);
    }

    /**
     * Bind to the view model to react to data changes.
     */
    private fun bindViewModel(freshLaunch: Boolean) {
        viewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)

        // Observe to changes in the ui model, every time the fetch status or the movie sourceFilter
        // changes, a new ui model will be emitted and the ui will be updated based on the new ui
        // model.
        viewModel.getPaginatedItemsUiModel(source, category)
                .observe(this, Observer(this::updateView))


        val restoringCache = viewModel.getItemsCache(source, category)
        if (freshLaunch || restoringCache.isEmpty()) {
            viewModel.loadFirstPage(source, category)
        } else {
            adapter.replaceData(restoringCache)
        }
    }

    /**
     * Set up the items grid layout and add the toolbar animation scroll listener.
     */
    private fun setupRecyclerView() {
        val spanCount = resources.getInteger(R.integer.movie_grid_span_count)

        adapter = PaginatedItemsAdapter(mutableListOf())
        adapter.setOnItemTappedListener(this::navigateToMovieDetail)

        moviesRecyclerView.layoutManager = GridLayoutManager(requireActivity(), spanCount)
        moviesRecyclerView.adapter = adapter
        moviesRecyclerView.addOnScrollListener(LoadMoreScrollListener(this::onLoadMore))
    }

    /**
     * Callback to trigger the loading of more items.
     */
    private fun onLoadMore() {
        if (!loading) {
            loading = true
            viewModel.loadNextPage(source, category)
        }
    }


    /**
     * Updates the view based on the UiModel state, this gets called each time new data is
     * available to display in the UI.
     *
     * @param uiModel with all the data to display in the UI.
     */
    private fun updateView(uiModel: PaginatedItemsUiModel?) {
        if (uiModel == null) {
            return
        }

        val progressVisibility = if (uiModel.isProgressVisible) View.VISIBLE else View.GONE

        adapter.appendData(uiModel.items ?: mutableListOf())
        progressContainer.visibility = progressVisibility
        loading = false

        if (uiModel.isErrorVisible) {
            Snackbar.make(progressContainer, "Error while retrieve items, check connection.",
                    Snackbar.LENGTH_LONG).show()
        }
    }

    /**
     * Navigate to [ItemDetailActivity] to show the details of the given [DisplayableItem].
     *
     * @param item the [DisplayableItem] to show its details.
     */
    private fun navigateToMovieDetail(item: DisplayableItem) {
        ItemDetailActivity.start(requireActivity(), item)
    }

    companion object {
        private const val STATE_LOADING = "state_loading"

        private const val ARG_SOURCE = "arg_source"
        private const val ARG_CATEGORY = "arg_category"

        @JvmStatic
        internal fun newInstance(source: SourceType, category: FilterType): PaginatedItemsFragment {
            val args = Bundle().apply {
                putInt(ARG_SOURCE, source.value)
                putInt(ARG_CATEGORY, category.value)
            }

            return PaginatedItemsFragment().apply {
                arguments = args
            }
        }
    }
}
