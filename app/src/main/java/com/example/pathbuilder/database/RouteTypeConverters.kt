package com.example.pathbuilder.database

import androidx.room.TypeConverter
import com.example.pathbuilder.model.Waypoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RouteTypeConverters {
    private val gson = Gson()
    private val waypointType = object : TypeToken<List<Waypoint>>() {}.type

    @TypeConverter
    fun fromWaypointList(list: List<Waypoint>?): String = gson.toJson(list ?: emptyList<Waypoint>(), waypointType)

    @TypeConverter
    fun toWaypointList(value: String?): List<Waypoint> =
        value?.let { gson.fromJson(it, waypointType) } ?: emptyList()
}

