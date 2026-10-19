package dev.yezi.anniversary.feature.todo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.yezi.anniversary.feature.todo.data.TodoTaskEntity
import dev.yezi.anniversary.feature.todo.domain.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

data class TodoUiState(val tasks: List<TodoTaskEntity> = emptyList())

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = mutableState.asStateFlow()
    init { viewModelScope.launch { repository.observeTasks().collect { mutableState.value = TodoUiState(it.toList()) } } }
    companion object {
        fun factory(repository: TodoRepository = EmptyTodoRepository()) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = TodoViewModel(repository) as T
        }
    }
}
private class EmptyTodoRepository : TodoRepository {
    override fun observeTasks() = flowOf(emptyList<TodoTaskEntity>())
}
