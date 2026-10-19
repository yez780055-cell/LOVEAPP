package dev.yezi.anniversary.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        val app = application as LifeSpaceApp
        setContent {
            AppShell(
                todoViewModel = viewModel(factory = app.todoViewModelFactory),
                periodViewModel = viewModel(factory = app.periodViewModelFactory),
                anniversaryViewModel = viewModel(factory = app.anniversaryViewModelFactory),
                initialEventId = intent.getStringExtra("open_event_id")
            )
        }
    }
}
