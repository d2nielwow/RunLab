package com.dmirzayants.runlab.ui.runs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmirzayants.runlab.db.Run
import com.dmirzayants.runlab.domain.SortType
import com.dmirzayants.runlab.repository.RunRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RunsViewModel @Inject constructor(
    private val runRepository: RunRepository
) : ViewModel() {

    private val _sortType = MutableStateFlow(SortType.DATE)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val runs: StateFlow<List<Run>?> = _sortType
        .flatMapLatest { runRepository.getRuns(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun sortRuns(sortType: SortType) {
        _sortType.value = sortType
    }

    fun deleteRun(run: Run) = viewModelScope.launch {
        runRepository.deleteRun(run)
    }
}
