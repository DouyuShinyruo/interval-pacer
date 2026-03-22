package com.github.intervalpacer.presentation.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.github.intervalpacer.presentation.ui.theme.IntervalPacerTheme
import com.github.intervalpacer.presentation.interval.IntervalConfigUi
import com.github.intervalpacer.presentation.interval.IntervalScreen
import com.github.intervalpacer.presentation.interval.IntervalViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: IntervalViewModel by viewModels()

    // 通知权限请求（Android 13+）
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* 权限结果处理 */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 请求通知权限
        requestNotificationPermission()

        setContent {
            IntervalPacerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel)
                }
            }
        }
    }

    /**
     * 请求通知权限（Android 13+）
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: IntervalViewModel) {
    val timerState by viewModel.timerState.collectAsState()
    val currentPhase by viewModel.currentPhase.collectAsState()
    var config by remember { mutableStateOf(viewModel.config) }

    IntervalScreen(
        timerState = timerState,
        currentPhase = currentPhase,
        config = config,
        onConfigChange = { newConfig ->
            config = newConfig
            viewModel.updateConfig(newConfig)
        },
        onStart = { viewModel.startWorkout() },
        onPause = { viewModel.pauseWorkout() },
        onResume = { viewModel.resumeWorkout() },
        onStop = { viewModel.stopWorkout() },
        onSkip = { viewModel.skipPhase() },
        onResetToIdle = { viewModel.resetToIdle() }
    )
}
