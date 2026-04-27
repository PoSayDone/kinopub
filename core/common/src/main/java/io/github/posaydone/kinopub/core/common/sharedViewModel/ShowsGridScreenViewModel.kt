package io.github.posaydone.kinopub.core.common.sharedViewModel

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.posaydone.kinopub.core.data.ShowRepository
import io.github.posaydone.kinopub.core.model.HistoryShow
import io.github.posaydone.kinopub.core.model.Show
import io.github.posaydone.kinopub.core.model.ShowList
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubContentType
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubCountry
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubGenre
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubGenreType
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubPeriod
import io.github.posaydone.kinopub.core.model.kinopub.KinoPubSort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "ShowsGridVM"

enum class ShowsGridQueryType { FAVORITES, HISTORY, CATALOG, WATCHING }

data class ShowsGridNavKey(
    val queryType: ShowsGridQueryType,
    val title: String = "",
    val contentType: String? = null,
    val sort: String = KinoPubSort.UPDATED,
    val period: String = KinoPubPeriod.MONTH,
)

enum class WatchingFilter(val label: String) {
    ALL("Все"), MOVIES("Фильмы"), SERIALS("Сериалы")
}

enum class CatalogSort(val apiValue: String, val label: String) {
    WATCHERS(KinoPubSort.WATCHERS, "Популярные"),
    UPDATED(KinoPubSort.UPDATED, "По обновлению"),
    CREATED(KinoPubSort.CREATED, "Новые"),
    RATING(KinoPubSort.RATING, "По рейтингу"),
    VIEWS(KinoPubSort.VIEWS, "По просмотрам"),
    KINOPOISK(KinoPubSort.KINOPOISK, "По Кинопоиску"),
    IMDB(KinoPubSort.IMDB, "По IMDb"),
}

enum class CatalogPeriod(val apiValue: String?, val label: String) {
    WEEK(KinoPubPeriod.WEEK, "Неделя"),
    MONTH(KinoPubPeriod.MONTH, "Месяц"),
    THREE_MONTHS(KinoPubPeriod.THREE_MONTHS, "3 месяца"),
    SIX_MONTHS(KinoPubPeriod.SIX_MONTHS, "6 месяцев"),
    YEAR(KinoPubPeriod.YEAR, "Год"),
    ALL(null, "Всё время"),
}

data class ContentTypeOption(val apiValue: String?, val label: String)

val allContentTypes = listOf(
    ContentTypeOption(null, "Все"),
    ContentTypeOption(KinoPubContentType.MOVIE, "Фильмы"),
    ContentTypeOption(KinoPubContentType.SERIAL, "Сериалы"),
    ContentTypeOption(KinoPubContentType.CONCERT, "Концерты"),
    ContentTypeOption(KinoPubContentType.FILM_3D, "3D"),
    ContentTypeOption(KinoPubContentType.DOCUMOVIE, "Докум. фильмы"),
    ContentTypeOption(KinoPubContentType.DOCUSERIAL, "Докум. сериалы"),
    ContentTypeOption(KinoPubContentType.TVSHOW, "ТВ Шоу"),
)

fun genreTypeForContentType(contentType: String?): String? = when (contentType) {
    KinoPubContentType.MOVIE, KinoPubContentType.SERIAL, KinoPubContentType.FILM_3D -> KinoPubGenreType.MOVIE
    KinoPubContentType.CONCERT -> KinoPubGenreType.MUSIC
    KinoPubContentType.DOCUMOVIE, KinoPubContentType.DOCUSERIAL -> KinoPubGenreType.DOCU
    KinoPubContentType.TVSHOW -> KinoPubGenreType.TVSHOW
    else -> null
}

@HiltViewModel(assistedFactory = ShowsGridScreenViewModel.Factory::class)
class ShowsGridScreenViewModel @AssistedInject constructor(
    @Assisted private val navKey: ShowsGridNavKey,
    private val repository: ShowRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(navKey: ShowsGridNavKey): ShowsGridScreenViewModel
    }

    private val _uiState = MutableStateFlow<ShowsGridUiState>(ShowsGridUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _queryType = MutableStateFlow(ShowsGridQueryType.HISTORY)
    val queryType = _queryType.asStateFlow()

    val screenTitle: String

    // CATALOG filter state
    private val _catalogContentType = MutableStateFlow<String?>(null)
    val catalogContentType = _catalogContentType.asStateFlow()

    private val _catalogSort = MutableStateFlow(CatalogSort.UPDATED)
    val catalogSort = _catalogSort.asStateFlow()

    private val _catalogPeriod = MutableStateFlow(CatalogPeriod.MONTH)
    val catalogPeriod = _catalogPeriod.asStateFlow()

    // Genre / country filter state
    private val _genres = MutableStateFlow<List<KinoPubGenre>>(emptyList())
    val genres = _genres.asStateFlow()

    private val _countries = MutableStateFlow<List<KinoPubCountry>>(emptyList())
    val countries = _countries.asStateFlow()

    private val _selectedGenreIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedGenreIds = _selectedGenreIds.asStateFlow()

    private val _selectedCountryIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedCountryIds = _selectedCountryIds.asStateFlow()

    // WATCHING filter state
    private val _watchingFilter = MutableStateFlow(WatchingFilter.ALL)
    val watchingFilter = _watchingFilter.asStateFlow()

    // Watching data (loaded once, filtered in-memory)
    private var watchingMovies: List<Show> = emptyList()
    private var watchingSerials: List<Show> = emptyList()

    // Genres cache keyed by genre type string
    private val genresCache = mutableMapOf<String?, List<KinoPubGenre>>()

    private var currentPage = 1
    private var isLoading = false
    private var hasReachedEnd = false
    private val internalQueryType: ShowsGridQueryType

    init {
        internalQueryType = navKey.queryType
        _queryType.value = internalQueryType
        screenTitle = navKey.title

        Log.d(
            TAG,
            "init: queryType=$internalQueryType title=$screenTitle contentType=${navKey.contentType} sort=${navKey.sort} period=${navKey.period}"
        )

        if (internalQueryType == ShowsGridQueryType.CATALOG) {
            _catalogContentType.value = navKey.contentType
            _catalogSort.value = CatalogSort.entries.firstOrNull { it.apiValue == navKey.sort }
                ?: CatalogSort.UPDATED
            _catalogPeriod.value = CatalogPeriod.entries.firstOrNull { it.apiValue == navKey.period }
                ?: CatalogPeriod.MONTH

            Log.d(
                TAG,
                "init catalog filters: contentType=${_catalogContentType.value} sort=${_catalogSort.value.apiValue} period=${_catalogPeriod.value.apiValue}"
            )

            loadGenresForContentType(navKey.contentType)
            loadCountries()
        }

        when (internalQueryType) {
            ShowsGridQueryType.WATCHING -> loadWatchingData()
            ShowsGridQueryType.HISTORY -> loadInitialHistoryData()
            else -> loadInitialShowsData()
        }
    }

    // — WATCHING —

    private fun loadWatchingData() {
        _uiState.value = ShowsGridUiState.Loading
        viewModelScope.launch {
            try {
                watchingMovies = repository.getWatchingMovies()
                watchingSerials = repository.getWatchingSerials()
                applyWatchingFilter()
            } catch (e: Exception) {
                _uiState.value = ShowsGridUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun applyWatchingFilter() {
        val shows = when (_watchingFilter.value) {
            WatchingFilter.ALL -> (watchingSerials + watchingMovies).distinctBy { it.id }
            WatchingFilter.MOVIES -> watchingMovies
            WatchingFilter.SERIALS -> watchingSerials
        }
        _uiState.value = ShowsGridUiState.ShowsSuccess(shows = shows, hasNextPage = false)
    }

    fun setWatchingFilter(filter: WatchingFilter) {
        if (_watchingFilter.value == filter) return
        _watchingFilter.value = filter
        applyWatchingFilter()
    }

    // — CATALOG filters —

    fun setCatalogContentType(contentType: String?) {
        if (_catalogContentType.value == contentType) return
        Log.d(
            TAG,
            "setCatalogContentType: old=${_catalogContentType.value} new=$contentType, clearing selected genres"
        )
        _catalogContentType.value = contentType
        _selectedGenreIds.value = emptySet()
        loadGenresForContentType(contentType)
        reload()
    }

    fun setCatalogSort(sort: CatalogSort) {
        if (_catalogSort.value == sort) return
        _catalogSort.value = sort
        reload()
    }

    fun setCatalogPeriod(period: CatalogPeriod) {
        if (_catalogPeriod.value == period) return
        _catalogPeriod.value = period
        reload()
    }

    fun setGenre(genreId: Int?) {
        val newIds = if (genreId == null) {
            emptySet()
        } else {
            val current = _selectedGenreIds.value
            if (genreId in current) current - genreId else current + genreId
        }
        if (_selectedGenreIds.value == newIds) return
        _selectedGenreIds.value = newIds
        reload()
    }

    fun setCountry(countryId: Int?) {
        val newIds = if (countryId == null) {
            emptySet()
        } else {
            val current = _selectedCountryIds.value
            if (countryId in current) current - countryId else current + countryId
        }
        if (_selectedCountryIds.value == newIds) return
        _selectedCountryIds.value = newIds
        reload()
    }

    // — Genre / country loading —

    private fun loadGenresForContentType(contentType: String?) {
        val genreType = genreTypeForContentType(contentType)

        Log.d(
            TAG,
            "loadGenresForContentType: contentType=$contentType genreType=$genreType cacheHit=${genresCache.containsKey(genreType)}"
        )

        genresCache[genreType]?.let { cachedGenres ->
            _genres.value = cachedGenres
            Log.d(
                TAG,
                "loadGenresForContentType: using cached genres count=${cachedGenres.size} preview=${cachedGenres.take(5).joinToString { "${it.id}:${it.title}" }}"
            )
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "loadGenresForContentType: requesting genres for genreType=$genreType")
                val loaded = repository.getGenres(genreType)
                genresCache[genreType] = loaded
                _genres.value = loaded

                Log.d(
                    TAG,
                    "loadGenresForContentType: loaded genres count=${loaded.size} preview=${loaded.take(5).joinToString { "${it.id}:${it.title}" }}"
                )
            } catch (error: Exception) {
                _genres.value = emptyList()
                Log.e(
                    TAG,
                    "loadGenresForContentType: failed for contentType=$contentType genreType=$genreType",
                    error
                )
            }
        }
    }

    private fun loadCountries() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "loadCountries: requesting countries")
                val loaded = repository.getCountries()
                _countries.value = loaded

                Log.d(
                    TAG,
                    "loadCountries: loaded countries count=${loaded.size} preview=${loaded.take(5).joinToString { "${it.id}:${it.title}" }}"
                )
            } catch (error: Exception) {
                _countries.value = emptyList()
                Log.e(TAG, "loadCountries: failed", error)
            }
        }
    }

    // — Pagination —

    private fun reload() {
        currentPage = 1
        hasReachedEnd = false
        isLoading = false
        when (internalQueryType) {
            ShowsGridQueryType.HISTORY -> loadInitialHistoryData()
            ShowsGridQueryType.WATCHING -> loadWatchingData()
            else -> loadInitialShowsData()
        }
    }

    fun retry() {
        reload()
    }

    private fun loadInitialShowsData() {
        if (isLoading) return
        isLoading = true
        _uiState.value = ShowsGridUiState.Loading

        viewModelScope.launch {
            try {
                val shows = fetchShowsPage(currentPage)
                hasReachedEnd = shows.isEmpty()
                _uiState.value = ShowsGridUiState.ShowsSuccess(
                    shows = shows,
                    hasNextPage = !hasReachedEnd,
                )
                currentPage = 2
            } catch (e: Exception) {
                _uiState.value = ShowsGridUiState.Error(e.message ?: "Unknown error")
            } finally {
                isLoading = false
            }
        }
    }

    private fun loadInitialHistoryData() {
        if (isLoading) return
        isLoading = true
        _uiState.value = ShowsGridUiState.Loading

        viewModelScope.launch {
            try {
                val historyShows = fetchHistoryPage(currentPage)
                hasReachedEnd = historyShows.isEmpty()
                _uiState.value = ShowsGridUiState.HistorySuccess(
                    historyShows = historyShows,
                    hasNextPage = !hasReachedEnd,
                )
                currentPage = 2
            } catch (e: Exception) {
                _uiState.value = ShowsGridUiState.Error(e.message ?: "Unknown error")
            } finally {
                isLoading = false
            }
        }
    }

    fun loadNextPage() {
        when (internalQueryType) {
            ShowsGridQueryType.HISTORY -> loadNextHistoryPage()
            ShowsGridQueryType.WATCHING -> Unit
            else -> loadNextShowsPage()
        }
    }

    private fun loadNextShowsPage() {
        if (isLoading || hasReachedEnd) return
        isLoading = true

        viewModelScope.launch {
            val currentState = _uiState.value as? ShowsGridUiState.ShowsSuccess ?: run {
                isLoading = false
                return@launch
            }
            try {
                val newShows = fetchShowsPage(currentPage)
                hasReachedEnd = newShows.isEmpty()
                val merged = (currentState.shows + newShows).distinctBy { it.id }
                _uiState.value = currentState.copy(
                    shows = merged,
                    hasNextPage = !hasReachedEnd,
                )
                if (!hasReachedEnd) currentPage++
            } catch (e: Exception) {
                _uiState.value = ShowsGridUiState.Error(e.message ?: "Unknown error")
            } finally {
                isLoading = false
            }
        }
    }

    private fun loadNextHistoryPage() {
        if (isLoading || hasReachedEnd) return
        isLoading = true

        viewModelScope.launch {
            val currentState = _uiState.value as? ShowsGridUiState.HistorySuccess ?: run {
                isLoading = false
                return@launch
            }
            try {
                val newShows = fetchHistoryPage(currentPage)
                hasReachedEnd = newShows.isEmpty()
                val merged = (currentState.historyShows + newShows).distinctBy { it.id }
                _uiState.value = currentState.copy(
                    historyShows = merged,
                    hasNextPage = !hasReachedEnd,
                )
                if (!hasReachedEnd) currentPage++
            } catch (e: Exception) {
                _uiState.value = ShowsGridUiState.Error(e.message ?: "Unknown error")
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun fetchShowsPage(page: Int): ShowList = when (internalQueryType) {
        ShowsGridQueryType.FAVORITES -> repository.getFavoritesPage(page = page)
        ShowsGridQueryType.HISTORY -> emptyList()
        ShowsGridQueryType.CATALOG -> {
            val sort = _catalogSort.value
            val periodApiValue = _catalogPeriod.value.apiValue.takeIf {
                sort == CatalogSort.WATCHERS || sort == CatalogSort.VIEWS
            }
            repository.getCatalogPage(
                contentType = _catalogContentType.value,
                sort = sort.apiValue,
                period = periodApiValue,
                genreIds = _selectedGenreIds.value,
                countryIds = _selectedCountryIds.value,
                page = page,
            )
        }

        ShowsGridQueryType.WATCHING -> emptyList()
    }

    private suspend fun fetchHistoryPage(page: Int): List<HistoryShow> {
        return repository.getHistoryList(limit = 48, page = page)
    }
}

@Immutable
sealed interface ShowsGridUiState {
    data object Loading : ShowsGridUiState
    data class Error(val message: String) : ShowsGridUiState
    data class ShowsSuccess(
        val shows: ShowList,
        val hasNextPage: Boolean,
    ) : ShowsGridUiState

    data class HistorySuccess(
        val historyShows: List<HistoryShow>,
        val hasNextPage: Boolean,
    ) : ShowsGridUiState
}
