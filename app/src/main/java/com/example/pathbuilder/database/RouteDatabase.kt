package com.example.pathbuilder.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [RouteEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(RouteTypeConverters::class)
abstract class RouteDatabase : RoomDatabase() {

    abstract fun routeDao(): RouteDao

    companion object {
        @Volatile
        private var Instance: RouteDatabase? = null

        fun getDatabase(context: Context): RouteDatabase =
            Instance ?: synchronized(this) {
                Instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RouteDatabase::class.java,
                    "route_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
    }
}

