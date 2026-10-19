package dev.yezi.anniversary.core.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppDatabaseMigrationTest {
    @Test fun v1AnniversaryDataSurvivesMigrationToSharedV3Schema() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "migration-" + System.nanoTime() + ".db"
        context.deleteDatabase(name)
        val path = context.getDatabasePath(name)
        path.parentFile?.mkdirs()
        val legacy = SQLiteDatabase.openOrCreateDatabase(path, null)
        try {
            legacy.execSQL("CREATE TABLE categories (id TEXT NOT NULL, name TEXT NOT NULL, sortOrder INTEGER NOT NULL, colorArgb INTEGER, PRIMARY KEY(id))")
            legacy.execSQL("CREATE TABLE events (id TEXT NOT NULL, title TEXT NOT NULL, targetDate TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, countingMode TEXT NOT NULL, includeDueDate INTEGER NOT NULL, repeatType TEXT NOT NULL, repeatInterval INTEGER NOT NULL, calendarType TEXT NOT NULL, timezone TEXT NOT NULL, categoryId TEXT, note TEXT NOT NULL, isPinned INTEGER NOT NULL, archivedAt INTEGER, trashedAt INTEGER, backgroundImageId TEXT, sortOrder INTEGER NOT NULL, PRIMARY KEY(id))")
            legacy.execSQL("CREATE INDEX index_events_categoryId ON events(categoryId)")
            legacy.execSQL("CREATE INDEX index_events_archivedAt ON events(archivedAt)")
            legacy.execSQL("CREATE INDEX index_events_trashedAt ON events(trashedAt)")
            legacy.execSQL("CREATE TABLE reminders (id TEXT NOT NULL, eventId TEXT NOT NULL, enabled INTEGER NOT NULL, triggerType TEXT NOT NULL, offsetDays INTEGER NOT NULL, localTime TEXT NOT NULL, exact INTEGER NOT NULL, nextTriggerAt INTEGER, notificationChannel TEXT NOT NULL, requestCode INTEGER NOT NULL, PRIMARY KEY(id))")
            legacy.execSQL("CREATE INDEX index_reminders_eventId ON reminders(eventId)")
            legacy.execSQL("CREATE TABLE background_images (id TEXT NOT NULL, eventId TEXT, uri TEXT NOT NULL, internalPath TEXT, cropLeft REAL NOT NULL, cropTop REAL NOT NULL, cropRight REAL NOT NULL, cropBottom REAL NOT NULL, rotationDegrees INTEGER NOT NULL, createdAt INTEGER NOT NULL, PRIMARY KEY(id))")
            legacy.execSQL("CREATE INDEX index_background_images_eventId ON background_images(eventId)")
            legacy.execSQL("CREATE TABLE milestones (id TEXT NOT NULL, eventId TEXT NOT NULL, type TEXT NOT NULL, value INTEGER NOT NULL, occurrenceDate TEXT NOT NULL, generated INTEGER NOT NULL, title TEXT, createdAt INTEGER NOT NULL, PRIMARY KEY(id))")
            legacy.execSQL("CREATE INDEX index_milestones_eventId ON milestones(eventId)")
            legacy.execSQL("CREATE TABLE todo_tasks (id TEXT NOT NULL, title TEXT NOT NULL, dueDate TEXT, isComplete INTEGER NOT NULL, scope TEXT NOT NULL, PRIMARY KEY(id))")
            legacy.execSQL("INSERT INTO todo_tasks (id, title, dueDate, isComplete, scope) VALUES ('todo-old', '旧待办', '2026-10-20', 1, 'personal')")
            legacy.execSQL("INSERT INTO events (id,title,targetDate,createdAt,updatedAt,countingMode,includeDueDate,repeatType,repeatInterval,calendarType,timezone,note,isPinned,sortOrder) VALUES ('event-1','test','2026-10-19',1,1,'countDown',0,'none',1,'solar','Asia/Shanghai','',0,0)")
            legacy.execSQL("INSERT INTO reminders (id,eventId,enabled,triggerType,offsetDays,localTime,exact,nextTriggerAt,notificationChannel,requestCode) VALUES ('reminder-1','event-1',1,'on_day',0,'09:00',1,NULL,'anniversary_reminders',11)")
            legacy.version = 1
        } finally {
            legacy.close()
        }

        val database = Room.databaseBuilder(context, AppDatabase::class.java, name)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()
        try {
            val migrated = database.openHelper.writableDatabase
            val reminderCursor = migrated.query("SELECT sourceType, sourceId, deliveryAction FROM reminders WHERE id = 'reminder-1'")
            reminderCursor.use {
                assertTrue(it.moveToFirst())
                assertEquals("anniversary", it.getString(0))
                assertEquals("event-1", it.getString(1))
                assertEquals("dev.yezi.anniversary.action.ANNIVERSARY_REMINDER", it.getString(2))
            }

            val columns = migrated.query("PRAGMA table_info(reminders)")
            val reminderColumns = mutableSetOf<String>()
            columns.use { while (it.moveToNext()) reminderColumns += it.getString(1) }
            assertFalse("eventId" in reminderColumns)
            assertTrue("sourceType" in reminderColumns)
            assertTrue("sourceId" in reminderColumns)

            val tables = migrated.query("SELECT name FROM sqlite_master WHERE type = 'table'")
            val names = mutableSetOf<String>()
            tables.use { while (it.moveToNext()) names += it.getString(0) }
            assertTrue("todo_tasks" in names)
            assertTrue("period_records" in names)
            assertTrue("period_settings" in names)

            val todoColumnsCursor = migrated.query("PRAGMA table_info(todo_tasks)")
            val todoColumns = mutableSetOf<String>()
            todoColumnsCursor.use { while (it.moveToNext()) todoColumns += it.getString(1) }
            assertTrue("note" in todoColumns)
            assertTrue("tagsJson" in todoColumns)
            assertTrue("recurrenceJson" in todoColumns)
            assertTrue("completedAt" in todoColumns)
            assertTrue("todo_tags" in names)
            assertTrue("todo_occurrences" in names)
            val oldTodo = migrated.query("SELECT title, dueDate, isComplete, completedAt FROM todo_tasks WHERE id = 'todo-old'")
            oldTodo.use { assertTrue(it.moveToFirst()); assertEquals("旧待办", it.getString(0)); assertEquals("2026-10-20", it.getString(1)); assertEquals(1, it.getInt(2)); assertTrue(it.getLong(3) > 0) }

            val events = migrated.query("SELECT title FROM events WHERE id = 'event-1'")
            events.use { assertTrue(it.moveToFirst()); assertEquals("test", it.getString(0)) }
        } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }
}
