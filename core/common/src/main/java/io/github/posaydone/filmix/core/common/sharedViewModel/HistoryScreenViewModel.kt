package io.github.posaydone.filmix.core.common.sharedViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.posaydone.filmix.core.data.ShowRepository
import io.github.posaydone.filmix.core.model.HistoryShow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HistoryScreenUiState {
    data object Loading : HistoryScreenUiState()
    data class Error(val message: String, val onRetry: () -> Unit) : HistoryScreenUiState()
    data class Done(
        val historyList: List<HistoryShow>,
    ) : HistoryScreenUiState()
}

@HiltViewModel
class HistoryScreenViewModel @Inject constructor(private val repository: ShowRepository) :
    ViewModel() {
    private val retryChannel = Channel<Unit>()

    val uiState = retryChannel.receiveAsFlow().flatMapLatest {
        repository.getHistoryListFull().map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
    }.map { result ->
        when {
            result.isSuccess -> HistoryScreenUiState.Done(historyList = result.getOrThrow())
            result.isFailure -> HistoryScreenUiState.Error(message = result.exceptionOrNull()?.message
                ?: "Unknown error",
                onRetry = { retry() })

            else -> HistoryScreenUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryScreenUiState.Loading
    )

    init {
        retry()
    }

    private fun retry() {
        viewModelScope.launch {
            retryChannel.send(Unit)
        }
    }
}
