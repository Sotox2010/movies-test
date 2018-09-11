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

    internal val sourceFilter: SourceType?
        get() = sourceFilteringLiveData.value

    internal fun getMainUiModel(): LiveData<MainUiModel> {
        return Transformations.map(sourceFilteringLiveData, this::constructMainUiModel)
    }

    /**
     *
     */
    internal fun getItemsUiModel(source: SourceType, filter: FilterType): LiveData<ItemsUiModel> {
        return Transformations.map(getItemsByFilter(source, filter), this::constructItemsUiModel)
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
    private fun getMoviesByFilter(filter: FilterType): LiveData<Resource<List<DisplayableItem>>> {
        val liveData = when (filter) {
            FilterType.POPULAR   ->  moviesRepository.loadPopularMovies()
            FilterType.TOP_RATED ->  moviesRepository.loadTopRatedMovies()
            FilterType.UPCOMING  ->  moviesRepository.loadUpcomingMovies()
        }

        return Transformations.map(liveData, this::mapMoviesToDisplayableItems)
    }

    /**
     *
     */
    private fun getTvSeriesByFilter(filter: FilterType): LiveData<Resource<List<DisplayableItem>>> {
        val resultData = when (filter) {
            FilterType.POPULAR -> tvSeriesRepository.loadPopularTvSeries()
            FilterType.TOP_RATED -> tvSeriesRepository.loadTopRatedTvSeries()
            else  ->  throw IllegalArgumentException("Tv series do not support 'Upcoming' category.")
        }

        return Transformations.map(resultData, this::mapTvSeriesToDisplayableItems)
    }

    /**
     *
     */
    private fun getItemsByFilter(source: SourceType, filter: FilterType):
            LiveData<Resource<List<DisplayableItem>>> {

        return if (source == SourceType.MOVIES) getMoviesByFilter(filter)
               else getTvSeriesByFilter(filter)
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

    internal fun refreshMovies() {
        /*if (sourceFilter === SourceType.MOVIES) {
            moviesRepository.refreshPopularMovies()
        } else {
            moviesRepository.refreshTopRatedMovies()
        }*/
    }

    private fun constructMainUiModel(sourceFiltering: SourceType): MainUiModel {
        return MainUiModel(sourceFiltering)
    }

    private fun constructItemsUiModel(result: Resource<List<DisplayableItem>>): ItemsUiModel {
        val items = result.data
        val showLoading = result.status == Resource.Status.LOADING
        val showError = result.status == Resource.Status.ERROR

        return ItemsUiModel(items, showLoading, showError)
    }
}
