package com.example.pathbuilder.data

import com.example.pathbuilder.database.RouteDao
import com.example.pathbuilder.database.RouteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LocalRouteRepository(
    private val routeDao: RouteDao
) : RouteRepository {

    override fun getRoutes(): Flow<List<RouteEntity>> = routeDao.getAllRoutes()

    override suspend fun insertRoute(route: RouteEntity) = withContext(Dispatchers.IO) {
        routeDao.insertRoute(route)
    }

    override suspend fun deleteRoute(route: RouteEntity) = withContext(Dispatchers.IO) {
        routeDao.deleteRoute(route)
    }

    override suspend fun clearRoutes() = withContext(Dispatchers.IO) {
        routeDao.clearAllRoutes()
    }
}

