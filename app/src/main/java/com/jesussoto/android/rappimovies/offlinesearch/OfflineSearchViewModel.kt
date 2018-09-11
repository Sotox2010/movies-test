package com.jesussoto.android.rappimovies.offlinesearch

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.text.TextUtils
import com.jesussoto.android.rappimovies.data.repository.MoviesRepository
import com.jesussoto.android.rappimovies.data.repository.TvSeriesRepository
import com.jesussoto.android.rappimovies.movies.FilterType
import com.jesussoto.android.rappimovies.movies.SourceType
import com.jesussoto.android.rappimovies.util.DisplayableItem
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.functions.Function3
import io.reactivex.subjects.BehaviorSubject

class OfflineSearchViewModel(app: Application): AndroidViewModel(app) {

    private val moviesRepository = MoviesRepository.getInstance(app)

    private val tvSeriesRepository = TvSeriesRepository.getInstance(app)

    private val sourceFilterSubject: BehaviorSubject<SourceType> = BehaviorSubject.create()

    private val categoryFilterSubject: BehaviorSubject<FilterType> = BehaviorSubject.create()

    private val searchQuerySubject: BehaviorSubject<String> = BehaviorSubject.create()

    val sourceFilter: SourceType?
        get() = sourceFilterSubject.value

    val categoryFilter: FilterType?
        get() = categoryFilterSubject.value

    init {

    }

    fun getUiModel(): Flowable<SearchUiModel> {
        return Observable.combineLatest(
                sourceFilterSubject,
                categoryFilterSubject,
                searchQuerySubject,
                Function3(this::combineParams))
                .toFlowable(BackpressureStrategy.LATEST)
                .switchMap { params -> searchWithParams(params)
                            .onErrorResumeNext(Flowable.empty()) }
                .map(this::constructUiModel)
    }

    private fun combineParams(source: SourceType, category: FilterType, query: String): SearchParams {
        return SearchParams(source, category, query)
    }

    private fun searchWithParams(params: SearchParams): Flowable<List<DisplayableItem>> {
        return if (params.source == SourceType.MOVIES)
            moviesRepository.searchByNameAndCategory(params.query, params.category)
                .flatMapSingle {
                    Flowable.fromIterable(it)
                            .map(::DisplayableItem)
                            .toList()
                }
        else
            tvSeriesRepository.searchByNameAndCategory(params.query, params.category)
                .flatMapSingle {
                    Flowable.fromIterable(it)
                            .map(::DisplayableItem)
                            .toList()
                }

    }

    fun setSearchQuery(query: String) {
        searchQuerySubject.onNext(query)
    }

    fun setSourceFilter(source: SourceType) {
        if (source != sourceFilterSubject.value) {
            sourceFilterSubject.onNext(source)
        }
    }

    fun setCategoryFilter(category: FilterType) {
        if (category != categoryFilterSubject.value) {
            categoryFilterSubject.onNext(category)
        }
    }

    private fun constructUiModel(results: List<DisplayableItem>): SearchUiModel {
        var emptyMessage: String? = null

        if (results.isEmpty()) {
            emptyMessage = if (sourceFilter == SourceType.TV_SERIES && categoryFilter == FilterType.UPCOMING) {
                "TV Series don't support the 'Upcoming' category."
            } else if (!TextUtils.isEmpty(searchQuerySubject.value)) {
                "No result for this query in the selected categories. Try a different query and categories."
            } else {
                "You haven't typed anything! Start typing to search for Movies or TV Series."
            }
        }

        return SearchUiModel(results, emptyMessage)
    }

}
