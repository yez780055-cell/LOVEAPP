package dev.yezi.anniversary.feature.todo.ui

import dev.yezi.anniversary.feature.todo.domain.TodoDraft
import dev.yezi.anniversary.feature.todo.domain.TodoItem
import dev.yezi.anniversary.feature.todo.presentation.TodoTagUi

/** UI callbacks keep Compose rendering independent from ViewModel, Room, and Android services. */
data class TodoActions(
    val setDate: (String) -> Unit = {},
    val shiftDate: (Long) -> Unit = {},
    val setAll: (Boolean) -> Unit = {},
    val toggleCompleted: () -> Unit = {},
    val toggle: (TodoItem) -> Unit = {},
    val setCompleted: (TodoItem, Boolean) -> Unit = { _, _ -> },
    val save: (TodoDraft, () -> Unit) -> Unit = { _, onSuccess -> onSuccess() },
    val remove: (TodoItem) -> Unit = {},
    val createTag: (String, (TodoTagUi) -> Unit) -> Unit = { _, _ -> },
)

enum class TodoPreviewDialog { CREATE, EDIT }

/** Isolated sample content for previews and screenshot tests; never persisted. */
object TodoPreviewFixtures {
    const val date = "2026-10-19"
    val tags = listOf(
        TodoTagUi("work", "工作"),
        TodoTagUi("life", "生活"),
        TodoTagUi("urgent", "重要"),
    )
    val shortTask = TodoItem("sample-short", "sample-short", null, "整理本周计划", "", date, null, emptyList(), null, 1L, null, false, 10)
    val datedTask = TodoItem("sample-dated", "sample-dated", null, "给重要事项留出时间", "", date, "14:30", listOf("work"), null, 2L, null, false, 10)
    val taggedTask = TodoItem("sample-tags", "sample-tags", null, "检查待办标签", "", date, "16:00", listOf("work", "life", "urgent"), null, 3L, null, false, 10)
    val longTask = TodoItem("sample-long", "sample-long", null, "整理并复核本周跨团队项目进度、待确认事项和下一阶段安排", "", date, null, listOf("work", "urgent"), null, 4L, null, false, 10)
    val completedTask = TodoItem("sample-completed", "sample-completed", null, "完成每周资料归档", "", date, null, listOf("life"), 1_760_000_000_000L, 5L, null, false, 10)
    val noteTask = TodoItem("sample-note", "sample-note", null, "准备周会", "整理关键进度、风险和需要协助确认的问题。", date, "09:30", listOf("work"), null, 6L, null, true, 10)
    val tasks = listOf(shortTask, datedTask, taggedTask, longTask, noteTask)
}
