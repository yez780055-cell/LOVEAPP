package dev.yezi.anniversary.feature.period.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.yezi.anniversary.feature.period.data.PeriodRecordEntity
import dev.yezi.anniversary.feature.period.domain.PeriodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

data class PeriodUiState(val records: List<PeriodRecordEntity> = emptyList())

class PeriodViewModel(private val repository: PeriodRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(PeriodUiState())
    val uiState: StateFlow<PeriodUiState> = mutableState.asStateFlow()
    init { viewModelScope.launch { repository.observeRecords().collect { mutableState.value = PeriodUiState(it.toList()) } } }
    companion object {
        fun factory(repository: PeriodRepository = EmptyPeriodRepository()) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = PeriodViewModel(repository) as T
        }
    }
}
private class EmptyPeriodRepository : PeriodRepository {
    override fun observeRecords() = flowOf(emptyList<PeriodRecordEntity>())
}
