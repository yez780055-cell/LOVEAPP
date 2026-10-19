package dev.yezi.anniversary.feature.todo.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_tasks")
data class TodoTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val dueDate: String? = null,
    val isComplete: Boolean = false,
    val scope: String = "personal"
)
