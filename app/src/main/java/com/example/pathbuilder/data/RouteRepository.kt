package com.example.pathbuilder.data

import com.example.pathbuilder.database.RouteEntity
import kotlinx.coroutines.flow.Flow

interface RouteRepository {
    fun getRoutes(): Flow<List<RouteEntity>>
    suspend fun insertRoute(route: RouteEntity)
    suspend fun deleteRoute(route: RouteEntity)
    suspend fun clearRoutes()
}

