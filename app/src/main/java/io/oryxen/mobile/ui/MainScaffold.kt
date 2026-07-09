package io.oryxen.mobile.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Yard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.oryxen.mobile.ui.analytics.AnalyticsScreen
import io.oryxen.mobile.ui.dashboard.DashboardScreen
import io.oryxen.mobile.ui.plants.PlantsListScreen
import io.oryxen.mobile.ui.settings.SettingsScreen
import io.oryxen.mobile.ui.community.CommunityScreen
import io.oryxen.mobile.ui.theme.OryxenGreen

data class BottomTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val tabs = listOf(
    BottomTab("tab_dashboard", "Dashboard", Icons.Filled.Home, Icons.Outlined.Home),
    BottomTab("tab_plants", "Plants", Icons.Filled.Yard, Icons.Outlined.Yard),
    BottomTab("tab_history", "History", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    BottomTab("tab_community", "Community", Icons.Filled.Group, Icons.Outlined.Group),
    BottomTab("tab_settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
)

@Composable
fun MainScaffold(
    onNavigatePlantDetail: (String) -> Unit,
    onNavigatePlantCreate: () -> Unit,
    onNavigatePlantEdit: (String) -> Unit,
    onNavigateScanner: () -> Unit,
    onNavigateDiagnosis: () -> Unit,
    onNavigatePlans: () -> Unit,
    onNavigateNotifications: () -> Unit,
    onNavigateChatbot: () -> Unit,
    onLogout: () -> Unit,
) {
    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                tabs.forEach { tab ->
                    val selected = navBackStackEntry?.destination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            tabNavController.navigate(tab.route) {
                                popUpTo(tabNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label,
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OryxenGreen,
                            selectedTextColor = OryxenGreen,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == "tab_dashboard") {
                FloatingActionButton(
                    onClick = onNavigateScanner,
                    containerColor = OryxenGreen,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                ) {
                    Icon(Icons.Filled.Yard, contentDescription = "Link device")
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = "tab_dashboard",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("tab_dashboard") {
                DashboardScreen(
                    onNavigateNotifications = onNavigateNotifications,
                    onNavigatePlantDetail = onNavigatePlantDetail,
                    onNavigateChatbot = onNavigateChatbot
                )
            }
            composable("tab_plants") {
                PlantsListScreen(
                    onPlantClick = onNavigatePlantDetail,
                    onAddPlant = onNavigatePlantCreate,
                    onEditPlant = onNavigatePlantEdit,
                )
            }
            composable("tab_history") {
                AnalyticsScreen()
            }
            composable("tab_community") {
                CommunityScreen()
            }
            composable("tab_settings") {
                SettingsScreen(
                    onNavigatePlans = onNavigatePlans,
                    onLogout = onLogout,
                )
            }
        }
    }
}
