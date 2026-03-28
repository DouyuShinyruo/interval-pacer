package com.github.intervalpacer.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.github.intervalpacer.presentation.home.HomeScreen
import com.github.intervalpacer.presentation.history.HistoryDetailScreen
import com.github.intervalpacer.presentation.history.HistoryScreen
import com.github.intervalpacer.presentation.history.HistoryViewModel
import com.github.intervalpacer.presentation.interval.IntervalConfigUi
import com.github.intervalpacer.presentation.interval.IntervalScreen
import com.github.intervalpacer.presentation.interval.IntervalViewModel
import com.github.intervalpacer.presentation.settraining.SetTrainingScreen
import com.github.intervalpacer.presentation.settraining.SetTrainingViewModel
import com.github.intervalpacer.presentation.settings.SettingsScreen
import com.github.intervalpacer.R

/**
 * 导航路由定义
 */
sealed class Screen(val route: String, val title: String, val iconId: Int) {
    data object Home : Screen("home", "首页", R.drawable.ic_home)
    data object History : Screen("history", "历史", R.drawable.ic_history)
    data object Settings : Screen("settings", "设置", R.drawable.ic_settings)

    data object Interval : Screen("interval", "间歇训练", 0)
    data object SetTraining : Screen("set_training", "力量训练", 0)

    data object HistoryDetail : Screen("history_detail/{recordId}", "训练详情", 0) {
        fun createRoute(recordId: String) = "history_detail/$recordId"
    }
}

/**
 * 全屏页面路由（不显示底部导航栏）
 */
private val fullScreenRoutes = setOf(Screen.Interval.route, Screen.SetTraining.route, Screen.HistoryDetail.route)

/**
 * 应用主导航结构
 * 包含底部 Tab 导航和页面路由
 *
 * @param intervalViewModel 间歇训练的 ViewModel（从 MainActivity 传入）
 * @param setTrainingViewModel 力量训练的 ViewModel（从 MainActivity 传入）
 */
@Composable
fun AppNavigation(
    intervalViewModel: IntervalViewModel,
    setTrainingViewModel: SetTrainingViewModel,
    historyViewModel: HistoryViewModel
) {
    val navController = rememberNavController()
    val bottomNavScreens = listOf(Screen.Home, Screen.History, Screen.Settings)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route !in fullScreenRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavScreens.forEach { screen ->
                        val isSelected = currentDestination?.hierarchy?.any {
                            it.route == screen.route
                        } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = screen.iconId),
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = isSelected,
                            onClick = {
                                if (currentDestination?.route == screen.route) return@NavigationBarItem
                                if (!navController.popBackStack(screen.route, inclusive = false)) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            // 首页
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToInterval = {
                        navController.navigate(Screen.Interval.route)
                    },
                    onNavigateToSetTraining = {
                        navController.navigate(Screen.SetTraining.route)
                    },
                    onNavigateToHistory = {
                        navController.navigate(Screen.History.route)
                    }
                )
            }

            // 历史记录
            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = historyViewModel,
                    onNavigateToDetail = { recordId ->
                        navController.navigate(Screen.HistoryDetail.createRoute(recordId))
                    }
                )
            }

            // 设置
            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            // 间歇训练页面（全屏，无底部导航）
            composable(Screen.Interval.route) {
                val timerState by intervalViewModel.timerState.collectAsState()
                val currentPhase by intervalViewModel.currentPhase.collectAsState()
                var config by remember { mutableStateOf(intervalViewModel.config) }

                IntervalScreen(
                    timerState = timerState,
                    currentPhase = currentPhase,
                    config = config,
                    onConfigChange = { newConfig ->
                        config = newConfig
                        intervalViewModel.updateConfig(newConfig)
                    },
                    onStart = { intervalViewModel.startWorkout() },
                    onPause = { intervalViewModel.pauseWorkout() },
                    onResume = { intervalViewModel.resumeWorkout() },
                    onStop = {
                        intervalViewModel.stopWorkout()
                    },
                    onSkip = { intervalViewModel.skipPhase() },
                    onResetToIdle = {
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                        intervalViewModel.resetToIdle()
                    }
                )
            }

            // 力量训练页面（全屏，无底部导航）
            composable(Screen.SetTraining.route) {
                val trainingState by setTrainingViewModel.trainingState.collectAsState()
                var config by remember { mutableStateOf(setTrainingViewModel.config) }

                SetTrainingScreen(
                    trainingState = trainingState,
                    config = config,
                    onConfigChange = { newConfig ->
                        config = newConfig
                        setTrainingViewModel.updateConfig(newConfig)
                    },
                    onStart = { setTrainingViewModel.startTraining() },
                    onPause = { setTrainingViewModel.pauseTraining() },
                    onResume = { setTrainingViewModel.resumeTraining() },
                    onStop = {
                        setTrainingViewModel.stopTraining()
                    },
                    onCompleteSet = { setTrainingViewModel.completeSet() },
                    onSkipRest = { setTrainingViewModel.skipRest() },
                    onResetToIdle = {
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                        setTrainingViewModel.resetToIdle()
                    }
                )
            }

            // 训练详情页（全屏，无底部导航）
            composable(
                route = Screen.HistoryDetail.route,
                arguments = listOf(
                    navArgument("recordId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val recordId = backStackEntry.arguments?.getString("recordId") ?: ""
                HistoryDetailScreen(
                    recordId = recordId,
                    viewModel = historyViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
