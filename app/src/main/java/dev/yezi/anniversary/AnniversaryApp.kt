package dev.yezi.anniversary

import android.app.Application
import dev.yezi.anniversary.data.AppDatabase
import dev.yezi.anniversary.data.EventRepository

class AnniversaryApp : Application() {
    val repository by lazy { EventRepository(AppDatabase.get(this).eventDao(), applicationContext) }
}
