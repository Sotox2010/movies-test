package com.jesussoto.android.rappimovies.movies

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.jesussoto.android.rappimovies.api.Resource
import com.jesussoto.android.rappimovies.data.entity.Movie
import com.jesussoto.android.rappimovies.data.entity.TvSeries
import com.jesussoto.android.rappimovies.data.repository.MoviesRepository
import com.jesussoto.android.rappimovies.data.repository.TvSeriesRepository
import com.jesussoto.android.rappimovies.util.DisplayableItem

class MainViewModel(app: Application): AndroidViewModel(app) {

    // This should be preferably injected from outside;
    private val moviesRepository: MoviesRepository = MoviesRepository.getInstance(app)

    private val tvSeriesRepository: TvSeriesRepository = TvSeriesRepository.getInstance(app)

    // LiveData to keep track of the current source filter (tvSeries of tv-series)
    private val sourceFilteringLiveData: MutableLiveData<SourceType> = MutableLiveData()

    /**
     * Live data to keep track of the current page.
     */
    private val popularMoviesCurrentPage = MutableLiveData<Int>()
    private val topRatedMoviesCurrentPage = MutableLiveData<Int>()
    private val upcomingMoviesCurrentPage = MutableLiveData<Int>()
    private val popularTvSeriesCurrentPage = MutableLiveData<Int>()
    private val topRatedTvSeriesCurrentPage = MutableLiveData<Int>()

    /**
     * Items cache for each source and category
     */
    private val popularMoviesItemsCache = ArrayList<Movie>()
    private val topRatedMoviesItemsCache = ArrayList<Movie>()
    private val upcomingMoviesItemsCache = ArrayList<Movie>()
    private val popularTvSeriesItemsCache = ArrayList<TvSeries>()
    private val topRatedTvSeriesItemsCache = ArrayList<TvSeries>()

    internal val sourceFilter: SourceType?
        get() = sourceFilteringLiveData.value

    /**
     *
     */
    internal fun getMainUiModel(): LiveData<MainUiModel> {
        return Transformations.map(sourceFilteringLiveData, this::constructMainUiModel)
    }

    /**
     *
     */
    fun loadNextPage(source: SourceType, category: FilterType) {
        val data = getCurrentPageLiveData(source, category)
        val value = data.value ?: 0
        data.value = value + 1
    }

    fun loadFirstPage(source: SourceType, category: FilterType) {
        val data = getCurrentPageLiveData(source, category)
        data.value = 1
    }

    /**
     *
     */
    private fun getCurrentPageLiveData(source: SourceType, category: FilterType): MutableLiveData<Int> {
        return when(source) {
            SourceType.MOVIES -> when(category) {
                FilterType.POPULAR -> popularMoviesCurrentPage
                FilterType.TOP_RATED -> topRatedMoviesCurrentPage
                FilterType.UPCOMING -> upcomingMoviesCurrentPage
            }
            SourceType.TV_SERIES -> when(category) {
                FilterType.POPULAR -> popularTvSeriesCurrentPage
                FilterType.TOP_RATED -> topRatedTvSeriesCurrentPage
                else -> throw IllegalArgumentException()
            }
        }
    }

    /**
     *
     */
    internal fun getPaginatedItemsUiModel(source: SourceType, filter: FilterType): LiveData<PaginatedItemsUiModel> {
        return Transformations.map(
                Transformations.switchMap(
                        getCurrentPageLiveData(source, filter))
                        { getPaginatedItemsByFilter(source, filter, it) },
                this::constructPaginatedItemsUiModel)
    }

    /**
     *
     */
    internal fun setSourceFiltering(source: SourceType) {
        if (sourceFilter != source) {
            sourceFilteringLiveData.value = source
        }
    }

    /**
     *
     */
    private fun getMoviesByFilter(filter: FilterType, page: Int): LiveData<Resource<List<DisplayableItem>>> {
        val liveData = when (filter) {
            FilterType.POPULAR   ->  moviesRepository.loadPopularMoviesByPage(page, popularMoviesItemsCache)
            FilterType.TOP_RATED ->  moviesRepository.loadTopRatedMoviesByPage(page, topRatedMoviesItemsCache)
            FilterType.UPCOMING  ->  moviesRepository.loadUpcomingMoviesByPage(page, upcomingMoviesItemsCache)
        }

        return Transformations.map(liveData, this::mapMoviesToDisplayableItems)
    }

    /**
     *
     */
    private fun getTvSeriesByFilter(filter: FilterType, page: Int): LiveData<Resource<List<DisplayableItem>>> {
        val resultData = when (filter) {
            FilterType.POPULAR -> tvSeriesRepository.loadPopularTvSeriesByPage(page, popularTvSeriesItemsCache)
            FilterType.TOP_RATED -> tvSeriesRepository.loadTopRatedTvSeriesByPage(page, topRatedTvSeriesItemsCache)
            else  ->  throw IllegalArgumentException("Tv series do not support 'Upcoming' category.")
        }

        return Transformations.map(resultData, this::mapTvSeriesToDisplayableItems)
    }

    /**
     *
     */
    private fun getPaginatedItemsByFilter(source: SourceType, filter: FilterType, page: Int):
            LiveData<Resource<List<DisplayableItem>>> {

        return if (source == SourceType.MOVIES)
            getMoviesByFilter(filter, page)
        else
            getTvSeriesByFilter(filter, page)

        //return Transformations.map(moviesRepository.loadPopularMoviesByPage(page, popularMoviesItemsCache),
          //      this::mapMoviesToDisplayableItems)
    }

    fun getItemsCache(source: SourceType, category: FilterType): MutableList<DisplayableItem> {
        val itemsCache = when(source) {
            SourceType.MOVIES -> when(category) {
                FilterType.POPULAR -> popularMoviesItemsCache.map(::DisplayableItem)
                FilterType.TOP_RATED -> topRatedMoviesItemsCache.map(::DisplayableItem)
                FilterType.UPCOMING -> upcomingMoviesItemsCache.map(::DisplayableItem)
            }
            SourceType.TV_SERIES -> when(category) {
                FilterType.POPULAR -> popularTvSeriesItemsCache.map(::DisplayableItem)
                FilterType.TOP_RATED -> topRatedTvSeriesItemsCache.map(::DisplayableItem)
                else -> throw IllegalArgumentException()
            }
        }

        return itemsCache.toMutableList()
    }

    private fun mapMoviesToDisplayableItems(resource: Resource<List<Movie>>):
            Resource<List<DisplayableItem>> {

        var items: List<DisplayableItem>? = null
        if (resource.data != null) {
            items = resource.data.map { DisplayableItem(it) }
        }

        return Resource(resource.status, items, resource.throwable)
    }

    private fun mapTvSeriesToDisplayableItems(resource: Resource<List<TvSeries>>):
            Resource<List<DisplayableItem>> {

        var items: List<DisplayableItem>? = null
        if (resource.data != null) {
            items = resource.data.map { DisplayableItem(it) }
        }

        return Resource(resource.status, items, resource.throwable)
    }

    /**
     *
     */
    private fun constructMainUiModel(sourceFiltering: SourceType): MainUiModel {
        return MainUiModel(sourceFiltering)
    }

    /**
     *
     */
    private fun constructPaginatedItemsUiModel(result: Resource<List<DisplayableItem>>): PaginatedItemsUiModel {
        val items = result.data
        val showLoading = result.status == Resource.Status.LOADING
        val showError = result.status == Resource.Status.ERROR

        return PaginatedItemsUiModel(items, showLoading, showError)
    }
}
