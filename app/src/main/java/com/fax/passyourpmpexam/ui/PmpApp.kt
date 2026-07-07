package com.fax.passyourpmpexam.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.fax.passyourpmpexam.feature.daily.DailyScreen
import com.fax.passyourpmpexam.feature.home.HomeScreen
import com.fax.passyourpmpexam.feature.free.FreeScreen
import com.fax.passyourpmpexam.feature.settings.SettingsScreen
import com.fax.passyourpmpexam.feature.quiz.QuizScreen
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

// The Home tab is a nested graph so its sub-screens (Quiz/Daily/Free) keep the Home tab
// selected and route Home taps back to the dashboard instead of round-tripping.
@Serializable
object HomeBaseRoute

@Serializable
object HomeRoute

@Serializable
object PracticeRoute

@Serializable
object SettingsRoute

// Destinations reachable from the Home hub (not top-level tabs).
@Serializable
object DailyRoute

@Serializable
object QuizRoute

private data class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    val label: String,
    val icon: ImageVector,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(HomeBaseRoute, HomeBaseRoute::class, "Home", Icons.Filled.Home),
    TopLevelDestination(PracticeRoute, PracticeRoute::class, "Practice", Icons.Filled.PlayArrow),
    TopLevelDestination(SettingsRoute, SettingsRoute::class, "Settings", Icons.Filled.Settings),
)

@Composable
fun PmpApp() {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination
                topLevelDestinations.forEach { destination ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.hasRoute(destination.routeClass)
                    } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeBaseRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            navigation<HomeBaseRoute>(startDestination = HomeRoute) {
                composable<HomeRoute> {
                    HomeScreen(
                        onStartDaily = { navController.navigate(DailyRoute) },
                        onStartQuiz = { navController.navigate(QuizRoute) },
                        // Free Mode lives on the Practice tab; switch to it like a bottom-nav tap
                        // so tab selection and saved state stay consistent.
                        onStartFree = {
                            navController.navigate(PracticeRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
                composable<DailyRoute> { DailyScreen(onBack = { navController.popBackStack() }) }
                composable<QuizRoute> { QuizScreen(onBack = { navController.popBackStack() }) }
            }
            composable<PracticeRoute> { FreeScreen() }
            composable<SettingsRoute> { SettingsScreen() }
        }
    }
}
