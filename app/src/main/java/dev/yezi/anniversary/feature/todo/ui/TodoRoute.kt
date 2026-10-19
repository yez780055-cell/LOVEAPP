package dev.yezi.anniversary.feature.todo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.yezi.anniversary.feature.todo.presentation.TodoUiState
import dev.yezi.anniversary.feature.todo.presentation.TodoViewModel

@Composable
fun TodoRoute(viewModel: TodoViewModel) {
    val state by viewModel.uiState.collectAsState()
    TodoScreen(state)
}

@Composable
fun TodoScreen(state: TodoUiState) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("个人待办", style = MaterialTheme.typography.headlineSmall)
        Text("待办模块迁移中")
    }
}
