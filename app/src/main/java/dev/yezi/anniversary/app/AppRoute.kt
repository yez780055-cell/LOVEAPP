package dev.yezi.anniversary.app

object AppRoute {
    const val TODO_GRAPH = "todo"
    const val TODO_HOME = "todo/home"
    const val PERIOD_GRAPH = "period"
    const val PERIOD_HOME = "period/home"
    const val ANNIVERSARY_GRAPH = "anniversary"
    const val ANNIVERSARY_HOME = "anniversary/home"
    const val ANNIVERSARY_EDIT = "anniversary/edit/{id}"
    const val ANNIVERSARY_DETAIL = "anniversary/detail/{id}"
    const val SETTINGS = "settings"
    fun anniversaryEdit(id: String) = "anniversary/edit/$id"
    fun anniversaryDetail(id: String) = "anniversary/detail/$id"
}
