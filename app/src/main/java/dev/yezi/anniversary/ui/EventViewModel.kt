package dev.yezi.anniversary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.yezi.anniversary.data.EventEntity
import dev.yezi.anniversary.data.EventRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EventViewModel(private val repository: EventRepository) : ViewModel() {
    val events = repository.activeEvents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val categories = repository.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun save(event: EventEntity, reminderEnabled: Boolean?) = viewModelScope.launch { repository.save(event, reminderEnabled) }
    fun create(title: String, date: String, note: String, mode: String, include: Boolean, repeat: String, reminderEnabled: Boolean) = viewModelScope.launch { repository.create(title, date, note, mode, include, repeat, reminderEnabled) }
    fun delete(id: String) = viewModelScope.launch { repository.delete(id) }
    suspend fun hasEnabledReminder(id: String) = repository.hasEnabledReminder(id)
    companion object {
        fun factory(repository: EventRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = EventViewModel(repository) as T
        }
    }
}
