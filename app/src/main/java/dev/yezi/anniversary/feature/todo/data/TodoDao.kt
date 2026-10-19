package dev.yezi.anniversary.feature.todo.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo_tasks ORDER BY dueDate, title")
    fun observeTasks(): Flow<List<TodoTaskEntity>>
}
