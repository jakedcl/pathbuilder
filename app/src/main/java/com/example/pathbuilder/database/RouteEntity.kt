package com.example.pathbuilder.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pathbuilder.model.Route
import com.example.pathbuilder.model.Waypoint

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val distanceMiles: Double,
    val elevationFeet: Double,
    val difficulty: String,
    val type: String,
    val estimatedTimeMinutes: Int,
    val waypoints: List<Waypoint> = emptyList(),
    val routeGeometry: List<Waypoint> = emptyList()
)

fun RouteEntity.toModel(): Route =
    Route(
        id = id,
        name = name,
        distanceMiles = distanceMiles,
        elevationFeet = elevationFeet,
        difficulty = difficulty,
        type = type,
        estimatedTimeMinutes = estimatedTimeMinutes,
        waypoints = waypoints,
        routeGeometry = routeGeometry
    )

fun Route.toEntity(): RouteEntity =
    RouteEntity(
        id = if (id == 0L) 0 else id,
        name = name,
        distanceMiles = distanceMiles,
        elevationFeet = elevationFeet,
        difficulty = difficulty,
        type = type,
        estimatedTimeMinutes = estimatedTimeMinutes,
        waypoints = waypoints,
        routeGeometry = routeGeometry
    )

