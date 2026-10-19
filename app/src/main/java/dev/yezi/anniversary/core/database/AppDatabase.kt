package dev.yezi.anniversary.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.yezi.anniversary.feature.anniversary.data.BackgroundImageEntity
import dev.yezi.anniversary.feature.anniversary.data.CategoryEntity
import dev.yezi.anniversary.feature.anniversary.data.EventDao
import dev.yezi.anniversary.feature.anniversary.data.EventEntity
import dev.yezi.anniversary.feature.anniversary.data.MilestoneEntity
import dev.yezi.anniversary.feature.period.data.PeriodRecordEntity
import dev.yezi.anniversary.feature.period.data.PeriodSettingsEntity
import dev.yezi.anniversary.feature.todo.data.TodoTaskEntity

@Database(entities = [CategoryEntity::class, EventEntity::class, ReminderEntity::class, BackgroundImageEntity::class, MilestoneEntity::class, TodoTaskEntity::class, PeriodRecordEntity::class, PeriodSettingsEntity::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun todoDao(): TodoDao
    abstract fun periodDao(): PeriodDao

    companion object {
        @Volatile private var instance: AppDatabase? = null
        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "anniversary.db")
                .addMigrations(MIGRATION_1_2)
                .build().also { instance = it }
        }
    }
}

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE reminders ADD COLUMN sourceType TEXT NOT NULL DEFAULT 'anniversary'")
        db.execSQL("ALTER TABLE reminders ADD COLUMN sourceId TEXT NOT NULL DEFAULT ''")
        db.execSQL("UPDATE reminders SET sourceId = eventId WHERE sourceId = ''")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_sourceType_sourceId ON reminders(sourceType, sourceId)")
        db.execSQL("CREATE TABLE IF NOT EXISTS todo_tasks (id TEXT NOT NULL, title TEXT NOT NULL, dueDate TEXT, isComplete INTEGER NOT NULL, scope TEXT NOT NULL, PRIMARY KEY(id))")
        db.execSQL("CREATE TABLE IF NOT EXISTS period_records (id TEXT NOT NULL, startDate TEXT NOT NULL, endDate TEXT, PRIMARY KEY(id))")
        db.execSQL("CREATE TABLE IF NOT EXISTS period_settings (id TEXT NOT NULL, cycleLength INTEGER NOT NULL, periodLength INTEGER NOT NULL, PRIMARY KEY(id))")
    }
}
