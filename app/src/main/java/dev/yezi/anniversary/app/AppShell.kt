package dev.yezi.anniversary.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavDestination.Companion.hierarchy
import dev.yezi.anniversary.core.design.LifeSpaceTheme
import dev.yezi.anniversary.feature.anniversary.presentation.EventViewModel
import dev.yezi.anniversary.feature.anniversary.ui.anniversaryGraph
import dev.yezi.anniversary.feature.period.presentation.PeriodViewModel
import dev.yezi.anniversary.feature.period.ui.PeriodRoute
import dev.yezi.anniversary.feature.settings.ui.SettingsRoute
import dev.yezi.anniversary.feature.todo.presentation.TodoViewModel
import dev.yezi.anniversary.feature.todo.ui.TodoRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShell(todoViewModel: TodoViewModel, periodViewModel: PeriodViewModel, anniversaryViewModel: EventViewModel, initialEventId: String? = null) {
    LifeSpaceTheme {
        val nav = rememberNavController()
        val entry by nav.currentBackStackEntryAsState()
        val destination = entry?.destination
        val tabRoutes = setOf(AppRoute.TODO_GRAPH, AppRoute.PERIOD_GRAPH, AppRoute.ANNIVERSARY_GRAPH)
        val showTabs = destination?.hierarchy?.any { it.route in tabRoutes } == true
        val onAnniversary = destination?.hierarchy?.any { it.route == AppRoute.ANNIVERSARY_GRAPH } == true
        val title = if (destination?.hierarchy?.any { it.route == AppRoute.PERIOD_GRAPH } == true) "小月历" else "待办"
        LaunchedEffect(initialEventId) { initialEventId?.let { nav.navigate(AppRoute.anniversaryDetail(it)) } }
        Scaffold(
            topBar = { if (showTabs && !onAnniversary) TopAppBar(title = { Text(title) }, actions = {
                IconButton(onClick = { nav.navigate(AppRoute.SETTINGS) }) { Icon(Icons.Default.Settings, contentDescription = "设置") }
            }) },
            bottomBar = { if (showTabs) NavigationBar {
                TabItem(nav, AppRoute.TODO_GRAPH, "待办", Icons.Default.CheckBox)
                TabItem(nav, AppRoute.PERIOD_GRAPH, "小月历", Icons.Default.CalendarMonth)
                TabItem(nav, AppRoute.ANNIVERSARY_GRAPH, "纪念日", Icons.Default.Favorite)
            } }
        ) { padding ->
            NavHost(nav, startDestination = AppRoute.TODO_GRAPH, modifier = Modifier.padding(padding)) {
                navigation(startDestination = AppRoute.TODO_HOME, route = AppRoute.TODO_GRAPH) {
                    composable(AppRoute.TODO_HOME) { TodoRoute(todoViewModel) }
                }
                navigation(startDestination = AppRoute.PERIOD_HOME, route = AppRoute.PERIOD_GRAPH) {
                    composable(AppRoute.PERIOD_HOME) { PeriodRoute(periodViewModel) }
                }
                navigation(startDestination = AppRoute.ANNIVERSARY_HOME, route = AppRoute.ANNIVERSARY_GRAPH) {
                    anniversaryGraph(nav, anniversaryViewModel)
                }
                composable(AppRoute.SETTINGS) { SettingsRoute(onBack = { nav.popBackStack() }) }
            }
        }
    }
}

@Composable
private fun TabItem(nav: NavHostController, route: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val entry by nav.currentBackStackEntryAsState()
    val selected = entry?.destination?.hierarchy?.any { it.route == route } == true
    NavigationBarItem(selected = selected, onClick = {
        nav.navigate(route) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }, icon = { Icon(icon, contentDescription = label) }, label = { Text(label) })
}
