package dev.yezi.anniversary.feature.todo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo_tasks WHERE scope = 'personal' ORDER BY dueDate, createdAt") fun observeTasks(): Flow<List<TodoTaskEntity>>
    @Query("SELECT * FROM todo_tasks WHERE id = :id") suspend fun task(id: String): TodoTaskEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun putTask(task: TodoTaskEntity)
    @Query("DELETE FROM todo_tasks WHERE id = :id") suspend fun deleteTask(id: String)
    @Query("SELECT * FROM todo_tags ORDER BY name COLLATE NOCASE") fun observeTags(): Flow<List<TodoTagEntity>>
    @Query("SELECT * FROM todo_tags WHERE normalizedName = :normalized LIMIT 1") suspend fun tagByNormalizedName(normalized: String): TodoTagEntity?
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertTag(tag: TodoTagEntity)
    @Query("SELECT * FROM todo_occurrences") suspend fun occurrences(): List<TodoOccurrenceEntity>
    @Query("SELECT * FROM todo_occurrences WHERE taskId = :taskId AND periodDate = :date") suspend fun occurrence(taskId: String, date: String): TodoOccurrenceEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun putOccurrence(item: TodoOccurrenceEntity)
    @Query("DELETE FROM todo_occurrences WHERE taskId = :taskId") suspend fun deleteOccurrences(taskId: String)
    @Transaction suspend fun deleteTaskAndOccurrences(id: String) { deleteOccurrences(id); deleteTask(id) }
}
