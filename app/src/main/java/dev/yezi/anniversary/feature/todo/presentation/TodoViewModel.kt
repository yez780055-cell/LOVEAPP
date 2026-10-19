package dev.yezi.anniversary.feature.todo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.yezi.anniversary.feature.todo.data.TodoDao
import dev.yezi.anniversary.feature.todo.data.TodoTagEntity
import dev.yezi.anniversary.feature.todo.domain.RoomTodoRepository
import dev.yezi.anniversary.core.database.ReminderDao
import android.content.Context
import dev.yezi.anniversary.feature.todo.domain.TodoDraft
import dev.yezi.anniversary.feature.todo.domain.TodoItem
import dev.yezi.anniversary.feature.todo.domain.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TodoUiState(val tasks: List<TodoItem> = emptyList(), val tags: List<TodoTagEntity> = emptyList(), val date: String = LocalDate.now().toString(), val all: Boolean = false, val completedOpen: Boolean = false, val error: String? = null)

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = mutableState.asStateFlow()
    init { viewModelScope.launch { repository.observeTags().collect { mutableState.value = mutableState.value.copy(tags = it) } }; refresh() }
    fun setDate(date: String) { mutableState.value = mutableState.value.copy(date = date); refresh() }
    fun shiftDate(delta: Long) { setDate(runCatching { LocalDate.parse(mutableState.value.date).plusDays(delta).toString() }.getOrDefault(LocalDate.now().toString())) }
    fun setAll(all: Boolean) { mutableState.value = mutableState.value.copy(all = all, completedOpen = false); refresh() }
    fun toggleCompleted() { mutableState.value = mutableState.value.copy(completedOpen = !mutableState.value.completedOpen) }
    fun save(draft: TodoDraft) = perform { repository.save(draft) }
    fun remove(item: TodoItem) = perform { repository.remove(item) }
    fun toggle(item: TodoItem) = perform { repository.complete(item, item.completedAt == null) }
    fun setCompleted(item: TodoItem, done: Boolean) = perform { repository.complete(item, done) }
    fun createTag(name: String, onCreated: (TodoTagEntity) -> Unit) { viewModelScope.launch { runCatching { repository.createTag(name) }.onSuccess(onCreated).onFailure { mutableState.value = mutableState.value.copy(error = it.message) } } }
    private fun refresh() { viewModelScope.launch { runCatching { repository.instances(mutableState.value.date, mutableState.value.all) }.onSuccess { mutableState.value = mutableState.value.copy(tasks = it, error = null) }.onFailure { mutableState.value = mutableState.value.copy(error = it.message) } } }
    private fun perform(action: suspend () -> Any?) { viewModelScope.launch { runCatching { action() }.onFailure { mutableState.value = mutableState.value.copy(error = it.message ?: "操作失败") }; refresh() } }
    companion object {
        fun factory(dao: TodoDao, reminderDao: ReminderDao, context: Context) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = TodoViewModel(RoomTodoRepository(dao, reminderDao, context.applicationContext)) as T
        }
    }
}
