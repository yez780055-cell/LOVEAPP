package dev.yezi.anniversary.feature.anniversary.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.yezi.anniversary.feature.anniversary.data.CategoryEntity
import dev.yezi.anniversary.feature.anniversary.data.EventEntity
import dev.yezi.anniversary.feature.anniversary.domain.AnniversaryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AnniversaryUiState(val events: List<EventEntity> = emptyList(), val categories: List<CategoryEntity> = emptyList(), val isLoading: Boolean = true)

class EventViewModel(private val repository: AnniversaryRepository) : ViewModel() {
    val uiState: StateFlow<AnniversaryUiState> = combine(repository.activeEvents, repository.categories) { events, categories ->
        AnniversaryUiState(events = events.toList(), categories = categories.toList(), isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnniversaryUiState())

    fun save(event: EventEntity, reminderEnabled: Boolean?) = viewModelScope.launch { repository.save(event, reminderEnabled) }
    fun create(title: String, date: String, note: String, mode: String, include: Boolean, repeat: String, reminderEnabled: Boolean) = viewModelScope.launch { repository.create(title, date, note, mode, include, repeat, reminderEnabled) }
    fun delete(id: String) = viewModelScope.launch { repository.delete(id) }
    suspend fun hasEnabledReminder(id: String) = repository.hasEnabledReminder(id)

    companion object {
        fun factory(repository: AnniversaryRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = EventViewModel(repository) as T
        }
    }
}
