package io.github.posaydone.filmix.core.common.sharedViewModel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.posaydone.filmix.core.data.KinopubRepository
import io.github.posaydone.filmix.core.model.Show
import io.github.posaydone.filmix.core.model.ShowList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ShowsGridQueryType { FAVORITES, HISTORY, CATALOG, WATCHING }

data class ShowsGridNavKey(
    val queryType: ShowsGridQueryType,
    val title: String = "",
    val contentType: String? = null,
    val sort: String = CatalogSort.VIEWS.apiValue,
    val period: String = CatalogPeriod.MONTH.apiValue ?: "",
)

enum class WatchingFilter(val label: String) {
    ALL("Все"), MOVIES("Фильмы"), SERIALS("Сериалы")
}

enum class CatalogSort(val apiValue: String, val label: String) {
    VIEWS("views", "По просмотрам"),
    ADDED("added", "По новизне"),
    UPDATED("updated-", "По обновлению"),
}

enum class CatalogPeriod(val apiValue: String?, val label: String) {
    MONTH("month", "Месяц"),
    THREE_MONTHS("3month", "3 месяца"),
    SIX_MONTHS("6month", "6 месяцев"),
    YEAR("year", "Год"),
    ALL(null, "Всё время"),
}

data class ContentTypeOption(val apiValue: String?, val label: String)

val allContentTypes = listOf(
    ContentTypeOption(null, "Все"),
    ContentTypeOption("movie", "Фильмы"),
    ContentTypeOption("serial", "Сериалы"),
    ContentTypeOption("concert", "Концерты"),
    ContentTypeOption("3d", "3D"),
    ContentTypeOption("documovie", "Докум. фильмы"),
    ContentTypeOption("docuserial", "Докум. сериалы"),
    ContentTypeOption("tvshow", "ТВ Шоу"),
)

@HiltViewModel(assistedFactory = ShowsGridScreenViewModel.Factory::class)
class ShowsGridScreenViewModel @AssistedInject constructor(
    @Assisted private val navKey: ShowsGridNavKey,
    private val repository: KinopubRepository,
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

    private val _catalogSort = MutableStateFlow(CatalogSort.VIEWS)
    val catalogSort = _catalogSort.asStateFlow()

    private val _catalogPeriod = MutableStateFlow(CatalogPeriod.MONTH)
    val catalogPeriod = _catalogPeriod.asStateFlow()

    // WATCHING filter state
    private val _watchingFilter = MutableStateFlow(WatchingFilter.ALL)
    val watchingFilter = _watchingFilter.asStateFlow()

    // Watching data (loaded once, filtered in-memory)
    private var watchingMovies: List<Show> = emptyList()
    private var watchingSerials: List<Show> = emptyList()

    private var currentPage = 1
    private var isLoading = false
    private var hasReachedEnd = false
    private val internalQueryType: ShowsGridQueryType

    init {
        internalQueryType = navKey.queryType
        _queryType.value = internalQueryType
        screenTitle = navKey.title

        if (internalQueryType == ShowsGridQueryType.CATALOG) {
            _catalogContentType.value = navKey.contentType
            _catalogSort.value = CatalogSort.entries.firstOrNull { it.apiValue == navKey.sort }
                ?: CatalogSort.VIEWS
            _catalogPeriod.value = CatalogPeriod.entries.firstOrNull { it.apiValue == navKey.period }
                ?: CatalogPeriod.MONTH
        }

        if (internalQueryType == ShowsGridQueryType.WATCHING) {
            loadWatchingData()
        } else {
            loadInitialData()
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
            WatchingFilter.ALL -> (watchingMovies + watchingSerials).distinctBy { it.id }
            WatchingFilter.MOVIES -> watchingMovies
            WatchingFilter.SERIALS -> watchingSerials
        }
        _uiState.value = ShowsGridUiState.Success(shows = shows, hasNextPage = false)
    }

    fun setWatchingFilter(filter: WatchingFilter) {
        if (_watchingFilter.value == filter) return
        _watchingFilter.value = filter
        applyWatchingFilter()
    }

    // — CATALOG filters —

    fun setCatalogContentType(contentType: String?) {
        if (_catalogContentType.value == contentType) return
        _catalogContentType.value = contentType
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

    // — Pagination —

    private fun reload() {
        currentPage = 1
        hasReachedEnd = false
        isLoading = false
        loadInitialData()
    }

    private fun loadInitialData() {
        if (isLoading) return
        isLoading = true
        _uiState.value = ShowsGridUiState.Loading

        viewModelScope.launch {
            try {
                val shows = fetchPage(currentPage)
                hasReachedEnd = shows.isEmpty()
                _uiState.value = ShowsGridUiState.Success(shows = shows, hasNextPage = !hasReachedEnd)
                currentPage = 2
            } catch (e: Exception) {
                _uiState.value = ShowsGridUiState.Error(e.message ?: "Unknown error")
            } finally {
                isLoading = false
            }
        }
    }

    fun loadNextPage() {
        if (isLoading || hasReachedEnd) return
        isLoading = true

        viewModelScope.launch {
            val currentState = _uiState.value as? ShowsGridUiState.Success ?: run {
                isLoading = false
                return@launch
            }
            try {
                val newShows = fetchPage(currentPage)
                hasReachedEnd = newShows.isEmpty()
                val merged = (currentState.shows + newShows).distinctBy { it.id }
                _uiState.value = currentState.copy(shows = merged, hasNextPage = !hasReachedEnd)
                if (!hasReachedEnd) currentPage++
            } catch (e: Exception) {
                _uiState.value = ShowsGridUiState.Error(e.message ?: "Unknown error")
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun fetchPage(page: Int): ShowList = when (internalQueryType) {
        ShowsGridQueryType.FAVORITES -> repository.getFavoritesPage(page = page).items
        ShowsGridQueryType.HISTORY -> repository.getHistoryPage(page = page).items
        ShowsGridQueryType.CATALOG -> repository.getCatalogPage(
            contentType = _catalogContentType.value,
            sort = _catalogSort.value.apiValue,
            period = _catalogPeriod.value.apiValue,
            page = page,
        ).items
        ShowsGridQueryType.WATCHING -> emptyList() // handled separately via loadWatchingData
    }
}

@Immutable
sealed interface ShowsGridUiState {
    data object Loading : ShowsGridUiState
    data class Error(val message: String) : ShowsGridUiState
    data class Success(
        val shows: ShowList,
        val hasNextPage: Boolean,
    ) : ShowsGridUiState
}
