package dev.yezi.anniversary.feature.period.ui

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
import dev.yezi.anniversary.feature.period.presentation.PeriodUiState
import dev.yezi.anniversary.feature.period.presentation.PeriodViewModel

@Composable
fun PeriodRoute(viewModel: PeriodViewModel) {
    val state by viewModel.uiState.collectAsState()
    PeriodScreen(state)
}

@Composable
fun PeriodScreen(state: PeriodUiState) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("小月历", style = MaterialTheme.typography.headlineSmall)
        Text("小月历模块迁移中")
    }
}
