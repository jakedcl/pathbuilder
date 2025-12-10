package com.example.pathbuilder.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.pathbuilder.app.AppScreen

data class BottomNavItem(
    val screen: AppScreen,
    val label: String,
    val icon: @Composable () -> Unit
)

private val navItems = listOf(
    BottomNavItem(AppScreen.MAP, "Map", { Icon(Icons.Default.Map, contentDescription = "Map") }),
    BottomNavItem(AppScreen.ROUTES, "Routes", { Icon(Icons.Default.List, contentDescription = "Routes") }),
    BottomNavItem(AppScreen.CREATE, "Create", { Icon(Icons.Default.Build, contentDescription = "Create") }),
    BottomNavItem(AppScreen.STATS, "Stats", { Icon(Icons.Default.Timeline, contentDescription = "Stats") }),
    BottomNavItem(AppScreen.SETTINGS, "Settings", { Icon(Icons.Default.Settings, contentDescription = "Settings") })
)

@Composable
fun BottomNavBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        navItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.screen.name,
                onClick = {
                    if (currentRoute != item.screen.name) {
                        navController.navigate(item.screen.name) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { item.icon() },
                label = { Text(item.label) }
            )
        }
    }
}

