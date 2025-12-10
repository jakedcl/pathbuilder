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

enum class MapLayer {
    STANDARD,
    SATELLITE
}

data class MapUiState(
    val routes: List<Route> = emptyList(),
    val visibleRoutes: List<Route> = emptyList(),
    val searchQuery: String = "",
    val selectedTypes: Set<String> = setOf("walk", "drive"),
    val selectedDifficulties: Set<String> = setOf("Easy", "Moderate", "Hard"),
    val selectedElevationRanges: Set<String> = setOf("Flat", "Low", "Medium", "High"),
    val layer: MapLayer = MapLayer.STANDARD,
    val filtersExpanded: Boolean = false
)

class MapViewModel(
    private val repository: RouteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getRoutes().collectLatest { entities ->
                val routes = entities.map { it.toModel() }
                _uiState.value = _uiState.value.copy(
                    routes = routes,
                    visibleRoutes = filterRoutes(
                        routes = routes,
                        search = _uiState.value.searchQuery,
                        types = _uiState.value.selectedTypes,
                        difficulties = _uiState.value.selectedDifficulties,
                        elevationRanges = _uiState.value.selectedElevationRanges
                    )
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            visibleRoutes = filterRoutes(
                routes = _uiState.value.routes,
                search = query,
                types = _uiState.value.selectedTypes,
                difficulties = _uiState.value.selectedDifficulties,
                elevationRanges = _uiState.value.selectedElevationRanges
            )
        )
    }

    fun toggleType(type: String) {
        val updated = _uiState.value.selectedTypes.toMutableSet().apply {
            if (contains(type)) remove(type) else add(type)
        }
        _uiState.value = _uiState.value.copy(
            selectedTypes = updated,
            visibleRoutes = filterRoutes(
                routes = _uiState.value.routes,
                search = _uiState.value.searchQuery,
                types = updated,
                difficulties = _uiState.value.selectedDifficulties,
                elevationRanges = _uiState.value.selectedElevationRanges
            )
        )
    }

    fun toggleDifficulty(difficulty: String) {
        val updated = _uiState.value.selectedDifficulties.toMutableSet().apply {
            if (contains(difficulty)) remove(difficulty) else add(difficulty)
        }
        _uiState.value = _uiState.value.copy(
            selectedDifficulties = updated,
            visibleRoutes = filterRoutes(
                routes = _uiState.value.routes,
                search = _uiState.value.searchQuery,
                types = _uiState.value.selectedTypes,
                difficulties = updated,
                elevationRanges = _uiState.value.selectedElevationRanges
            )
        )
    }

    fun toggleElevationRange(range: String) {
        val updated = _uiState.value.selectedElevationRanges.toMutableSet().apply {
            if (contains(range)) remove(range) else add(range)
        }
        _uiState.value = _uiState.value.copy(
            selectedElevationRanges = updated,
            visibleRoutes = filterRoutes(
                routes = _uiState.value.routes,
                search = _uiState.value.searchQuery,
                types = _uiState.value.selectedTypes,
                difficulties = _uiState.value.selectedDifficulties,
                elevationRanges = updated
            )
        )
    }

    fun toggleLayer() {
        val next = when (_uiState.value.layer) {
            MapLayer.STANDARD -> MapLayer.SATELLITE
            MapLayer.SATELLITE -> MapLayer.STANDARD
        }
        _uiState.value = _uiState.value.copy(layer = next)
    }

    fun toggleFiltersExpanded() {
        _uiState.value = _uiState.value.copy(filtersExpanded = !_uiState.value.filtersExpanded)
    }

    private fun filterRoutes(
        routes: List<Route>,
        search: String,
        types: Set<String>,
        difficulties: Set<String>,
        elevationRanges: Set<String>
    ): List<Route> =
        routes.filter { route ->
            val matchesSearch = search.isBlank() || route.name.contains(search, ignoreCase = true)
            val matchesType = types.isEmpty() || types.contains(route.type.lowercase())
            val matchesDifficulty = difficulties.isEmpty() || difficulties.contains(route.difficulty)
            
            // Elevation range filtering
            val elevationRange = when {
                route.elevationFeet < 100 -> "Flat"         // < 100 ft
                route.elevationFeet < 500 -> "Low"          // 100-500 ft
                route.elevationFeet < 1500 -> "Medium"      // 500-1500 ft
                else -> "High"                               // > 1500 ft
            }
            val matchesElevation = elevationRanges.isEmpty() || elevationRanges.contains(elevationRange)
            
            matchesSearch && matchesType && matchesDifficulty && matchesElevation
        }

    companion object {
        fun provideFactory(repository: RouteRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { MapViewModel(repository) }
            }

        fun provideFactory(container: AppContainer): ViewModelProvider.Factory =
            provideFactory(container.routeRepository)
    }
}

