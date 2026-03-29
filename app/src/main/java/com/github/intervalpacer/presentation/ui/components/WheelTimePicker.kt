package com.github.intervalpacer.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlinx.coroutines.launch

// ========== 时间预设常量 ==========

/** 间歇训练时间预设（跑步/步行时长） */
val intervalTimePresets = listOf(
    0 to 30,   // 0:30
    0 to 45,   // 0:45
    1 to 0,    // 1:00
    1 to 30,   // 1:30
    2 to 0,    // 2:00
    5 to 0,    // 5:00
)

/** 组间休息时间预设 */
val restTimePresets = listOf(
    0 to 30,   // 0:30
    0 to 45,   // 0:45
    1 to 0,    // 1:00
    1 to 30,   // 1:30
    2 to 0,    // 2:00
    3 to 0,    // 3:00
)

/**
 * 格式化预设时间为统一的 "m:ss" 格式
 */
fun formatPresetTime(minutes: Int, seconds: Int): String =
    "${minutes}:${String.format("%02d", seconds)}"

// ========== 配置对话框中的时间选择行 ==========

/**
 * 时间选择行 —— 显示标签 + 当前值 + 右侧箭头，点击弹出滚轮选择器
 */
@Composable
fun TimeSelectorRow(
    label: String,
    currentMinutes: Int,
    currentSeconds: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatPresetTime(currentMinutes, currentSeconds),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ========== 底部弹窗双列循环滚轮选择器 ==========

private val WheelItemHeight = 48.dp
private const val VisibleItems = 5
private const val PaddingItemCount = 2
private const val TotalRepetitions = 1000

/** 分钟可选值：0 ~ 30 */
private val minuteValues = (0..30).toList()

/** 秒可选值：0 ~ 59 */
private val secondValues = (0..59).toList()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelTimePickerSheet(
    title: String,
    initialMinutes: Int,
    initialSeconds: Int,
    presets: List<Pair<Int, Int>> = intervalTimePresets,
    onDismiss: () -> Unit,
    onConfirm: (minutes: Int, seconds: Int) -> Unit
) {
    var selectedMinutes by remember { mutableIntStateOf(initialMinutes) }
    var selectedSeconds by remember { mutableIntStateOf(initialSeconds) }

    // 初始滚动位置：在重复列表的中间，确保双向无限循环
    val initialMinIdx = minuteValues.indexOf(initialMinutes).coerceAtLeast(0)
    val initialSecIdx = secondValues.indexOf(initialSeconds).coerceAtLeast(0)
    val minStartPos = (TotalRepetitions / 2) * minuteValues.size + initialMinIdx
    val secStartPos = (TotalRepetitions / 2) * secondValues.size + initialSecIdx

    val minutesState = rememberLazyListState(initialFirstVisibleItemIndex = minStartPos)
    val secondsState = rememberLazyListState(initialFirstVisibleItemIndex = secStartPos)
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val containerColor = MaterialTheme.colorScheme.surfaceContainerLow

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = containerColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // 顶部栏：取消 | 标题 | 完成
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { onConfirm(selectedMinutes, selectedSeconds) }) {
                    Text("完成", color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(8.dp))

            // ===== 分钟列追踪 =====
            LaunchedEffect(minutesState.isScrollInProgress) {
                if (!minutesState.isScrollInProgress) {
                    val offset = minutesState.firstVisibleItemScrollOffset
                    val rawIndex = minutesState.firstVisibleItemIndex
                    val centerIndex = if (offset > 0) rawIndex + 1 else rawIndex
                    val valueIndex = ((centerIndex % minuteValues.size) + minuteValues.size) % minuteValues.size
                    selectedMinutes = minuteValues[valueIndex]
                    if (rawIndex != centerIndex || offset != 0) {
                        minutesState.animateScrollToItem(centerIndex)
                    }
                }
            }

            // ===== 秒列追踪 =====
            LaunchedEffect(secondsState.isScrollInProgress) {
                if (!secondsState.isScrollInProgress) {
                    val offset = secondsState.firstVisibleItemScrollOffset
                    val rawIndex = secondsState.firstVisibleItemIndex
                    val centerIndex = if (offset > 0) rawIndex + 1 else rawIndex
                    val valueIndex = ((centerIndex % secondValues.size) + secondValues.size) % secondValues.size
                    selectedSeconds = secondValues[valueIndex]
                    if (rawIndex != centerIndex || offset != 0) {
                        secondsState.animateScrollToItem(centerIndex)
                    }
                }
            }

            // ===== 双列循环滚轮（分 : 秒）=====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(WheelItemHeight * VisibleItems)
            ) {
                // 居中高亮条
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .height(WheelItemHeight)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                )

                // 滚轮内容
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // 分钟循环滚轮
                    LazyColumn(
                        state = minutesState,
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(PaddingItemCount) {
                            Box(Modifier.height(WheelItemHeight))
                        }
                        items(TotalRepetitions * minuteValues.size) { index ->
                            val value = minuteValues[index % minuteValues.size]
                            val distance = abs(index - minutesState.firstVisibleItemIndex)
                            WheelItem(
                                text = "$value",
                                isSelected = value == selectedMinutes,
                                alpha = when {
                                    distance == 0 -> 1f
                                    distance == 1 -> 0.5f
                                    else -> 0.15f
                                }
                            )
                        }
                    }

                    // 冒号分隔符
                    Text(
                        text = ":",
                        modifier = Modifier
                            .width(20.dp)
                            .align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 秒循环滚轮
                    LazyColumn(
                        state = secondsState,
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(PaddingItemCount) {
                            Box(Modifier.height(WheelItemHeight))
                        }
                        items(TotalRepetitions * secondValues.size) { index ->
                            val value = secondValues[index % secondValues.size]
                            val distance = abs(index - secondsState.firstVisibleItemIndex)
                            WheelItem(
                                text = String.format("%02d", value),
                                isSelected = value == selectedSeconds,
                                alpha = when {
                                    distance == 0 -> 1f
                                    distance == 1 -> 0.5f
                                    else -> 0.15f
                                }
                            )
                        }
                    }
                }

                // 顶部渐变遮罩 —— 让滚轮边缘自然过渡
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(containerColor, containerColor.copy(alpha = 0f))
                            )
                        )
                )

                // 底部渐变遮罩
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(containerColor.copy(alpha = 0f), containerColor)
                            )
                        )
                )
            }

            Spacer(Modifier.height(16.dp))

            // ===== 底部快捷预设（2x3 网格）=====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 第一行：3 个预设
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.take(3).forEach { (min, sec) ->
                        val isSelected = selectedMinutes == min && selectedSeconds == sec
                        PresetChip(
                            label = formatPresetTime(min, sec),
                            isSelected = isSelected,
                            onClick = {
                                selectedMinutes = min
                                selectedSeconds = sec
                                coroutineScope.launch {
                                    minutesState.animateScrollToItem(
                                        nearestIndexForValue(minutesState.firstVisibleItemIndex, minuteValues, min)
                                    )
                                    secondsState.animateScrollToItem(
                                        nearestIndexForValue(secondsState.firstVisibleItemIndex, secondValues, sec)
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                // 第二行：3 个预设
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.drop(3).take(3).forEach { (min, sec) ->
                        val isSelected = selectedMinutes == min && selectedSeconds == sec
                        PresetChip(
                            label = formatPresetTime(min, sec),
                            isSelected = isSelected,
                            onClick = {
                                selectedMinutes = min
                                selectedSeconds = sec
                                coroutineScope.launch {
                                    minutesState.animateScrollToItem(
                                        nearestIndexForValue(minutesState.firstVisibleItemIndex, minuteValues, min)
                                    )
                                    secondsState.animateScrollToItem(
                                        nearestIndexForValue(secondsState.firstVisibleItemIndex, secondValues, sec)
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * 在循环列表中找到离当前位置最近的、显示目标值的滚轮位置
 */
private fun nearestIndexForValue(currentFirstVisible: Int, values: List<Int>, targetValue: Int): Int {
    val cycleLen = values.size
    val targetIdx = values.indexOf(targetValue).coerceAtLeast(0)
    val base = currentFirstVisible - ((currentFirstVisible % cycleLen) + cycleLen) % cycleLen + targetIdx
    val candidates = listOf(base - cycleLen, base, base + cycleLen)
    return candidates.minByOrNull { abs(it - currentFirstVisible) }!!
}

// ========== 滚轮单项 ==========

@Composable
private fun WheelItem(
    text: String,
    isSelected: Boolean,
    alpha: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(WheelItemHeight),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.alpha(alpha)
        )
    }
}

// ========== 预设 Chip ==========

@Composable
private fun PresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.height(42.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
