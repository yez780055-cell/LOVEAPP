package dev.yezi.anniversary.feature.todo

import androidx.activity.ComponentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ActivityScenario
import com.github.takahirom.roborazzi.captureRoboImage
import dev.yezi.anniversary.core.design.LifeSpaceTheme
import dev.yezi.anniversary.feature.todo.presentation.TodoUiState
import dev.yezi.anniversary.feature.todo.ui.TodoActions
import dev.yezi.anniversary.feature.todo.ui.TodoPreviewDialog
import dev.yezi.anniversary.feature.todo.ui.TodoPreviewFixtures
import dev.yezi.anniversary.feature.todo.ui.TodoScreen
import java.io.File
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w393dp-h852dp")
class TodoScreenshotTest {
    private fun capture(name: String, state: TodoUiState, dialog: TodoPreviewDialog? = null, previewItem: dev.yezi.anniversary.feature.todo.domain.TodoItem? = null) {
        val directory = File(System.getProperty("loveapp.screenshots.dir", "build/ui-screenshots"))
        directory.mkdirs()
        val scenario = ActivityScenario.launch(ComponentActivity::class.java)
        try {
            scenario.captureRoboImage(File(directory, name)) {
                LifeSpaceTheme {
                    TodoScreen(state, TodoActions(), initialDialog = dialog, previewItem = previewItem)
                }
            }
        } finally {
            scenario.close()
        }
    }

    @Test fun todoHome() = capture("todo-home.png", state())

    @Test fun todoEmpty() = capture("todo-empty.png", state(tasks = emptyList()))

    @Test fun todoCompleted() = capture("todo-completed.png", state(tasks = listOf(TodoPreviewFixtures.shortTask, TodoPreviewFixtures.completedTask), completedOpen = true))

    @Test fun todoCreate() = capture("todo-create.png", state(), TodoPreviewDialog.CREATE)

    @Test fun todoEdit() = capture("todo-edit.png", state(), TodoPreviewDialog.EDIT, TodoPreviewFixtures.noteTask)

    @Test fun todoLongContent() = capture("todo-long-content.png", state(tasks = listOf(TodoPreviewFixtures.longTask)))

    private fun state(tasks: List<dev.yezi.anniversary.feature.todo.domain.TodoItem> = TodoPreviewFixtures.tasks, completedOpen: Boolean = false) =
        TodoUiState(tasks = tasks, tags = TodoPreviewFixtures.tags, date = TodoPreviewFixtures.date, completedOpen = completedOpen)
}
