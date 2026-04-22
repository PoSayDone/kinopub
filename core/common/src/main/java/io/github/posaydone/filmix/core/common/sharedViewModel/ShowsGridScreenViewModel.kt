package io.github.posaydone.filmix.core.common.sharedViewModel

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.posaydone.filmix.core.data.FilmixRepository
import io.github.posaydone.filmix.core.model.ShowList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShowsGridScreenViewModel @Inject constructor(
    private val repository: FilmixRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShowsGridUiState>(ShowsGridUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _currentQueryType = MutableStateFlow(ShowsGridQueryType.HISTORY)
    val currentQueryType = _currentQueryType.asStateFlow()

    private var currentPage = 1
    private var isLoading = false
    private var hasReachedEnd = false
    private var internalQueryType = ShowsGridQueryType.HISTORY

    init {
        val queryType = savedStateHandle.get<String>("queryType") ?: ShowsGridQueryType.HISTORY.name
        internalQueryType = ShowsGridQueryType.valueOf(queryType)
        _currentQueryType.value = internalQueryType
        loadInitialData()
    }

    private fun loadInitialData() {
        if (isLoading) return
        isLoading = true
        _uiState.value = ShowsGridUiState.Loading

        viewModelScope.launch {
            try {
                val shows = when (internalQueryType) {
                    ShowsGridQueryType.FAVORITES -> repository.getFavoritesList(page = currentPage)
                        .first()

                    ShowsGridQueryType.HISTORY -> repository.getHistoryList(page = currentPage)
                        .first()
                }

                hasReachedEnd = shows.isEmpty()
                val showList = if (shows.isNotEmpty()) shows else emptyList()

                _uiState.value = ShowsGridUiState.Success(
                    shows = showList, hasNextPage = !hasReachedEnd
                )
                currentPage = 2
                isLoading = false
            } catch (e: Exception) {
                _uiState.value = ShowsGridUiState.Error(e.message ?: "Unknown error")
                isLoading = false
            }
        }
    }

    fun loadNextPage() {
        if (isLoading || hasReachedEnd) {
            // Add this log to see if we're skipping the load
            Log.d(
                "ViewModelDebug",
                "Skipping loadNextPage. isLoading=$isLoading, hasReachedEnd=$hasReachedEnd"
            )
            return
        }
        isLoading = true
        Log.d("ViewModelDebug", "====== Entering loadNextPage for page: $currentPage ======")

        viewModelScope.launch {
            val currentState = _uiState.value as? ShowsGridUiState.Success ?: run {
                isLoading = false
                return@launch
            }
            val oldSize = currentState.shows.size
            Log.d("ViewModelDebug", "Current list size is: $oldSize")

            try {
                val newShows = when (internalQueryType) {
                    ShowsGridQueryType.FAVORITES -> repository.getFavoritesList(page = currentPage)
                        .first()

                    ShowsGridQueryType.HISTORY -> repository.getHistoryList(page = currentPage)
                        .first()
                }
                Log.d("ViewModelDebug", "Fetched ${newShows.size} new shows.")
                // Log the ID of the first new show to confirm it's different data
                if (newShows.isNotEmpty()) {
                    Log.d("ViewModelDebug", "First new show ID: ${newShows.first().id}")
                }

                hasReachedEnd = newShows.isEmpty()

                val mergedShows = (currentState.shows + newShows).distinctBy { it.id }
                _uiState.value = currentState.copy(
                    shows = mergedShows,
                    hasNextPage = !hasReachedEnd
                )
                val newSize = (_uiState.value as ShowsGridUiState.Success).shows.size
                Log.d("ViewModelDebug", "UI State updated. New total list size is: $newSize")


                if (!hasReachedEnd) {
                    currentPage++
                }
            } catch (e: Exception) {
                Log.e("ViewModelDebug", "Error loading next page", e)
                _uiState.value = ShowsGridUiState.Error(e.message ?: "Unknown error")
            } finally {
                isLoading = false
            }
        }
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

enum class ShowsGridQueryType {
    FAVORITES, HISTORY
}
