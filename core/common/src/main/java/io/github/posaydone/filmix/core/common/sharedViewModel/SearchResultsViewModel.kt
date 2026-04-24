package io.github.posaydone.filmix.core.common.sharedViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.posaydone.filmix.core.data.ShowRepository
import io.github.posaydone.filmix.core.model.Show
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchResultsNavKey(val query: String)

@HiltViewModel(assistedFactory = SearchResultsViewModel.Factory::class)
class SearchResultsViewModel @AssistedInject constructor(
    @Assisted val navKey: SearchResultsNavKey,
    repository: ShowRepository,
) : ViewModel() {
   
    @AssistedFactory
    interface Factory {
        fun create(navKey: SearchResultsNavKey): SearchResultsViewModel
    }

    private val query = navKey.query

    val _shows = MutableStateFlow<List<Show>>(listOf())
    val shows = _shows.asStateFlow()

    val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading = _isLoading.asStateFlow()

    val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()


    init {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val result = repository.getShowsListWithQuery(query)
                _shows.value = result
            } catch (e: Exception) {
                _error.value = "Failed to load movies"
            } finally {
                _isLoading.value = false
            }
        }
    }
}