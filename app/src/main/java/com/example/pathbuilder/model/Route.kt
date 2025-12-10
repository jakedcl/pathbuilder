package com.example.pathbuilder.model

data class Route(
    val id: Long = 0,
    val name: String,
    val distanceMiles: Double,
    val elevationFeet: Double,
    val difficulty: String = "Moderate",
    val type: String = "walk",
    val estimatedTimeMinutes: Int = 0,
    val waypoints: List<Waypoint> = emptyList(),
    val routeGeometry: List<Waypoint> = emptyList() // Detailed route path from OpenRouteService
)

