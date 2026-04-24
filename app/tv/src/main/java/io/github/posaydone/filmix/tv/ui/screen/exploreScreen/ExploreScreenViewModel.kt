package io.github.posaydone.filmix.tv.ui.screen.exploreScreen

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.posaydone.filmix.core.data.ShowRepository
import io.github.posaydone.filmix.core.model.ShowList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ExploreScreenViewModel @Inject constructor(
    private val repository: ShowRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Initial)
    val searchState = _searchState.asStateFlow()

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // 300ms debounce
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        // If query is empty, emit initial state
                        kotlinx.coroutines.flow.flowOf(SearchState.Initial)
                    } else {
                        // Emit searching state first
                        _searchState.value = SearchState.Searching
                        
                        // Then get and emit results
                        val result = repository.getShowsListWithQuery(query = query)
                        kotlinx.coroutines.flow.flowOf(SearchState.Done(result))
                    }
                }
                .collect { state ->
                    _searchState.value = state
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

@Immutable
sealed interface SearchState {
    data object Initial : SearchState
    data object Searching : SearchState
    data class Done(val showList: ShowList) : SearchState
}
