package dev.yezi.anniversary.feature.todo

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.yezi.anniversary.core.database.AppDatabase
import dev.yezi.anniversary.feature.todo.domain.RoomTodoRepository
import dev.yezi.anniversary.feature.todo.domain.RepeatRule
import dev.yezi.anniversary.feature.todo.domain.TodoDraft
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TodoRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repo: RoomTodoRepository

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        repo = RoomTodoRepository(db.todoDao(), db.reminderDao(), context)
    }
    @After fun tearDown() { db.close() }

    @Test fun crudCompletionUndoTagsAndDateFilteringPersistInRoom() = runBlocking {
        val tag = repo.createTag("工作")
        repo.save(TodoDraft("整理报表", "2026-10-19", "09:30", "本周数据", listOf(tag.id)))
        val monday = repo.instances("2026-10-19", false).single()
        assertEquals("整理报表", monday.title)
        assertEquals("09:30", monday.time)
        assertEquals(listOf(tag.id), monday.tags)
        repo.complete(monday, true)
        assertTrue(repo.instances("2026-10-19", false).single().completedAt != null)
        repo.complete(monday, false)
        assertEquals(null, repo.instances("2026-10-19", false).single().completedAt)
        assertTrue(repo.instances("2026-10-20", false).isEmpty())
        repo.save(TodoDraft("改名后的任务", "2026-10-19", null, "备注", listOf(tag.id), id = monday.id))
        assertEquals("改名后的任务", repo.instances("2026-10-19", true).single().title)
        repo.remove(repo.instances("2026-10-19", false).single())
        assertTrue(repo.instances("2026-10-19", true).isEmpty())
    }

    @Test fun recurringWeeklyOccurrencesSortAndCompleteIndependently() = runBlocking {
        repo.save(TodoDraft("每周复盘", "2026-10-19", "10:00", "", emptyList(), RepeatRule("weekly", 1, "2026-10-19", weekdays = listOf(1))))
        repo.save(TodoDraft("普通事项", "2026-10-19", "09:00", "", emptyList()))
        val items = repo.instances("2026-10-19", false)
        assertEquals(listOf("普通事项", "每周复盘"), items.map { it.title })
        repo.complete(items[1], true)
        assertTrue(repo.instances("2026-10-19", false).first { it.title == "每周复盘" }.completedAt != null)
        assertFalse(repo.instances("2026-10-26", false).first { it.title == "每周复盘" }.completedAt != null)
        assertEquals(3, repo.instances("2026-10-19", true).size)
        val next = repo.instances("2026-10-26", false).single()
        repo.save(TodoDraft("本周改名", next.date, next.time, "单次备注", next.tags, next.recurrence, next.reminderEnabled, next.reminderMinutes, next.id, next.periodDate))
        assertEquals("本周改名", repo.instances("2026-10-26", false).single().title)
        assertEquals("每周复盘", repo.instances("2026-11-02", false).single().title)
        repo.remove(repo.instances("2026-10-26", false).single())
        assertTrue(repo.instances("2026-10-26", false).isEmpty())
    }

    @Test fun changingOrStoppingARepeatRulePreservesEarlierCycles() = runBlocking {
        repo.save(TodoDraft("拉伸", "2026-10-12", null, "", emptyList(), RepeatRule("daily", 1, "2026-10-12")))
        val taskId = repo.instances("2026-10-12", false).single().id
        repo.complete(repo.instances("2026-10-13", false).single(), true)
        repo.save(TodoDraft("拉伸", "2026-10-15", null, "", emptyList(), RepeatRule("weekly", 1, "2026-10-12", weekdays = listOf(4)), id = taskId, effectiveFrom = "2026-10-15"))
        assertEquals(listOf("2026-10-12", "2026-10-13", "2026-10-14"), (12..14).map { "2026-10-${it}" }.filter { repo.instances(it, false).any { item -> item.title == "拉伸" } })
        assertTrue(repo.instances("2026-10-13", false).single().completedAt != null)
        assertEquals("2026-10-15", repo.instances("2026-10-15", false).single().date)

        repo.save(TodoDraft("拉伸", "2026-10-16", null, "", emptyList(), null, id = taskId, effectiveFrom = "2026-10-16"))
        assertEquals(1, repo.instances("2026-10-16", false).size)
        assertTrue(repo.instances("2026-10-17", false).isEmpty())
        assertEquals(1, repo.instances("2026-10-13", false).size)
    }

    @Test fun recurrenceIntervalsAndSkippedMonthlyDatesMatchCalendarRules() = runBlocking {
        repo.save(TodoDraft("隔周任务", "2026-10-19", null, "", emptyList(), RepeatRule("weekly", 2, "2026-10-19", weekdays = listOf(1))))
        repo.save(TodoDraft("月末任务", "2026-01-31", null, "", emptyList(), RepeatRule("monthly", 1, "2026-01-31")))
        assertTrue(repo.instances("2026-10-26", false).isEmpty())
        assertEquals("隔周任务", repo.instances("2026-11-02", false).single().title)
        assertEquals("月末任务", repo.instances("2026-01-31", false).single().title)
        assertTrue(repo.instances("2026-02-28", false).isEmpty())
        assertEquals("月末任务", repo.instances("2026-03-31", false).single().title)
    }
}
