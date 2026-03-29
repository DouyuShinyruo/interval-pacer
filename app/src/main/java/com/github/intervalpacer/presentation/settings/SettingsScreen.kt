package com.github.intervalpacer.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Slider
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.intervalpacer.data.local.DarkModeOption
import com.github.intervalpacer.data.local.PhasePromptStyle
import com.github.intervalpacer.data.local.TimeReminderOption

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.settingsState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .navigationBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 语音设置
        SettingsCategoryHeader("语音设置")
        SettingsSwitchItem(
            title = "语音播报",
            subtitle = "训练过程中播报阶段和倒计时",
            checked = state.voiceEnabled,
            onCheckedChange = { viewModel.setVoiceEnabled(it) }
        )
        if (state.voiceEnabled) {
            SettingsSwitchItem(
                title = "倒计时语音",
                subtitle = "最后 10 秒逐秒播报",
                checked = state.countdownVoice,
                onCheckedChange = { viewModel.setCountdownVoice(it) }
            )
            SettingsRadioItem(
                title = "时间提醒",
                options = listOf(
                    TimeReminderOption.NONE to "关闭",
                    TimeReminderOption.THIRTY_SECONDS to "剩余 30 秒",
                    TimeReminderOption.ONE_MINUTE to "剩余 1 分钟"
                ),
                selectedOption = state.timeReminder,
                onSelect = { viewModel.setTimeReminder(it) }
            )
            SettingsRadioItem(
                title = "阶段提示风格",
                options = listOf(
                    PhasePromptStyle.SIMPLE to "简洁",
                    PhasePromptStyle.DETAILED to "详细",
                    PhasePromptStyle.OFF to "关闭"
                ),
                selectedOption = state.phasePromptStyle,
                onSelect = { viewModel.setPhasePromptStyle(it) }
            )
            SettingsSliderItem(
                title = "语速",
                value = state.speechRate,
                onValueChange = { viewModel.setSpeechRate(it) }
            )
            SettingsButtonItem(
                title = "测试语音",
                onClick = { viewModel.testVoice() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 显示设置
        SettingsCategoryHeader("显示设置")
        SettingsSwitchItem(
            title = "保持屏幕常亮",
            subtitle = "训练中禁止屏幕自动熄灭",
            checked = state.keepScreenOn,
            onCheckedChange = { viewModel.setKeepScreenOn(it) }
        )
        SettingsRadioItem(
            title = "深色模式",
            options = listOf(
                DarkModeOption.FOLLOW_SYSTEM to "跟随系统",
                DarkModeOption.ALWAYS_ON to "始终开启",
                DarkModeOption.ALWAYS_OFF to "始终关闭"
            ),
            selectedOption = state.darkMode,
            onSelect = { viewModel.setDarkMode(it) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 交互设置
        SettingsCategoryHeader("交互设置")
        SettingsSwitchItem(
            title = "震动反馈",
            subtitle = "阶段切换和倒计时震动",
            checked = state.vibrationEnabled,
            onCheckedChange = { viewModel.setVibrationEnabled(it) }
        )
        SettingsSwitchItem(
            title = "锁屏通知",
            subtitle = "训练中在锁屏显示状态",
            checked = state.lockScreenNotification,
            onCheckedChange = { viewModel.setLockScreenNotification(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 恢复默认
        OutlinedButton(
            onClick = { viewModel.resetToDefault() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("恢复默认设置")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ========== 通用设置组件 ==========

@Composable
private fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String = "",
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun <T> SettingsRadioItem(
    title: String,
    options: List<Pair<T, String>>,
    selectedOption: T,
    onSelect: (T) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, (value, label) ->
                SegmentedButton(
                    selected = (value == selectedOption),
                    onClick = { onSelect(value) },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size)
                ) {
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun SettingsSliderItem(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "%.1fx".format(value),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = 0.5f..2.0f,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SettingsButtonItem(
    title: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(title)
    }
    Spacer(modifier = Modifier.height(4.dp))
}
