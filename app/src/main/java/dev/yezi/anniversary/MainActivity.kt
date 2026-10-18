@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package dev.yezi.anniversary

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import dev.yezi.anniversary.data.EventEntity
import dev.yezi.anniversary.domain.CountingMode
import dev.yezi.anniversary.domain.DateEngine
import dev.yezi.anniversary.domain.RepeatType
import dev.yezi.anniversary.ui.EventViewModel
import java.time.LocalDate

private val Ink = Color(0xFF302A2A)
private val Muted = Color(0xFF7A6268)
private val Canvas = Color(0xFFFFF8F4)

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF9A6472), background = Canvas, surface = Color.White, onSurface = Ink)) {
                val repo = (application as AnniversaryApp).repository
                val vm: EventViewModel = viewModel(factory = EventViewModel.factory(repo))
                AnniversaryNav(vm, intent.getStringExtra("open_event_id"))
            }
        }
    }
}

@Composable private fun AnniversaryNav(vm: EventViewModel, openEventId: String?) {
    val nav = rememberNavController()
    val events by vm.events.collectAsState()
    LaunchedEffect(openEventId) { openEventId?.let { nav.navigate("detail/$it") } }
    NavHost(navController = nav, startDestination = "home") {
        composable("home") { EventList(events, onAdd = { nav.navigate("edit/new") }, onOpen = { nav.navigate("detail/$it") }, onSettings = { nav.navigate("settings") }) }
        composable("edit/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
            val id = entry.arguments?.getString("id") ?: "new"
            EventEditor(events.firstOrNull { it.id == id }, vm, onBack = { nav.popBackStack() }, onSave = { title, date, note, mode, include, repeat, reminder ->
                if (id == "new") vm.create(title, date, note, mode, include, repeat, reminder)
                else events.firstOrNull { it.id == id }?.let { vm.save(it.copy(title = title, targetDate = date, note = note, countingMode = mode, includeDueDate = include, repeatType = repeat), reminder) }
                nav.popBackStack()
            })
        }
        composable("detail/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
            val event = events.firstOrNull { it.id == entry.arguments?.getString("id") }
            if (event != null) EventDetail(event, onBack = { nav.popBackStack() }, onEdit = { nav.navigate("edit/${event.id}") }, onDelete = { vm.delete(event.id); nav.popBackStack() })
        }
        composable("settings") { SettingsPage(onBack = { nav.popBackStack() }) }
    }
}

@Composable private fun EventList(events: List<EventEntity>, onAdd: () -> Unit, onOpen: (String) -> Unit, onSettings: () -> Unit) {
    Scaffold(containerColor = Canvas, topBar = { TopAppBar(title = { Text("纪念日", color = Ink) }, actions = { TextButton(onClick = onSettings) { Text("设置") } }) },
        floatingActionButton = { FloatingActionButton(onClick = onAdd, containerColor = Color(0xFF9A6472), contentColor = Color.White) { Icon(Icons.Default.Add, "新增") } }) { pad ->
        if (events.isEmpty()) Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) { Text("记录一个重要的日子", color = Muted) }
        else LazyColumn(Modifier.fillMaxSize().padding(pad).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 14.dp)) {
            items(events, key = { it.id }) { event -> EventCard(event, Modifier.clickable { onOpen(event.id) }) }
        }
    }
}

@Composable private fun EventCard(event: EventEntity, modifier: Modifier = Modifier) {
    val eventToday = runCatching { LocalDate.now(java.time.ZoneId.of(event.timezone)) }.getOrDefault(LocalDate.now())
    val target = runCatching { LocalDate.parse(event.targetDate) }.getOrNull()?.let { anchor ->
        if (event.repeatType == "yearly") DateEngine.nextOccurrence(anchor, eventToday, RepeatType.YEARLY) else anchor
    }
    val mode = if (event.countingMode == "countUp") CountingMode.COUNT_UP else CountingMode.COUNT_DOWN
    val count = target?.let { DateEngine.dayCount(it, eventToday, mode, event.includeDueDate) }
    Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(event.title, style = MaterialTheme.typography.titleMedium, color = Ink); Text("${event.targetDate}  ·  ${event.note}", style = MaterialTheme.typography.bodySmall, color = Muted) }
            Column(horizontalAlignment = Alignment.End) { Text(count?.displayDays?.toString() ?: "—", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF9A6472)); Text(if (mode == CountingMode.COUNT_UP || count?.isPast == true) "已过" else "还有", style = MaterialTheme.typography.labelSmall, color = Muted) }
        }
    }
}

@Composable private fun EventEditor(event: EventEntity?, vm: EventViewModel, onBack: () -> Unit, onSave: (String, String, String, String, Boolean, String, Boolean) -> Unit) {
    var title by remember(event?.id) { mutableStateOf(event?.title.orEmpty()) }
    var date by remember(event?.id) { mutableStateOf(event?.targetDate ?: LocalDate.now().toString()) }
    var note by remember(event?.id) { mutableStateOf(event?.note.orEmpty()) }
    var mode by remember(event?.id) { mutableStateOf(event?.countingMode ?: "countDown") }
    var include by remember(event?.id) { mutableStateOf(event?.includeDueDate ?: false) }
    var repeat by remember(event?.id) { mutableStateOf(event?.repeatType ?: "none") }
    var reminder by remember(event?.id) { mutableStateOf(false) }
    var reminderLoaded by remember(event?.id) { mutableStateOf(event == null) }
    LaunchedEffect(event?.id) {
        event?.let { reminder = vm.hasEnabledReminder(it.id); reminderLoaded = true }
    }
    var error by remember { mutableStateOf<String?>(null) }
    Scaffold(topBar = { TopAppBar(title = { Text(if (event == null) "新增纪念日" else "编辑纪念日") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } }) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(title, { title = it }, label = { Text("名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(date, { date = it }, label = { Text("日期（YYYY-MM-DD）") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(note, { note = it }, label = { Text("备注") }, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) { Text("计数方式", Modifier.weight(1f)); FilterChip(mode == "countDown", { mode = "countDown" }, label = { Text("倒数") }); Spacer(Modifier.width(8.dp)); FilterChip(mode == "countUp", { mode = "countUp" }, label = { Text("正数") }) }
            Row(verticalAlignment = Alignment.CenterVertically) { Text("当天计入", Modifier.weight(1f)); Switch(include, { include = it }) }
            Row(verticalAlignment = Alignment.CenterVertically) { Text("重复", Modifier.weight(1f)); FilterChip(repeat == "none", { repeat = "none" }, label = { Text("不重复") }); Spacer(Modifier.width(8.dp)); FilterChip(repeat == "yearly", { repeat = "yearly" }, label = { Text("每年") }) }
            Row(verticalAlignment = Alignment.CenterVertically) { Text("当天提醒（09:00）", Modifier.weight(1f)); Switch(reminder, { reminder = it }) }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(enabled = reminderLoaded, onClick = { val parsed = runCatching { LocalDate.parse(date) }.getOrNull(); if (title.isBlank()) error = "请填写名称" else if (parsed == null) error = "日期格式应为 YYYY-MM-DD" else onSave(title.trim(), parsed.toString(), note, mode, include, repeat, reminder) }, modifier = Modifier.fillMaxWidth()) { Text("保存") }
        }
    }
}

@Composable private fun EventDetail(event: EventEntity, onBack: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text(event.title) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } }, actions = { IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "移入回收站") } }) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            EventCard(event)
            Text("日期：${event.targetDate}"); Text("重复：${if (event.repeatType == "yearly") "每年" else "不重复"}"); if (event.note.isNotBlank()) Text("备注：${event.note}")
            Button(onClick = onEdit) { Text("编辑") }
        }
    }
}

@Composable private fun SimplePage(title: String, onBack: () -> Unit, text: String) {
    Scaffold(topBar = { TopAppBar(title = { Text(title) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } }) }) { pad -> Box(Modifier.fillMaxSize().padding(pad).padding(24.dp), contentAlignment = Alignment.TopStart) { Text(text, color = Muted) } }
}

@Composable private fun SettingsPage(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Scaffold(topBar = { TopAppBar(title = { Text("设置") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } }) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("提醒", style = MaterialTheme.typography.titleMedium)
            Text("Android 13 及以上需要允许通知；Android 12 及以上的精确提醒受系统授权控制。")
            if (Build.VERSION.SDK_INT >= 31) {
                OutlinedButton(onClick = {
                    val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))
                    runCatching { context.startActivity(intent) }
                }) { Text("管理精确闹钟权限") }
            }
        }
    }
}
