package com.github.intervalpacer.presentation.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.github.intervalpacer.data.local.DarkModeOption
import com.github.intervalpacer.data.local.SharedPreferencesManager
import com.github.intervalpacer.presentation.navigation.AppNavigation
import com.github.intervalpacer.presentation.history.HistoryViewModel
import com.github.intervalpacer.presentation.interval.IntervalViewModel
import com.github.intervalpacer.presentation.settings.SettingsViewModel
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
    private val settingsViewModel: SettingsViewModel by viewModels()

    // 通知权限请求（Android 13+）
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* 权限结果处理 */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 请求通知权限
        requestNotificationPermission()

        setContent {
            val settingsState by settingsViewModel.settingsState.collectAsState()

            // 深色模式
            val darkTheme = when (settingsState.darkMode) {
                DarkModeOption.ALWAYS_ON -> true
                DarkModeOption.ALWAYS_OFF -> false
                DarkModeOption.FOLLOW_SYSTEM -> isSystemInDarkTheme()
            }

            // 保持屏幕常亮
            LaunchedEffect(settingsState.keepScreenOn) {
                if (settingsState.keepScreenOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            IntervalPacerTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        intervalViewModel = intervalViewModel,
                        setTrainingViewModel = setTrainingViewModel,
                        historyViewModel = historyViewModel,
                        settingsViewModel = settingsViewModel,
                        prefsManager = SharedPreferencesManager(this)
                    )
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
