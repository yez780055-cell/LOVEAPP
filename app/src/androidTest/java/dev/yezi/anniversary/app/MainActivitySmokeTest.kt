package dev.yezi.anniversary.app

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.assertTextEquals
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivitySmokeTest {
    @get:Rule val permission: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun todoCreateEditCompleteRestoreAndTabsSurviveFastSwitches() {
        val title = "Smoke-${System.currentTimeMillis()}"
        compose.onNodeWithText("个人待办").assertIsDisplayed()
        compose.onNodeWithContentDescription("快速新增待办").performClick()
        compose.onNodeWithText("任务内容").performClick()
        compose.onNodeWithText("任务内容").performTextInput(title)
        compose.onNodeWithText("添加").performClick()
        compose.waitUntil(5_000) {
            runCatching {
                compose.onNodeWithTag("quick-todo-title").assertTextEquals("")
                true
            }.getOrDefault(false)
        }
        val secondTitle = "$title-repeat"
        compose.onNodeWithTag("quick-todo-title").performTextInput(secondTitle)
        compose.onNodeWithText("添加").performClick()
        compose.onNodeWithText("关闭").performClick()
        compose.waitUntil(5_000) {
            runCatching {
                compose.onNodeWithText(title).assertExists()
                true
            }.getOrDefault(false)
        }
        compose.onNodeWithText(secondTitle).assertIsDisplayed()
        compose.onNodeWithText(title).performClick()
        compose.onNodeWithText(title).performTextClearance()
        compose.onNodeWithText("任务内容").performTextInput("$title-edited")
        compose.onNodeWithText("保存").performClick()
        compose.onNodeWithText("$title-edited").assertIsDisplayed()
        compose.onNodeWithContentDescription("完成任务：$title-edited").performClick()
        compose.onNodeWithText("已完成 1 ⌄", substring = true).performClick()
        compose.onNodeWithText(secondTitle).assertIsDisplayed()
        compose.onNodeWithContentDescription("恢复任务：$title-edited").assertIsDisplayed().performClick()
        compose.onNodeWithContentDescription("小月历").performClick()
        compose.onNodeWithText("小月历模块迁移中").assertIsDisplayed()
        compose.onNodeWithContentDescription("纪念日").performClick()
        compose.onNodeWithText("记录一个重要的日子").assertIsDisplayed()
        compose.onNodeWithContentDescription("待办").performClick()
        compose.onNodeWithText("$title-edited").assertIsDisplayed()
        compose.onNodeWithContentDescription("小月历").performClick()
        compose.onNodeWithContentDescription("纪念日").performClick()
        compose.onNodeWithContentDescription("待办").performClick()
        compose.onNodeWithText("$title-edited").assertIsDisplayed()
    }
}
