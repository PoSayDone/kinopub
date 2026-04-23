package io.github.posaydone.filmix.core.common.sharedViewModel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.posaydone.filmix.core.data.KinopubRepository
import io.github.posaydone.filmix.core.model.ShowList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
sealed interface FavoritesScreenUiState {
    data object Loading : FavoritesScreenUiState
    data class Error(val message: String, val onRetry: () -> Unit) : FavoritesScreenUiState
    data class Done(
        val favoritesList: ShowList,
        val historyList: ShowList
    ) : FavoritesScreenUiState
}

@HiltViewModel
class FavoritesScreenViewModel @Inject constructor(
    private val repository: KinopubRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavoritesScreenUiState>(FavoritesScreenUiState.Loading)
    val uiState: StateFlow<FavoritesScreenUiState> = _uiState

    init {
        loadFavoritesAndHistory()
    }

    private fun loadFavoritesAndHistory() {
        _uiState.value = FavoritesScreenUiState.Loading

        viewModelScope.launch {
            combine(
                repository.getFavoritesList(), repository.getHistoryList()
            ) { favorites, history ->
                FavoritesScreenUiState.Done(
                    favoritesList = favorites, historyList = history
                )
            }.catch { e ->
                _uiState.value = FavoritesScreenUiState.Error(
                    message = e.message ?: "Unknown error", onRetry = { loadFavoritesAndHistory() })
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}