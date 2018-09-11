package com.jesussoto.android.rappimovies.onlinesearch

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.text.TextUtils
import com.jesussoto.android.rappimovies.api.Resource
import com.jesussoto.android.rappimovies.data.repository.MoviesRepository
import com.jesussoto.android.rappimovies.data.repository.TvSeriesRepository
import com.jesussoto.android.rappimovies.movies.FilterType
import com.jesussoto.android.rappimovies.movies.SourceType
import com.jesussoto.android.rappimovies.offlinesearch.SearchParams
import com.jesussoto.android.rappimovies.offlinesearch.SearchUiModel
import com.jesussoto.android.rappimovies.util.DisplayableItem
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit


class OnlineSearchViewModel(val app: Application) : AndroidViewModel(app) {

    private val moviesRepository = MoviesRepository.getInstance(app)

    private val tvSeriesRepository = TvSeriesRepository.getInstance(app)

    val uiModel = MutableLiveData<SearchUiModel>()

    val isOnlineSubject: MutableLiveData<Boolean> = MutableLiveData()

    private val searchQuerySubject: BehaviorSubject<String> = BehaviorSubject.create()

    private val sourceFilterSubject: BehaviorSubject<SourceType> = BehaviorSubject.create()

    private val networkStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            onNetworkChanged(manager.activeNetworkInfo)
        }
    }

    init {
        Observable.combineLatest(sourceFilterSubject, searchQuerySubject, BiFunction(this::combineParams))
            .debounce(700, TimeUnit.MILLISECONDS)
            .filter { !it.query.isEmpty() }
            .distinctUntilChanged()
            .doOnNext { uiModel.postValue(constructUiModel(Resource.loading(null))) }
            .switchMap { params -> searchWithParams(params)
                                .subscribeOn(Schedulers.computation())
                                .onErrorResumeNext(Observable.empty())}
            .map { Resource.success(it) }
            .map(this::constructUiModel)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(uiModel::setValue)


        // Start listening for network changes.
        app.registerReceiver(networkStateReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    fun setSourceFilter(source: SourceType) {
        if (source != sourceFilterSubject.value) {
            sourceFilterSubject.onNext(source)
        }
    }

    fun setSearchQuery(query: String) {
        searchQuerySubject.onNext(query)
    }


    private fun combineParams(source: SourceType, query: String): SearchParams {
        return SearchParams(source, FilterType.POPULAR, query)
    }

    private fun searchWithParams(params: SearchParams): Observable<List<DisplayableItem>> {
        return if (params.source == SourceType.MOVIES)
            moviesRepository.searchRemotelyByName(params.query)
                    .flatMapSingle {
                        Flowable.fromIterable(it)
                                .map(::DisplayableItem)
                                .toList()
                    }
        else
            tvSeriesRepository.searchRemotelyByName(params.query)
                    .flatMapSingle {
                        Flowable.fromIterable(it)
                                .map(::DisplayableItem)
                                .toList()
                    }

    }

    private fun constructUiModel(searchResult: Resource<List<DisplayableItem>>): SearchUiModel {
        var message: String? = null

        if (searchResult.status != Resource.Status.LOADING
                && (searchResult.data == null || searchResult.data.isEmpty())) {

            val query = searchQuerySubject.value
            if (!TextUtils.isEmpty(query)) {
                message = "No result for this query in the selected categories. Try a different query and category."
            }
        }

        return SearchUiModel(searchResult.data, message, searchResult.status == Resource.Status.LOADING)
    }

    private fun onNetworkChanged(info: NetworkInfo?) {
        isOnlineSubject.value = info != null && info.isConnected
    }

    override fun onCleared() {
        super.onCleared()
        app.unregisterReceiver(networkStateReceiver)
    }

}
