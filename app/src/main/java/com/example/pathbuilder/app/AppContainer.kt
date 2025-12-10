package com.example.pathbuilder.app

import android.content.Context
import com.example.pathbuilder.BuildConfig
import com.example.pathbuilder.data.LocalRouteRepository
import com.example.pathbuilder.data.RouteRepository
import com.example.pathbuilder.database.RouteDatabase

class AppContainer(context: Context) {
    private val database: RouteDatabase by lazy {
        RouteDatabase.getDatabase(context)
    }

    val routeRepository: RouteRepository by lazy {
        LocalRouteRepository(database.routeDao())
    }
    
    val orsApiKey: String = BuildConfig.ORS_API_KEY
}

