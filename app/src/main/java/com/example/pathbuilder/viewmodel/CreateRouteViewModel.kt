package com.example.pathbuilder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pathbuilder.app.AppContainer
import com.example.pathbuilder.data.RouteRepository
import com.example.pathbuilder.database.RouteEntity
import com.example.pathbuilder.model.Waypoint
import com.example.pathbuilder.network.ORSDirectionsRequest
import com.example.pathbuilder.network.OpenRouteServiceApi
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateRouteUiState(
    val routeName: String = "",
    val routeType: String? = null, // null = not selected yet
    val waypoints: List<Waypoint> = emptyList(),
    val routeGeometry: List<Waypoint> = emptyList(), // Detailed route path from ORS
    val distanceMiles: Double = 0.0,
    val elevationFeet: Double = 0.0,
    val estimatedTimeMinutes: Int = 0,
    val manualMode: Boolean = false,
    val saveCompleted: Boolean = false,
    val isCalculating: Boolean = false,
    val showRouteTypeDialog: Boolean = true, // Show at start
    val layer: MapLayer = MapLayer.STANDARD,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
)

class CreateRouteViewModel(
    private val repository: RouteRepository,
    private val orsApiKey: String = ""
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateRouteUiState())
    val uiState: StateFlow<CreateRouteUiState> = _uiState.asStateFlow()
    
    private val orsApi = OpenRouteServiceApi.create()
    
    private val undoStack = mutableListOf<RouteSnapshot>()
    private val redoStack = mutableListOf<RouteSnapshot>()
    
    data class RouteSnapshot(
        val waypoints: List<Waypoint>,
        val routeGeometry: List<Waypoint>,
        val distanceMiles: Double,
        val elevationFeet: Double,
        val estimatedTimeMinutes: Int
    )

    fun setRouteType(type: String) {
        _uiState.value = _uiState.value.copy(
            routeType = type,
            showRouteTypeDialog = false
        )
    }

    fun toggleLayer() {
        val next = when (_uiState.value.layer) {
            MapLayer.STANDARD -> MapLayer.SATELLITE
            MapLayer.SATELLITE -> MapLayer.STANDARD
        }
        _uiState.value = _uiState.value.copy(layer = next)
    }

    fun startNewRoute() {
        // Clear undo/redo history
        undoStack.clear()
        redoStack.clear()
        
        // Reset to initial state with route type modal
        _uiState.value = CreateRouteUiState(
            layer = _uiState.value.layer,
            showRouteTypeDialog = true,
            canUndo = false,
            canRedo = false
        )
    }

    fun toggleManualMode() {
        val newManualMode = !_uiState.value.manualMode
        _uiState.value = _uiState.value.copy(manualMode = newManualMode)
        
        // Recalculate with the new mode
        if (_uiState.value.waypoints.size >= 2) {
            if (newManualMode) {
                recalculateManual()
            } else {
                calculateRouteWithORS(_uiState.value.waypoints)
            }
        }
    }

    fun updateRouteName(name: String) {
        _uiState.value = _uiState.value.copy(routeName = name)
    }

    fun updateRouteType(type: String) {
        _uiState.value = _uiState.value.copy(routeType = type)
        // Recalculate route with new type
        if (_uiState.value.waypoints.size >= 2) {
            if (_uiState.value.manualMode) {
                recalculateManual()
            } else {
                calculateRouteWithORS(_uiState.value.waypoints)
            }
        }
    }

    fun addPin(point: Waypoint) {
        // Save current state to undo stack before making changes
        val currentState = _uiState.value
        if (currentState.waypoints.isNotEmpty()) {
            undoStack.add(
                RouteSnapshot(
                    waypoints = currentState.waypoints,
                    routeGeometry = currentState.routeGeometry,
                    distanceMiles = currentState.distanceMiles,
                    elevationFeet = currentState.elevationFeet,
                    estimatedTimeMinutes = currentState.estimatedTimeMinutes
                )
            )
            // Clear redo stack when new action is taken
            redoStack.clear()
        }
        
        val updated = _uiState.value.waypoints + point
        _uiState.value = _uiState.value.copy(
            waypoints = updated,
            canUndo = undoStack.isNotEmpty(),
            canRedo = false
        )
        
        if (_uiState.value.manualMode || updated.size < 2) {
            // Manual mode or first pin - just calculate straight line
            recalculateManual(updated)
        } else {
            // Use OpenRouteService to calculate route
            calculateRouteWithORS(updated)
        }
    }
    
    fun undo() {
        if (undoStack.isEmpty()) return
        
        // Save current state to redo stack
        val currentState = _uiState.value
        redoStack.add(
            RouteSnapshot(
                waypoints = currentState.waypoints,
                routeGeometry = currentState.routeGeometry,
                distanceMiles = currentState.distanceMiles,
                elevationFeet = currentState.elevationFeet,
                estimatedTimeMinutes = currentState.estimatedTimeMinutes
            )
        )
        
        // Pop from undo stack
        val previousState = undoStack.removeLast()
        
        _uiState.value = _uiState.value.copy(
            waypoints = previousState.waypoints,
            routeGeometry = previousState.routeGeometry,
            distanceMiles = previousState.distanceMiles,
            elevationFeet = previousState.elevationFeet,
            estimatedTimeMinutes = previousState.estimatedTimeMinutes,
            canUndo = undoStack.isNotEmpty(),
            canRedo = redoStack.isNotEmpty(),
            saveCompleted = false
        )
    }
    
    fun redo() {
        if (redoStack.isEmpty()) return
        
        // Save current state to undo stack
        val currentState = _uiState.value
        undoStack.add(
            RouteSnapshot(
                waypoints = currentState.waypoints,
                routeGeometry = currentState.routeGeometry,
                distanceMiles = currentState.distanceMiles,
                elevationFeet = currentState.elevationFeet,
                estimatedTimeMinutes = currentState.estimatedTimeMinutes
            )
        )
        
        // Pop from redo stack
        val nextState = redoStack.removeLast()
        
        _uiState.value = _uiState.value.copy(
            waypoints = nextState.waypoints,
            routeGeometry = nextState.routeGeometry,
            distanceMiles = nextState.distanceMiles,
            elevationFeet = nextState.elevationFeet,
            estimatedTimeMinutes = nextState.estimatedTimeMinutes,
            canUndo = undoStack.isNotEmpty(),
            canRedo = redoStack.isNotEmpty(),
            saveCompleted = false
        )
    }

    private fun recalculateManual(waypoints: List<Waypoint> = _uiState.value.waypoints) {
        val distance = computeDistanceMiles(waypoints)
        val elevation = computeElevationGain(waypoints)
        val estimatedTime = estimateTravelMinutes(distance, _uiState.value.routeType ?: "walk")
        _uiState.value = _uiState.value.copy(
            distanceMiles = distance,
            elevationFeet = elevation,
            estimatedTimeMinutes = estimatedTime,
            routeGeometry = waypoints, // In manual mode, geometry is same as waypoints
            saveCompleted = false
        )
    }

    private fun calculateRouteWithORS(waypoints: List<Waypoint>) {
        if (orsApiKey.isBlank()) {
            recalculateManual(waypoints)
            return
        }

        val previousGeometry = _uiState.value.routeGeometry
        val previousDistance = _uiState.value.distanceMiles
        val previousElevation = _uiState.value.elevationFeet

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isCalculating = true)
                
                val profile = when (_uiState.value.routeType) {
                    "drive" -> "driving-car"
                    else -> "foot-walking"
                }
                
                val coordinates = waypoints.map { listOf(it.longitude, it.latitude) }
                val request = ORSDirectionsRequest(coordinates = coordinates, elevation = true)
                
                val response = orsApi.getDirections(profile, orsApiKey, request)
                
                if (response.error != null || response.features == null) {
                    fallbackToManualSegment(waypoints, previousGeometry, previousDistance, previousElevation)
                    return@launch
                }
                
                if (response.features.isNotEmpty()) {
                    val feature = response.features.first()
                    val geometry = feature.geometry
                    val properties = feature.properties
                    val summary = properties?.summary
                    val coordinates = geometry?.coordinates
                    
                    if (geometry == null || properties == null || summary == null || coordinates == null) {
                        fallbackToManualSegment(waypoints, previousGeometry, previousDistance, previousElevation)
                        return@launch
                    }
                    
                    val geometryWaypoints = coordinates.map { coord ->
                        Waypoint(
                            latitude = coord[1],
                            longitude = coord[0],
                            elevationFeet = if (coord.size > 2) coord[2] * 3.28084 else 0.0
                        )
                    }
                    
                    val elevationGain = computeElevationGain(geometryWaypoints)
                    val distanceMiles = (summary.distance ?: 0.0) / 1609.34
                    val estimatedTime = estimateTravelMinutes(distanceMiles, _uiState.value.routeType ?: "walk")
                    
                    _uiState.value = _uiState.value.copy(
                        routeGeometry = geometryWaypoints,
                        distanceMiles = distanceMiles,
                        elevationFeet = elevationGain,
                        estimatedTimeMinutes = estimatedTime,
                        isCalculating = false,
                        saveCompleted = false
                    )
                } else {
                    fallbackToManualSegment(waypoints, previousGeometry, previousDistance, previousElevation)
                }
            } catch (e: Exception) {
                fallbackToManualSegment(waypoints, previousGeometry, previousDistance, previousElevation)
            }
        }
    }
    
    private fun fallbackToManualSegment(
        waypoints: List<Waypoint>,
        previousGeometry: List<Waypoint>,
        previousDistance: Double,
        previousElevation: Double
    ) {
        if (waypoints.size < 2 || previousGeometry.isEmpty()) {
            recalculateManual(waypoints)
            return
        }
        
        val lastPreviousPoint = waypoints[waypoints.size - 2]
        val newPoint = waypoints.last()
        
        val newSegmentDistance = haversine(
            lastPreviousPoint.latitude,
            lastPreviousPoint.longitude,
            newPoint.latitude,
            newPoint.longitude
        ) / 1609.34
        
        val newSegmentElevation = if (newPoint.elevationFeet > lastPreviousPoint.elevationFeet) {
            newPoint.elevationFeet - lastPreviousPoint.elevationFeet
        } else 0.0
        
        val updatedGeometry = previousGeometry + newPoint
        val totalDistance = previousDistance + newSegmentDistance
        val totalElevation = previousElevation + newSegmentElevation
        val estimatedTime = estimateTravelMinutes(totalDistance, _uiState.value.routeType ?: "walk")
        
        _uiState.value = _uiState.value.copy(
            routeGeometry = updatedGeometry,
            distanceMiles = totalDistance,
            elevationFeet = totalElevation,
            estimatedTimeMinutes = estimatedTime,
            isCalculating = false,
            saveCompleted = false
        )
    }

    private fun computeDistanceMiles(points: List<Waypoint>): Double {
        if (points.size < 2) return 0.0
        var totalMeters = 0.0
        points.windowed(2).forEach { pair ->
            val first = pair.first()
            val second = pair.last()
            totalMeters += haversine(
                first.latitude,
                first.longitude,
                second.latitude,
                second.longitude
            )
        }
        return totalMeters / 1609.34
    }

    private fun computeElevationGain(points: List<Waypoint>): Double {
        if (points.size < 2) return 0.0
        var gain = 0.0
        points.windowed(2).forEach { pair ->
            val diff = pair.last().elevationFeet - pair.first().elevationFeet
            if (diff > 0) gain += diff
        }
        return gain
    }

    private fun estimateTravelMinutes(distanceMiles: Double, type: String): Int {
        val speedMph = if (type == "drive") 30.0 else 3.0
        return if (distanceMiles == 0.0) 0 else ((distanceMiles / speedMph) * 60).toInt()
    }

    private fun haversine(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val r = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun saveRoute() {
        val state = _uiState.value
        if (state.routeName.isBlank() || state.waypoints.isEmpty()) return
        
        val difficultyScore = state.distanceMiles + (state.elevationFeet / 100.0)
        val difficulty = when {
            difficultyScore > 8 -> "Hard"
            difficultyScore > 4 -> "Moderate"
            else -> "Easy"
        }
        val entity = RouteEntity(
            id = 0,
            name = state.routeName,
            distanceMiles = state.distanceMiles,
            elevationFeet = state.elevationFeet,
            difficulty = difficulty,
            type = state.routeType ?: "walk",
            estimatedTimeMinutes = state.estimatedTimeMinutes,
            waypoints = state.waypoints,
            routeGeometry = state.routeGeometry
        )
        viewModelScope.launch {
            repository.insertRoute(entity)
            _uiState.value = CreateRouteUiState(
                routeType = state.routeType,
                showRouteTypeDialog = false,
                saveCompleted = true
            )
        }
    }

    companion object {
        fun provideFactory(repository: RouteRepository, orsApiKey: String = ""): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { CreateRouteViewModel(repository, orsApiKey) }
            }

        fun provideFactory(container: AppContainer, orsApiKey: String = ""): ViewModelProvider.Factory =
            provideFactory(container.routeRepository, orsApiKey)
    }
}

