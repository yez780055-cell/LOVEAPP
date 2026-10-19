package dev.yezi.anniversary.feature.todo.domain

import dev.yezi.anniversary.feature.todo.data.TodoDao
import dev.yezi.anniversary.feature.todo.data.TodoOccurrenceEntity
import dev.yezi.anniversary.feature.todo.data.TodoTagEntity
import dev.yezi.anniversary.feature.todo.data.TodoTaskEntity
import dev.yezi.anniversary.feature.todo.data.TodoReminderSchedule
import dev.yezi.anniversary.core.database.ReminderDao
import dev.yezi.anniversary.core.notification.ReminderScheduler
import dev.yezi.anniversary.core.notification.ReminderScheduleSpec
import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

data class RepeatRule(val frequency: String, val interval: Int = 1, val startsOn: String, val endsOn: String? = null, val weekdays: List<Int> = emptyList())
data class TodoItem(val key: String, val id: String, val periodDate: String?, val title: String, val note: String, val date: String?, val time: String?, val tags: List<String>, val completedAt: Long?, val createdAt: Long, val recurrence: RepeatRule?, val reminderEnabled: Boolean, val reminderMinutes: Int)
data class TodoDraft(val title: String, val date: String?, val time: String?, val note: String, val tags: List<String>, val recurrence: RepeatRule? = null, val reminderEnabled: Boolean = false, val reminderMinutes: Int = 10, val id: String? = null, val periodDate: String? = null)

interface TodoRepository {
    fun observeTasks(): Flow<List<TodoTaskEntity>>
    fun observeTags(): Flow<List<TodoTagEntity>>
    suspend fun save(draft: TodoDraft)
    suspend fun remove(item: TodoItem)
    suspend fun complete(item: TodoItem, done: Boolean)
    suspend fun createTag(name: String): TodoTagEntity
    suspend fun instances(date: String, all: Boolean): List<TodoItem>
}

class RoomTodoRepository(private val dao: TodoDao, private val reminderDao: ReminderDao, private val context: Context) : TodoRepository {
    override fun observeTasks() = dao.observeTasks()
    override fun observeTags() = dao.observeTags()

    override suspend fun save(draft: TodoDraft) {
        require(draft.title.isNotBlank()) { "请先填写任务标题。" }
        require(draft.time == null || draft.date != null) { "具体时间需要先选择日期。" }
        require(!draft.reminderEnabled || (draft.date != null && draft.time != null)) { "提醒需要同时设置日期和具体时间。" }
        require(draft.recurrence == null || draft.date != null) { "重复任务需要设置开始日期。" }
        val old = draft.id?.let { dao.task(it) }
        val rule = draft.recurrence
        val id = old?.id ?: UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        if (draft.periodDate == null) {
            val saved = TodoTaskEntity(id, draft.title.trim(), draft.date, old?.isComplete ?: false, "personal", draft.note.trim(), draft.time, JSONArray(draft.tags.distinct()).toString(), old?.createdAt ?: now, now, old?.completedAt, rule?.let(::encodeRule), old?.recurrenceHistoryJson ?: "[]", if (rule != null) old?.seriesId ?: id else old?.seriesId, draft.reminderEnabled, draft.reminderMinutes.coerceIn(0, 1440))
            dao.putTask(saved)
            updateReminder(saved)
        }
        if (draft.periodDate != null) {
            val current = dao.occurrence(id, draft.periodDate) ?: TodoOccurrenceEntity(id, draft.periodDate, draft.date ?: draft.periodDate)
            dao.putOccurrence(current.copy(scheduledDate = draft.date ?: draft.periodDate, titleOverride = draft.title.trim(), noteOverride = draft.note.trim(), timeOverride = draft.time, tagsOverrideJson = JSONArray(draft.tags.distinct()).toString(), reminderEnabledOverride = draft.reminderEnabled, reminderMinutesBeforeOverride = draft.reminderMinutes.coerceIn(0, 1440), skipped = false, updatedAt = now))
        }
    }

    override suspend fun remove(item: TodoItem) {
        if (item.periodDate != null) {
            val existing = dao.occurrence(item.id, item.periodDate) ?: TodoOccurrenceEntity(item.id, item.periodDate, item.date ?: item.periodDate)
            dao.putOccurrence(existing.copy(skipped = true, updatedAt = System.currentTimeMillis()))
        } else {
            cancelReminder(item.id)
            dao.deleteTaskAndOccurrences(item.id)
        }
    }

    override suspend fun complete(item: TodoItem, done: Boolean) {
        val now = if (done) System.currentTimeMillis() else null
        if (item.periodDate != null) {
            val existing = dao.occurrence(item.id, item.periodDate) ?: TodoOccurrenceEntity(item.id, item.periodDate, item.date ?: item.periodDate)
            dao.putOccurrence(existing.copy(completedAt = now, updatedAt = System.currentTimeMillis()))
        } else dao.task(item.id)?.let { task ->
            val updated = task.copy(isComplete = done, completedAt = now, updatedAt = System.currentTimeMillis())
            dao.putTask(updated)
            if (done) cancelReminder(item.id) else updateReminder(updated)
        }
    }

    override suspend fun createTag(name: String): TodoTagEntity {
        val clean = name.trim().replace(Regex("\\s+"), " ")
        require(clean.isNotEmpty()) { "标签名称不能为空。" }
        val normalized = clean.lowercase()
        dao.tagByNormalizedName(normalized)?.let { return it }
        val tag = TodoTagEntity(UUID.randomUUID().toString(), clean, normalized)
        dao.insertTag(tag)
        return dao.tagByNormalizedName(normalized) ?: tag
    }

    override suspend fun instances(date: String, all: Boolean): List<TodoItem> {
        val tasks = dao.observeTasks().first()
        val records = dao.occurrences()
        val from = LocalDate.parse(date)
        val result = mutableListOf<TodoItem>()
        for (task in tasks) {
            val rules = parseRules(task)
            val taskOccurrences = records.filter { it.taskId == task.id }
            if (rules.isEmpty()) {
                if (all || task.dueDate == date) result += item(task, null, null)
                continue
            }
            if (all) {
                for (occ in taskOccurrences) if (!occ.skipped && rules.any { occurs(it, occ.periodDate) }) result += item(task, occ.periodDate, occ)
                decodeRule(task.recurrenceJson)?.let { rule ->
                    var d = maxOf(from, LocalDate.parse(rule.startsOn)); var guard = 0
                    while (d < from.plusYears(3) && guard++ < 1100 && rule.endsOn?.let { d <= LocalDate.parse(it) } != false) {
                        val key = d.toString()
                        if (occurs(rule, key) && taskOccurrences.none { it.periodDate == key }) { result += item(task, key, null); break }
                        d = d.plusDays(1)
                    }
                }
            } else {
                val occurrence = taskOccurrences.firstOrNull { it.periodDate == date }
                if (rules.any { occurs(it, date) } && occurrence?.skipped != true) result += item(task, date, occurrence)
                taskOccurrences.filter { it.scheduledDate == date && it.periodDate != date && !it.skipped }.forEach { result += item(task, it.periodDate, it) }
            }
            if (task.recurrenceJson == null && task.dueDate != null && (all || task.dueDate == date)) result += item(task, null, null)
        }
        val unique = result.distinctBy { it.key }
        return if (all) unique.sortedWith(compareBy<TodoItem> { it.date ?: "" }.thenBy { it.createdAt })
        else unique.sortedWith(compareBy<TodoItem> { it.date ?: "" }.thenBy { it.time ?: "" }.thenBy { it.createdAt })
    }

    private fun item(task: TodoTaskEntity, period: String?, occurrence: TodoOccurrenceEntity?): TodoItem = TodoItem(
        occurrence?.let { "${task.id}:${it.periodDate}" } ?: task.id, task.id, period,
        occurrence?.titleOverride ?: task.title, occurrence?.noteOverride ?: task.note,
        occurrence?.scheduledDate ?: task.dueDate, occurrence?.timeOverride ?: task.scheduledTime,
        parseStrings(occurrence?.tagsOverrideJson ?: task.tagsJson), occurrence?.completedAt ?: if (period == null) task.completedAt else null,
        task.createdAt, decodeRule(task.recurrenceJson), occurrence?.reminderEnabledOverride ?: task.reminderEnabled, occurrence?.reminderMinutesBeforeOverride ?: task.reminderMinutesBefore
    )
    private suspend fun updateReminder(task: TodoTaskEntity) {
        val old = reminderDao.getForSource(TodoReminderSchedule.SOURCE_TYPE, task.id)
        old.forEach { ReminderScheduler.cancel(context, it) }
        val spec = TodoReminderSchedule.resolve(task)
        if (spec.reminder.enabled && spec.triggerAtEpochMillis != null) {
            reminderDao.upsert(spec.reminder)
            ReminderScheduler.schedule(context, spec)
        } else reminderDao.deleteForSource(TodoReminderSchedule.SOURCE_TYPE, task.id)
    }
    private suspend fun cancelReminder(id: String) {
        reminderDao.getForSource(TodoReminderSchedule.SOURCE_TYPE, id).forEach { ReminderScheduler.cancel(context, it) }
        reminderDao.deleteForSource(TodoReminderSchedule.SOURCE_TYPE, id)
    }
    private fun parseRules(task: TodoTaskEntity): List<RepeatRule> = buildList {
        runCatching { JSONArray(task.recurrenceHistoryJson) }.getOrNull()?.let { arr -> for (i in 0 until arr.length()) decodeRule(arr.optJSONObject(i)?.toString())?.let(::add) }
        decodeRule(task.recurrenceJson)?.let(::add)
    }
    private fun parseStrings(raw: String?): List<String> = runCatching { JSONArray(raw ?: "[]").let { a -> (0 until a.length()).map { a.getString(it) } } }.getOrDefault(emptyList())
    private fun encodeRule(rule: RepeatRule) = JSONObject().put("frequency", rule.frequency).put("interval", rule.interval).put("startsOn", rule.startsOn).put("endsOn", rule.endsOn).put("weekdays", JSONArray(rule.weekdays)).toString()
    private fun decodeRule(raw: String?): RepeatRule? = runCatching { raw?.let { JSONObject(it).let { o -> RepeatRule(o.getString("frequency"), o.optInt("interval", 1), o.getString("startsOn"), o.optString("endsOn").takeIf(String::isNotBlank), o.optJSONArray("weekdays")?.let { a -> (0 until a.length()).map(a::getInt) } ?: emptyList()) } } }.getOrNull()
    private fun occurs(rule: RepeatRule, key: String): Boolean = runCatching {
        val d = LocalDate.parse(key); val start = LocalDate.parse(rule.startsOn)
        if (d < start || rule.endsOn?.let { d > LocalDate.parse(it) } == true) return@runCatching false
        when (rule.frequency) {
            "daily" -> ChronoUnit.DAYS.between(start, d) % rule.interval.coerceAtLeast(1) == 0L
            "weekly" -> ChronoUnit.WEEKS.between(start.with(java.time.DayOfWeek.MONDAY), d.with(java.time.DayOfWeek.MONDAY)) % rule.interval.coerceAtLeast(1) == 0L && (d.dayOfWeek.value % 7) in rule.weekdays
            "monthly" -> ChronoUnit.MONTHS.between(start.withDayOfMonth(1), d.withDayOfMonth(1)) % rule.interval.coerceAtLeast(1) == 0L && d.dayOfMonth == start.dayOfMonth
            else -> false
        }
    }.getOrDefault(false)
}
