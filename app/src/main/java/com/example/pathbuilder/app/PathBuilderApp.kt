package com.example.pathbuilder.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pathbuilder.ui.components.BottomNavBar
import com.example.pathbuilder.ui.create.CreateRouteScreen
import com.example.pathbuilder.ui.map.MapScreen
import com.example.pathbuilder.ui.routes.RoutesScreen
import com.example.pathbuilder.ui.settings.SettingsScreen
import com.example.pathbuilder.ui.stats.StatsScreen
import com.example.pathbuilder.viewmodel.CreateRouteViewModel
import com.example.pathbuilder.viewmodel.MapViewModel
import com.example.pathbuilder.viewmodel.RouteListViewModel
import com.example.pathbuilder.viewmodel.StatsViewModel

@Composable
fun PathBuilderApp(appContainer: AppContainer) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppScreen.MAP.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppScreen.MAP.name) {
                val mapViewModel: MapViewModel = viewModel(
                    factory = MapViewModel.provideFactory(appContainer)
                )
                MapScreen(
                    viewModel = mapViewModel,
                    onCreateRoute = { navController.navigate(AppScreen.CREATE.name) },
                    onOpenRoutes = { navController.navigate(AppScreen.ROUTES.name) }
                )
            }
            composable(AppScreen.ROUTES.name) {
                val routeListViewModel: RouteListViewModel = viewModel(
                    factory = RouteListViewModel.provideFactory(appContainer)
                )
                RoutesScreen(
                    viewModel = routeListViewModel,
                    onCreateNew = { navController.navigate(AppScreen.CREATE.name) }
                )
            }
            composable(AppScreen.CREATE.name) {
                val createRouteViewModel: CreateRouteViewModel = viewModel(
                    factory = CreateRouteViewModel.provideFactory(appContainer, appContainer.orsApiKey)
                )
                CreateRouteScreen(
                    viewModel = createRouteViewModel
                )
            }
            composable(AppScreen.STATS.name) {
                val statsViewModel: StatsViewModel = viewModel(
                    factory = StatsViewModel.provideFactory(appContainer)
                )
                StatsScreen(viewModel = statsViewModel)
            }
            composable(AppScreen.SETTINGS.name) {
                SettingsScreen()
            }
        }
    }
}

