package com.jesussoto.android.rappimovies.moviedetail

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.jesussoto.android.rappimovies.api.Resource
import com.jesussoto.android.rappimovies.api.model.Video
import com.jesussoto.android.rappimovies.data.repository.MoviesRepository
import com.jesussoto.android.rappimovies.data.repository.TvSeriesRepository
import com.jesussoto.android.rappimovies.movies.SourceType
import com.jesussoto.android.rappimovies.util.DisplayableItem
import io.reactivex.schedulers.Schedulers

class ItemDetailViewModel(app: Application): AndroidViewModel(app) {

    private val moviesRepository = MoviesRepository.getInstance(app)

    private val tvSeriesRepository = TvSeriesRepository.getInstance(app)

    val uiModel: MutableLiveData<ItemDetailUiModel> = MutableLiveData()

    /**
     * Set the ID and the source of the item to load.
     */
    fun setIdAndSource(itemId: Long, itemSource: SourceType) {
        if (itemSource == SourceType.MOVIES) {
            loadMovie(itemId)
            return
        }

        loadTvSeries(itemId)
    }

    /**
     * Directly sets the item to display its details.
     */
    fun setDisplayableItem(item: DisplayableItem) {
        uiModel.value = constructUiModel(item)
    }

    /**
     *
     */
    private fun loadMovie(movieId: Long) {
        moviesRepository.loadMovieById(movieId)
                .map(::DisplayableItem)
                .map(this::constructUiModel)
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .subscribe { value: ItemDetailUiModel? -> uiModel.postValue(value) }
    }

    /**
     *
     */
    private fun loadTvSeries(tvSeriesId: Long) {
        tvSeriesRepository.loadTvSeriesById(tvSeriesId)
                .map(::DisplayableItem)
                .map(this::constructUiModel)
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .subscribe { value: ItemDetailUiModel? -> uiModel.postValue(value) }
    }

    /**
     *
     */
    fun loadVideosForItemId(itemId: Long, itemSource: SourceType): LiveData<ItemVideosUiModel> {
        return if (itemSource == SourceType.MOVIES)
            Transformations.map(
                moviesRepository.loadVideosByMovieId(itemId),
                this::constructVideosUiModel)
        else
            Transformations.map(
                    tvSeriesRepository.loadVideosByTvSeriesID(itemId),
                    this::constructVideosUiModel)
    }

    /**
     * Builds the Ui Model based on the resulting item.
     */
    private fun constructUiModel(item: DisplayableItem): ItemDetailUiModel {
        return ItemDetailUiModel(item)
    }

    /**
     * Builds the Ui Model based on the result of obtaining the videos to present them correctly
     * in the UI.
     */
    private fun constructVideosUiModel(result: Resource<List<Video>>): ItemVideosUiModel {
        val videos = result.data
        val isProgressVisible = result.status == Resource.Status.LOADING
        var message: String? = null

        if (videos == null || videos.isEmpty()) {
            if (result.status == Resource.Status.ERROR){
                message = "Could not load videos. Check your connection."
            } else if (result.status == Resource.Status.SUCCESS) {
                message = "No videos."
            }
        }
        return ItemVideosUiModel(videos, isProgressVisible, message)
    }
}
