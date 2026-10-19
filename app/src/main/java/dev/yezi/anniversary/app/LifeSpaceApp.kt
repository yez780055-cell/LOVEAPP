package dev.yezi.anniversary.app

import android.app.Application
import dev.yezi.anniversary.core.database.AppDatabase
import dev.yezi.anniversary.feature.anniversary.data.EventRepository
import dev.yezi.anniversary.feature.anniversary.presentation.EventViewModel
import dev.yezi.anniversary.feature.period.presentation.PeriodViewModel
import dev.yezi.anniversary.feature.todo.presentation.TodoViewModel

class LifeSpaceApp : Application() {
    val database by lazy { AppDatabase.get(this) }
    val anniversaryRepository by lazy {
        EventRepository(
            database.eventDao(),
            database.reminderDao(),
            applicationContext
        )
    }
    val anniversaryViewModelFactory by lazy { EventViewModel.factory(anniversaryRepository) }
    val todoViewModelFactory by lazy { TodoViewModel.factory() }
    val periodViewModelFactory by lazy { PeriodViewModel.factory() }
}
