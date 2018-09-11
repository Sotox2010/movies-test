package com.jesussoto.android.rappimovies.movies

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.PopupMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.jesussoto.android.rappimovies.R
import com.jesussoto.android.rappimovies.offlinesearch.OfflineSearchActivity
import com.jesussoto.android.rappimovies.onlinesearch.OnlineSearchActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity: AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Restore the filter from the previous saved state (if any).
        val filter = if (savedInstanceState == null)
            SourceType.MOVIES
        else
            SourceType.fromValue(savedInstanceState.getInt(STATE_SOURCE_FILTERING))

        setupToolbar()
        bindViewModel(filter)
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_SOURCE_FILTERING, viewModel.sourceFilter?.value ?: SourceType.MOVIES.value)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.menu_item_search) {
            showFilteringPopUpMenu()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun bindViewModel(restoredSource: SourceType) {
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.setSourceFiltering(restoredSource)
        viewModel.getMainUiModel().observe(this, Observer(this::updateView))
    }

    /**
     * Set up the toolbar and the filter spinner as well.
     */
    private fun setupToolbar() {
        setSupportActionBar(toolbar)

        // Remove the default title set by the system in favor of the filter spinner.
        toolbar.post { toolbar.title = null }

        val options = resources.getStringArray(R.array.source_filters)
        val verticalOffset = resources.getDimensionPixelSize(R.dimen.spacing_medium)

        val adapter = ArrayAdapter(this,
                R.layout.spinner_item_movie_filter,
                options)
        adapter.setDropDownViewResource(R.layout.spinner_item_movie_filter_dropdown)

        val spinner = toolbarSpinner.apply {
            dropDownVerticalOffset = verticalOffset
            setAdapter(adapter)
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setSourceFiltering(when(position) {
                    0 -> SourceType.MOVIES
                    else -> SourceType.TV_SERIES
                })
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    private fun showFilteringPopUpMenu() {
        val popup = PopupMenu(this, findViewById(R.id.menu_item_search))
        popup.menuInflater.inflate(R.menu.menu_search, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.search_offline -> navigateToOfflineSearchActivity()
                R.id.search_online -> navigateToOnlineSearchActivity()
            }
            true
        }

        popup.show()
    }

    private fun updateView(uiModel: MainUiModel?) {
        if (uiModel == null) {
            return
        }

        val source = uiModel.filter

        val titles = listOf<String>(
            getString(R.string.category_most_popular),
            getString(R.string.category_top_rated),
            getString(R.string.category_upcoming)
        )

        val fragments = arrayListOf<Fragment>(
            PaginatedItemsFragment.newInstance(source, FilterType.POPULAR),
            PaginatedItemsFragment.newInstance(source, FilterType.TOP_RATED)
        )

        if (source == SourceType.MOVIES) {
            fragments.add(PaginatedItemsFragment.newInstance(source, FilterType.UPCOMING))
        }

        val pagerAdapter = MoviesPagerAdapter(supportFragmentManager, fragments, titles)
        viewPager.adapter = pagerAdapter
        tabLayout.setupWithViewPager(viewPager)
    }

    private fun navigateToOfflineSearchActivity() {
         OfflineSearchActivity.start(this)
    }

    private fun navigateToOnlineSearchActivity() {
        OnlineSearchActivity.start(this)
    }

    internal class MoviesPagerAdapter(
            fm: FragmentManager,
            private val fragments: List<Fragment>,
            private val titles: List<String>): FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }
    }

    companion object {

        private const val STATE_SOURCE_FILTERING = "state_source_filtering"

    }
}
