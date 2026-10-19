package dev.yezi.anniversary.feature.todo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import dev.yezi.anniversary.feature.todo.domain.TodoDraft
import dev.yezi.anniversary.feature.todo.domain.TodoItem
import dev.yezi.anniversary.feature.todo.presentation.TodoUiState
import dev.yezi.anniversary.feature.todo.presentation.TodoViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.app.TimePickerDialog

@Composable
fun TodoRoute(viewModel: TodoViewModel) {
    val state by viewModel.uiState.collectAsState()
    TodoScreen(state, TodoActions(
        setDate = viewModel::setDate, shiftDate = viewModel::shiftDate,
        setAll = viewModel::setAll, toggleCompleted = viewModel::toggleCompleted,
        toggle = viewModel::toggle, setCompleted = viewModel::setCompleted,
        save = viewModel::save, remove = viewModel::remove, createTag = viewModel::createTag,
    ))
}

@Composable
fun TodoScreen(state: TodoUiState, actions: TodoActions, initialDialog: TodoPreviewDialog? = null, previewItem: TodoItem? = null) {
    val pending = state.tasks.filter { it.completedAt == null }
    val completed = state.tasks.filter { it.completedAt != null }
    var editor by remember { mutableStateOf(if (initialDialog == TodoPreviewDialog.EDIT) previewItem else null) }
    var adding by remember { mutableStateOf(initialDialog == TodoPreviewDialog.CREATE) }
    var undoItem by remember { mutableStateOf<TodoItem?>(null) }
    LaunchedEffect(undoItem?.key) { if (undoItem != null) { delay(2_000); undoItem = null } }
    fun toggleTask(task: TodoItem) {
        if (task.completedAt == null) undoItem = task else if (undoItem?.key == task.key) undoItem = null
        actions.toggle(task)
    }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).pointerInput(state.all, state.date) {
        var distance = 0f
        detectHorizontalDragGestures(onDragEnd = { if (!state.all && kotlin.math.abs(distance) > 58f) actions.shiftDate(if (distance < 0) 1 else -1); distance = 0f }, onHorizontalDrag = { change, amount -> distance += amount; change.consume() })
    }) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("个人待办", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text("${pending.size} 项未完成 · ${prettyDate(if (state.all) LocalDate.now().toString() else state.date)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            FilterChip(selected = !state.all, onClick = { actions.setAll(false) }, label = { Text("按日期") })
            Spacer(Modifier.width(8.dp)); FilterChip(selected = state.all, onClick = { actions.setAll(true) }, label = { Text("全部") })
            Spacer(Modifier.weight(1f))
            TextButton(onClick = vm::toggleCompleted) { Text("已完成 ${completed.size} ${if (state.completedOpen) "⌃" else "⌄"}") }
        }
        if (!state.all) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { actions.shiftDate(-1) }) { Icon(Icons.Default.KeyboardArrowLeft, "前一天") }
                Text(prettyDate(state.date), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { actions.shiftDate(1) }) { Icon(Icons.Default.KeyboardArrowRight, "后一天") }
                if (state.date != LocalDate.now().toString()) TextButton(onClick = { actions.setDate(LocalDate.now().toString()) }) { Text("回到今天") }
            }
        }
        if (state.error != null) Text(state.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        if (pending.isEmpty() && (!state.completedOpen || completed.isEmpty())) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(if (state.all) "还没有未完成的待办。" else "这一天没有未完成的待办。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (pending.isEmpty()) item { Text(if (state.all) "还没有未完成的待办。" else "这一天没有未完成的待办。", Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                else if (state.all) {
                    pending.groupBy { it.date ?: "" }.forEach { (date, group) ->
                        item(key = "header-$date") { Text(if (date.isBlank()) "未设置日期" else prettyDate(date), Modifier.padding(top = 12.dp, bottom = 4.dp), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary) }
                        items(group, key = { it.key }) { task -> TodoRow(task, state.tags.associate { it.id to it.name }, { toggleTask(task) }, { editor = task }) }
                    }
                } else items(pending, key = { it.key }) { task -> TodoRow(task, state.tags.associate { it.id to it.name }, { toggleTask(task) }, { editor = task }) }
                if (state.completedOpen) {
                    item(key = "completed-heading") { Text("已完成", Modifier.padding(top = 16.dp, bottom = 4.dp), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    if (completed.isEmpty()) item { Text("还没有已完成的待办。", Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    else if (state.all) completed.groupBy { it.date ?: "" }.forEach { (date, group) ->
                        item(key = "completed-header-$date") { Text(if (date.isBlank()) "未设置日期" else prettyDate(date), Modifier.padding(top = 8.dp, bottom = 4.dp), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary) }
                        items(group, key = { "completed-${it.key}" }) { task -> TodoRow(task, state.tags.associate { it.id to it.name }, { toggleTask(task) }, { editor = task }) }
                    } else items(completed, key = { "completed-${it.key}" }) { task -> TodoRow(task, state.tags.associate { it.id to it.name }, { toggleTask(task) }, { editor = task }) }
                }
            }
        }
        undoItem?.let { item -> Snackbar(Modifier.fillMaxWidth().padding(bottom = 12.dp), action = { TextButton(onClick = { actions.setCompleted(item, false); undoItem = null }) { Text("撤销") } }) { Text("已完成") } }
    }
    ExtendedFloatingActionButton(onClick = { adding = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).semantics { contentDescription = "快速新增待办" }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("记一件事") }, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
    }
    if (adding) QuickTodoAddDialog(state, onClose = { adding = false }, onSave = actions.save)
    editor?.let { task -> TodoEditor(task, state, onClose = { editor = null }, onSave = { actions.save(it); editor = null }, onDelete = { actions.remove(task); editor = null }, onCreateTag = actions.createTag) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickTodoAddDialog(state: TodoUiState, onClose: () -> Unit, onSave: (TodoDraft, () -> Unit) -> Unit) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(if (state.all) LocalDate.now().toString() else state.date) }
    var noDate by remember { mutableStateOf(false) }
    var showDate by remember { mutableStateOf(false) }
    val picker = rememberDatePickerState(initialSelectedDateMillis = runCatching { LocalDate.parse(date).toEpochDay() * 86_400_000 }.getOrDefault(System.currentTimeMillis()))
    AlertDialog(onDismissRequest = onClose,
        title = { Text("快速添加") },
        text = { Column {
            OutlinedTextField(title, { title = it.take(500) }, Modifier.fillMaxWidth().testTag("quick-todo-title"), label = { Text("任务内容") }, singleLine = true)
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { showDate = true }, modifier = Modifier.weight(1f)) { Text(if (noDate) "选择日期" else prettyDate(date)) }
                TextButton(onClick = { noDate = !noDate }) { Text(if (noDate) "恢复日期" else "不设日期") }
            }
        } },
        confirmButton = { TextButton(onClick = { if (title.isNotBlank()) onSave(TodoDraft(title, if (noDate) null else date, null, "", emptyList())) { title = "" } }, enabled = title.isNotBlank()) { Text("添加") } },
        dismissButton = { TextButton(onClick = onClose) { Text("关闭") } }
    )
    if (showDate) DatePickerDialog(onDismissRequest = { showDate = false }, confirmButton = { TextButton(onClick = { picker.selectedDateMillis?.let { date = LocalDate.ofEpochDay(it / 86_400_000).toString(); noDate = false }; showDate = false }) { Text("确定") } }, dismissButton = { TextButton(onClick = { showDate = false }) { Text("取消") } }) { DatePicker(picker) }
}

@Composable
private fun TodoRow(item: TodoItem, labels: Map<String, String>, onToggle: () -> Unit, onEdit: () -> Unit) {
    Surface(Modifier.fillMaxWidth().clickable(onClick = onEdit), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onToggle, modifier = Modifier.size(42.dp).semantics { contentDescription = if (item.completedAt == null) "完成任务：${item.title}" else "恢复任务：${item.title}" }) {
                Surface(shape = CircleShape, color = if (item.completedAt != null) MaterialTheme.colorScheme.primary else Color.Transparent, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)) {
                    Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) { if (item.completedAt != null) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp)) }
                }
            }
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                Text(item.title, style = if (item.completedAt != null) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge, color = if (item.completedAt != null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                val meta = buildList { item.time?.let(::add); item.tags.mapNotNull { labels[it] }.forEach(::add) }
                if (meta.isNotEmpty()) Text(meta.joinToString("  ·  "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (item.completedAt != null) TextButton(onClick = onToggle) { Text("恢复") } else IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "编辑任务") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoEditor(item: TodoItem?, state: TodoUiState, onClose: () -> Unit, onSave: (TodoDraft) -> Unit, onDelete: () -> Unit, onCreateTag: (String, (dev.yezi.anniversary.feature.todo.presentation.TodoTagUi) -> Unit) -> Unit) {
    var title by remember(item?.key) { mutableStateOf(item?.title.orEmpty()) }
    var date by remember(item?.key) { mutableStateOf(item?.date ?: state.date) }
    var noDate by remember(item?.key) { mutableStateOf(item?.date == null && item != null) }
    var time by remember(item?.key) { mutableStateOf(item?.time.orEmpty()) }
    var note by remember(item?.key) { mutableStateOf(item?.note.orEmpty()) }
    var selectedTags by remember(item?.key) { mutableStateOf(item?.tags.orEmpty()) }
    var newTag by remember { mutableStateOf("") }
    var repeat by remember(item?.key) { mutableStateOf(item?.recurrence?.frequency ?: "none") }
    var reminder by remember(item?.key) { mutableStateOf(item?.reminderEnabled ?: false) }
    var minutes by remember(item?.key) { mutableIntStateOf(item?.reminderMinutes ?: 10) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var showReminderMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val dateState = rememberDatePickerState(initialSelectedDateMillis = runCatching { LocalDate.parse(date).toEpochDay() * 86_400_000 }.getOrDefault(System.currentTimeMillis()))
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth().padding(18.dp), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(20.dp).heightIn(max = 680.dp)) {
                Text(if (item == null) "新增待办" else "编辑待办", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(title, { title = it.take(500) }, Modifier.fillMaxWidth(), label = { Text("任务内容") }, singleLine = true)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) { Text(if (noDate) "选择日期" else prettyDate(date)) }
                    TextButton(onClick = { noDate = !noDate; if (noDate) { time = ""; reminder = false } }, enabled = item?.periodDate == null) { Text(if (noDate) "恢复日期" else "不设日期") }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = {
                        val parts = time.split(":"); val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9; val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
                        TimePickerDialog(context, { _, h, m -> time = "%02d:%02d".format(h, m) }, hour, minute, true).show()
                    }, enabled = !noDate) { Text(if (time.isBlank()) "设置时间（可选）" else "时间 $time") }
                    if (time.isNotBlank()) TextButton(onClick = { time = ""; reminder = false }) { Text("清除") }
                }
                Text("标签", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    state.tags.forEach { tag -> FilterChip(selected = tag.id in selectedTags, onClick = { selectedTags = if (tag.id in selectedTags) selectedTags - tag.id else selectedTags + tag.id }, label = { Text(tag.name) }) }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(newTag, { newTag = it.take(80) }, Modifier.weight(1f), label = { Text("新建标签") }, singleLine = true)
                    TextButton(onClick = { if (newTag.isNotBlank()) onCreateTag(newTag) { tag -> selectedTags = (selectedTags + tag.id).distinct(); newTag = "" } }) { Text("添加") }
                }
                OutlinedTextField(note, { note = it.take(5000) }, Modifier.fillMaxWidth(), label = { Text("备注") }, minLines = 2, maxLines = 4)
                if (repeat != "none") Text("重复周期已保留：${when(repeat) { "daily" -> "每天"; "weekly" -> "每周"; "monthly" -> "每月"; else -> "重复" }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(reminder, { reminder = it }, enabled = !noDate && date.isNotBlank() && time.isNotBlank()); Text("提醒")
                    if (reminder) {
                        Box { TextButton(onClick = { showReminderMenu = true }) { Text(if (minutes == 0) "准时" else "提前 $minutes 分钟") }
                            DropdownMenu(showReminderMenu, onDismissRequest = { showReminderMenu = false }) {
                                listOf(0, 5, 10, 15, 30, 60).forEach { value -> DropdownMenuItem(text = { Text(if (value == 0) "准时" else "提前 $value 分钟") }, onClick = { minutes = value; showReminderMenu = false }) }
                            }
                        }
                    }
                }
                if (showDelete) {
                    Text(if (item?.periodDate != null) "只会移除这一天的重复任务，其他周期会保留。" else "这会删除该待办及其周期记录。", color = MaterialTheme.colorScheme.error)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showDelete = false }) { Text("取消删除") }
                        TextButton(onClick = onDelete) { Text("确认删除", color = MaterialTheme.colorScheme.error) }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    if (item != null) IconButton(onClick = { showDelete = true }) { Icon(Icons.Default.Delete, "删除任务", tint = MaterialTheme.colorScheme.error) } else Spacer(Modifier.width(1.dp))
                    Row { TextButton(onClick = onClose) { Text("取消") }; Button(onClick = {
                        val taskDate = if (noDate) null else date
                        val rule = item?.recurrence
                        onSave(TodoDraft(title, taskDate, time.takeIf { it.isNotBlank() }, note, selectedTags, rule, reminder, minutes, item?.id, item?.periodDate))
                    }, enabled = title.isNotBlank()) { Text("保存") } }
                }
            }
        }
    }
    if (showDatePicker) DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = { TextButton(onClick = { dateState.selectedDateMillis?.let { date = LocalDate.ofEpochDay(it / 86_400_000).toString(); noDate = false }; showDatePicker = false }) { Text("确定") } }, dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }) { DatePicker(dateState) }
}

private fun prettyDate(value: String): String = runCatching { LocalDate.parse(value).format(DateTimeFormatter.ofPattern("M月d日 EEEE")) }.getOrDefault(value)
