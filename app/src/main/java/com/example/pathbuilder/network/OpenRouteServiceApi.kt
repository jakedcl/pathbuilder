package com.example.pathbuilder.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

// OpenRouteService API Models
data class ORSDirectionsRequest(
    val coordinates: List<List<Double>>, // [[lon, lat], [lon, lat], ...]
    val elevation: Boolean = true,
    val instructions: Boolean = false
)

// GeoJSON Response Format (for /geojson endpoint)
data class ORSDirectionsResponse(
    val type: String? = null, // "FeatureCollection"
    val features: List<ORSFeature>? = null,
    val bbox: List<Double>? = null,
    val metadata: ORSMetadata? = null,
    val error: ORSError? = null
)

data class ORSFeature(
    val type: String? = null, // "Feature"
    val properties: ORSProperties? = null,
    val geometry: ORSGeometry? = null,
    val bbox: List<Double>? = null
)

data class ORSProperties(
    val summary: ORSSummary? = null,
    val way_points: List<Int>? = null,
    val ascent: Double? = null,
    val descent: Double? = null
)

data class ORSSummary(
    val distance: Double? = null, // meters
    val duration: Double? = null  // seconds
)

data class ORSGeometry(
    val type: String? = null, // "LineString"
    val coordinates: List<List<Double>>? = null // [[lon, lat, elevation], ...]
)

data class ORSMetadata(
    val attribution: String? = null,
    val service: String? = null,
    val timestamp: Long? = null
)

data class ORSError(
    val code: Int? = null,
    val message: String? = null
)

// Retrofit API Interface
interface OpenRouteServiceApi {
    @POST("v2/directions/{profile}/geojson")
    suspend fun getDirections(
        @Path("profile") profile: String, // "driving-car" or "foot-walking"
        @Header("Authorization") apiKey: String,
        @Body request: ORSDirectionsRequest
    ): ORSDirectionsResponse

    companion object {
        private const val BASE_URL = "https://api.openrouteservice.org/"

        fun create(): OpenRouteServiceApi {
            val loggingInterceptor = okhttp3.logging.HttpLoggingInterceptor().apply {
                level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
            }
            
            val client = okhttp3.OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()
            
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(OpenRouteServiceApi::class.java)
        }
    }
}

