package dev.yezi.anniversary.app

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivitySmokeTest {
    @get:Rule val permission: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun allFeatureTabsCanBeOpened() {
        compose.onNodeWithText("待办模块迁移中").assertIsDisplayed()
        compose.onAllNodesWithText("小月历").onLast().performClick()
        compose.onNodeWithText("小月历模块迁移中").assertIsDisplayed()
        compose.onAllNodesWithText("纪念日").onLast().performClick()
        compose.onNodeWithText("记录一个重要的日子").assertIsDisplayed()
        compose.onAllNodesWithText("待办").onLast().performClick()
        compose.onNodeWithText("待办模块迁移中").assertIsDisplayed()
    }
}
