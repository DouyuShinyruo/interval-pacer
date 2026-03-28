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
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.github.intervalpacer.presentation.navigation.AppNavigation
import com.github.intervalpacer.presentation.history.HistoryViewModel
import com.github.intervalpacer.presentation.interval.IntervalViewModel
import com.github.intervalpacer.presentation.settraining.SetTrainingViewModel
import com.github.intervalpacer.presentation.ui.theme.IntervalPacerTheme

/**
 * 主 Activity
 * 应用的入口点，管理导航和权限请求
 */
class MainActivity : ComponentActivity() {

    private val intervalViewModel: IntervalViewModel by viewModels()
    private val setTrainingViewModel: SetTrainingViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()

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
                    AppNavigation(intervalViewModel, setTrainingViewModel, historyViewModel)
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
