package dev.yezi.anniversary.feature.todo.domain

import dev.yezi.anniversary.feature.todo.data.TodoTaskEntity
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    fun observeTasks(): Flow<List<TodoTaskEntity>>
}
