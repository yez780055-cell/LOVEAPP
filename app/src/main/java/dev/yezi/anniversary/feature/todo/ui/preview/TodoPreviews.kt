package dev.yezi.anniversary.feature.todo.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.yezi.anniversary.core.design.LifeSpaceTheme
import dev.yezi.anniversary.feature.todo.presentation.TodoUiState
import dev.yezi.anniversary.feature.todo.ui.TodoActions
import dev.yezi.anniversary.feature.todo.ui.TodoPreviewDialog
import dev.yezi.anniversary.feature.todo.ui.TodoPreviewFixtures
import dev.yezi.anniversary.feature.todo.ui.TodoScreen

@Composable
private fun Showcase(dialog: TodoPreviewDialog? = null, completedOpen: Boolean = false, empty: Boolean = false, longContent: Boolean = false) {
    val fixture = TodoPreviewFixtures
    val tasks = when {
        empty -> emptyList()
        completedOpen -> listOf(fixture.shortTask, fixture.completedTask)
        longContent -> listOf(fixture.longTask)
        else -> fixture.tasks
    }
    LifeSpaceTheme {
        TodoScreen(
            state = TodoUiState(tasks = tasks, tags = fixture.tags, date = fixture.date, completedOpen = completedOpen),
            actions = TodoActions(),
            initialDialog = dialog,
            previewItem = fixture.noteTask,
        )
    }
}

@Preview(name = "Todo home", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun TodoHomePreview() = Showcase()

@Preview(name = "Todo empty", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun TodoEmptyPreview() = Showcase(empty = true)

@Preview(name = "Todo completed", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun TodoCompletedPreview() = Showcase(completedOpen = true)

@Preview(name = "Todo create", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun TodoCreatePreview() = Showcase(dialog = TodoPreviewDialog.CREATE)

@Preview(name = "Todo edit", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun TodoEditPreview() = Showcase(dialog = TodoPreviewDialog.EDIT)

@Preview(name = "Todo long title", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun TodoLongContentPreview() = Showcase(longContent = true)
