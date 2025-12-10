package com.example.pathbuilder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pathbuilder.app.AppContainer
import com.example.pathbuilder.data.RouteRepository
import com.example.pathbuilder.database.toModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class StatsUiState(
    val totalWalkMiles: Double = 0.0,
    val totalDriveMiles: Double = 0.0,
    val averageElevationGain: Double = 0.0
)

class StatsViewModel(
    private val repository: RouteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getRoutes().collectLatest { entities ->
                val routes = entities.map { it.toModel() }
                val elevations = routes.map { it.elevationFeet }
                val averageElevation =
                    if (elevations.isEmpty()) 0.0 else elevations.sum() / elevations.size
                _uiState.value = StatsUiState(
                    totalWalkMiles = routes.filter { it.type == "walk" }
                        .sumOf { it.distanceMiles },
                    totalDriveMiles = routes.filter { it.type == "drive" }
                        .sumOf { it.distanceMiles },
                    averageElevationGain = averageElevation
                )
            }
        }
    }

    companion object {
        fun provideFactory(repository: RouteRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { StatsViewModel(repository) }
            }

        fun provideFactory(container: AppContainer): ViewModelProvider.Factory =
            provideFactory(container.routeRepository)
    }
}

