package com.jesussoto.android.rappimovies.movies

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jesussoto.android.rappimovies.R
import com.jesussoto.android.rappimovies.moviedetail.ItemDetailActivity
import com.jesussoto.android.rappimovies.util.DisplayableItem
import kotlinx.android.synthetic.main.fragment_movies.*

class ItemsFragment : Fragment() {

    private lateinit var adapter: ItemsAdapter

    private lateinit var viewModel: MainViewModel

    private lateinit var source: SourceType

    private lateinit var category: FilterType

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
        emptyAction.setOnClickListener { viewModel.refreshMovies() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindViewModel()
    }

    /**
     * Bind to the view model to react to data changes.
     */
    private fun bindViewModel() {
        viewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)

        // Observe to changes in the ui model, every time the fetch status or the movie sourceFilter
        // changes, a new ui model will be emitted and the ui will be updated based on the new ui
        // model.
        viewModel.getItemsUiModel(source, category).observe(this, Observer(this::updateView))
    }

    /**
     * Set up the items grid layout and add the toolbar animation scroll listener.
     */
    private fun setupRecyclerView() {
        val spanCount = resources.getInteger(R.integer.movie_grid_span_count)

        adapter = ItemsAdapter(null)
        adapter.setOnItemTappedListener(this::navigateToMovieDetail)

        moviesRecyclerView.layoutManager = GridLayoutManager(requireActivity(), spanCount)
        moviesRecyclerView.adapter = adapter
    }

    /**
     * Updates the view based on the UiModel state, this gets called each time new data is
     * available to display in the UI.
     *
     * @param uiModel with all the data to display in the UI.
     */
    private fun updateView(uiModel: ItemsUiModel?) {
        if (uiModel == null) {
            return
        }

        val listVisibility = if (uiModel.items == null) View.GONE else View.VISIBLE
        val progressVisibility = if (uiModel.isProgressVisible) View.VISIBLE else View.GONE
        val errorVisibility = if (uiModel.isErrorVisible) View.VISIBLE else View.GONE

        adapter.replaceData(uiModel.items)
        moviesRecyclerView.visibility = listVisibility
        progress.visibility = progressVisibility
        emptyContainer.visibility = errorVisibility
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
        private const val ARG_SOURCE = "arg_source"
        private const val ARG_CATEGORY = "arg_category"

        @JvmStatic
        internal fun newInstance(source: SourceType, category: FilterType): ItemsFragment {
            val args = Bundle().apply {
                putInt(ARG_SOURCE, source.value)
                putInt(ARG_CATEGORY, category.value)
            }

            return ItemsFragment().apply {
                arguments = args
            }
        }
    }
}
