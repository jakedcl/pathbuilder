package com.example.pathbuilder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pathbuilder.app.AppContainer
import com.example.pathbuilder.data.RouteRepository
import com.example.pathbuilder.database.toModel
import com.example.pathbuilder.model.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class RouteListUiState(
    val routes: List<Route> = emptyList(),
    val totalWalkMiles: Double = 0.0,
    val totalDriveMiles: Double = 0.0
)

class RouteListViewModel(
    private val repository: RouteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouteListUiState())
    val uiState: StateFlow<RouteListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getRoutes().collectLatest { entities ->
                val routes = entities.map { it.toModel() }
                _uiState.value = RouteListUiState(
                    routes = routes,
                    totalWalkMiles = routes.filter { it.type == "walk" }
                        .sumOf { it.distanceMiles },
                    totalDriveMiles = routes.filter { it.type == "drive" }
                        .sumOf { it.distanceMiles }
                )
            }
        }
    }

    companion object {
        fun provideFactory(repository: RouteRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { RouteListViewModel(repository) }
            }

        fun provideFactory(container: AppContainer): ViewModelProvider.Factory =
            provideFactory(container.routeRepository)
    }
}

