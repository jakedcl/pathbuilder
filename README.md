# PathBuilder - Android Route Planning App
**CSC 438 Mobile Development Project**

An Android app for creating and managing hiking/driving routes with smart route calculation and elevation tracking.

---

## 📱 Quick Overview

**What it does**: Users place pins on a map to create routes. The app automatically calculates the best path between pins using real roads/trails, tracks elevation gain, and saves routes to a local database.

**Tech Stack**: Kotlin, Jetpack Compose, Room Database, Retrofit, OSMDroid, OpenRouteService API

**Architecture**: MVVM (Model-View-ViewModel)

---

## 🎯 Core Features

### 1. Interactive Map
- Pan, zoom, and view saved routes
- Three map layers: Standard (street), Satellite (aerial), Topo (topographic)
- GPS location tracking (blue dot shows your position)
- Click routes to see details

### 2. Smart Route Creation
- **Smart Mode**: Uses OpenRouteService API to follow actual roads/trails between pins
- **Manual Mode**: Draws straight lines between pins
- Choose route type: Walking or Driving (affects routing algorithm)
- Real-time stats: distance, elevation gain, estimated time
- Undo/Redo for pin placement

### 3. Filtering & Search
- Filter by: Type (walk/drive), Difficulty (easy/moderate/hard), Elevation range
- Search routes by name
- Filters work together (AND logic)

### 4. Statistics Dashboard
- Total routes, total miles, total elevation gain, average distance
- Calculated from all saved routes

### 5. Local Storage
- All routes saved to Room database (SQLite)
- Works offline (except route creation needs API)

---

## 🏗️ Architecture (MVVM)

```
┌─────────────────┐
│   UI (Compose)  │  ← User sees and interacts
│   Screens       │
└────────┬────────┘
         │ observes StateFlow
         │ calls functions
┌────────▼────────┐
│   ViewModel     │  ← Business logic + state management
│   State + Logic │
└────────┬────────┘
         │ calls repository
┌────────▼────────┐
│   Repository    │  ← Data abstraction layer
│   Interface     │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
┌───▼──┐  ┌──▼───┐
│ Room │  │ API  │  ← Data sources
│  DB  │  │ Call │
└──────┘  └──────┘
```

**Why MVVM?**
- Separates UI from logic (easier to test and modify)
- ViewModels survive screen rotations
- UI automatically updates when data changes (reactive)

---

## 🛠️ Tech Stack Breakdown

### Kotlin
**What**: Programming language for Android
**Why**: Modern, concise, null-safe, official Android language

### Jetpack Compose
**What**: UI framework (declarative)
**Why**: Less code than XML, easier to maintain, live preview
**How**: Write functions that describe UI, Compose handles rendering

### Room Database
**What**: SQLite wrapper for local storage
**Why**: Type-safe queries, compile-time checks, reactive (Flow)
**What's stored**: Routes with name, distance, elevation, waypoints, geometry

### Retrofit
**What**: HTTP client for API calls
**Why**: Simplifies REST API calls, converts JSON to Kotlin objects
**Used for**: OpenRouteService API calls

### OSMDroid
**What**: OpenStreetMap library for Android
**Why**: Free (no API key needed), supports offline tiles
**Features**: Map display, zoom/pan, overlays (routes, pins)

### OpenRouteService API
**What**: Routing API (like Google Directions but free)
**Why**: Free tier (2000 requests/day), returns elevation data
**Profiles**: `foot-walking` for hiking, `driving-car` for driving

### Kotlin Coroutines
**What**: Async programming (like threads but better)
**Why**: Non-blocking API calls, automatic lifecycle management
**Used for**: API calls, database operations

### StateFlow
**What**: Observable state holder
**Why**: UI automatically updates when state changes
**Pattern**: ViewModel holds StateFlow, UI collects it

---

## 📂 Code Structure

```
app/src/main/java/com/example/pathbuilder/

├── app/
│   ├── AppContainer.kt          # Dependency injection (provides ViewModels)
│   ├── AppScreen.kt              # Navigation routes enum
│   └── PathBuilderApp.kt         # Main app with NavHost + Bottom Nav

├── data/
│   ├── RouteRepository.kt        # Interface for data access
│   └── LocalRouteRepository.kt   # Implementation using Room

├── database/
│   ├── RouteDao.kt               # SQL queries (@Insert, @Query, @Delete)
│   ├── RouteDatabase.kt          # Database singleton
│   ├── RouteEntity.kt            # Table schema (@Entity)
│   └── RouteTypeConverters.kt    # JSON converters for List<Waypoint>

├── model/
│   ├── Route.kt                  # Domain model (used in app)
│   └── Waypoint.kt               # Lat/lng/elevation data class

├── network/
│   └── OpenRouteServiceApi.kt    # Retrofit API interface + data models

├── ui/
│   ├── components/               # Reusable UI pieces
│   │   ├── BottomNavBar.kt       # Bottom nav (5 tabs)
│   │   ├── FilterDialog.kt       # Filter popup
│   │   ├── MapboxMapView.kt      # OSMDroid map wrapper (draws routes/pins)
│   │   └── RouteCard.kt          # List item for routes
│   │
│   ├── create/CreateRouteScreen.kt   # Route builder UI
│   ├── map/MapScreen.kt              # Main map UI
│   ├── routes/RoutesScreen.kt        # Routes list UI
│   ├── stats/StatsScreen.kt          # Statistics UI
│   └── settings/SettingsScreen.kt    # Settings UI (placeholder)

└── viewmodel/
    ├── CreateRouteViewModel.kt   # Route creation logic (410 lines)
    ├── MapViewModel.kt           # Map + filtering logic (167 lines)
    ├── RouteListViewModel.kt     # Routes list logic
    └── StatsViewModel.kt         # Statistics calculations
```

---

## 🔑 Key Implementation Details

### How Smart Routing Works

**User Action**: Taps 3 pins on map

**What Happens**:
1. User taps pin 1 → Stored in `waypoints` list
2. User taps pin 2 → Now 2 points, call OpenRouteService API
   ```kotlin
   // Convert to API format
   coordinates = [[lng1, lat1], [lng2, lat2]]
   
   // API call
   response = orsApi.getDirections(
       profile = "foot-walking",
       apiKey = orsApiKey,
       request = { coordinates, elevation: true }
   )
   
   // Response: GeoJSON with ~50 points following roads
   geometry = response.features[0].geometry.coordinates
   // [[lng, lat, elevation], [lng, lat, elevation], ...]
   ```
3. Extract geometry and calculate stats:
   ```kotlin
   distance = response.summary.distance / 1609.34  // meters to miles
   elevation = sumOf positive elevation changes
   time = distance / speed (3 mph for walking)
   ```
4. Draw black line on map using geometry points
5. User taps pin 3 → Repeat with all 3 points

**If API Fails**: Keep previous smart route, add straight line to new point

---

### Difficulty Calculation Algorithm

```kotlin
// Formula: Difficulty = Distance + (Elevation / 100)
// Every 100 ft elevation = 1 mile of difficulty

val score = distanceMiles + (elevationFeet / 100.0)

val difficulty = when {
    score > 8 -> "Hard"      // >8 miles equivalent
    score > 4 -> "Moderate"  // 4-8 miles equivalent
    else -> "Easy"           // <4 miles equivalent
}
```

**Examples**:
- 3 mi flat = 3.0 score = Easy
- 3 mi + 500 ft = 8.0 score = Moderate  
- 5 mi + 1000 ft = 15.0 score = Hard

---

### State Management Pattern

**Every screen has**:
```kotlin
// In ViewModel
private val _uiState = MutableStateFlow(ScreenUiState())
val uiState: StateFlow<ScreenUiState> = _uiState.asStateFlow()

// In UI
val uiState by viewModel.uiState.collectAsState()
Text("Distance: ${uiState.distance}") // Auto-updates!
```

**Flow**:
1. User taps button → UI calls ViewModel function
2. ViewModel updates `_uiState.value`
3. StateFlow emits new value
4. UI's `collectAsState()` receives update
5. Composable re-runs with new data
6. Screen updates

---

### Database Schema

**Table: `routes`**
```sql
CREATE TABLE routes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    distanceMiles REAL,
    elevationFeet REAL,
    difficulty TEXT,
    type TEXT,
    estimatedTimeMinutes INTEGER,
    waypoints TEXT,      -- JSON: [{"lat":37.9,"lng":-122.5,"elevation":800}]
    routeGeometry TEXT   -- JSON: [...100 points following roads...]
);
```

**Why two coordinate fields?**
- `waypoints`: User-placed pins (2-10 points) - for editing
- `routeGeometry`: Detailed path (50-200 points) - for drawing accurate lines

**Type Converter** (for complex types):
```kotlin
@TypeConverter
fun fromWaypointList(list: List<Waypoint>): String = 
    Gson().toJson(list)

@TypeConverter
fun toWaypointList(json: String): List<Waypoint> = 
    Gson().fromJson(json, List::class.java)
```

---

### Undo/Redo Implementation

**Data structures**:
```kotlin
undoStack: MutableList<RouteSnapshot>  // Previous states
redoStack: MutableList<RouteSnapshot>  // Undone states

data class RouteSnapshot(
    waypoints: List<Waypoint>,
    routeGeometry: List<Waypoint>,
    distanceMiles: Double,
    elevationFeet: Double,
    estimatedTimeMinutes: Int
)
```

**Logic**:
- Before adding pin: Save current state to `undoStack`, clear `redoStack`
- Undo: Move current to `redoStack`, pop from `undoStack`, restore
- Redo: Move current to `undoStack`, pop from `redoStack`, restore

---

### Navigation System

**Bottom Nav Tabs**:
```kotlin
enum class AppScreen { MAP, ROUTES, CREATE, STATS, SETTINGS }

NavHost(navController, startDestination = MAP) {
    composable(MAP.name) { MapScreen(...) }
    composable(ROUTES.name) { RoutesScreen(...) }
    composable(CREATE.name) { CreateRouteScreen(...) }
    composable(STATS.name) { StatsScreen(...) }
    composable(SETTINGS.name) { SettingsScreen(...) }
}
```

**Navigation**: `navController.navigate(AppScreen.CREATE.name)`

---

## 📊 Key Algorithms

### Haversine Formula (GPS distance)
```kotlin
fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0  // Earth radius in meters
    val dLat = toRadians(lat2 - lat1)
    val dLon = toRadians(lon2 - lon1)
    
    val a = sin(dLat/2)^2 + cos(lat1) * cos(lat2) * sin(dLon/2)^2
    val c = 2 * atan2(√a, √(1-a))
    return R * c
}
```

### Elevation Gain (only count uphill)
```kotlin
fun computeElevationGain(points: List<Waypoint>): Double {
    var gain = 0.0
    points.windowed(2).forEach { (p1, p2) ->
        val diff = p2.elevationFeet - p1.elevationFeet
        if (diff > 0) gain += diff  // Ignore descents
    }
    return gain
}
```

### Filtering Logic
```kotlin
routes.filter { route ->
    val matchesSearch = searchQuery.isBlank() || 
                       route.name.contains(searchQuery, ignoreCase = true)
    val matchesType = selectedTypes.isEmpty() || 
                     selectedTypes.contains(route.type)
    val matchesDifficulty = selectedDifficulties.isEmpty() || 
                           selectedDifficulties.contains(route.difficulty)
    val matchesElevation = /* elevation range check */
    
    matchesSearch && matchesType && matchesDifficulty && matchesElevation
}
```

---

## 🌐 API Integration

### OpenRouteService Request
```http
POST https://api.openrouteservice.org/v2/directions/foot-walking/geojson
Authorization: YOUR_API_KEY
Content-Type: application/json

{
  "coordinates": [
    [-122.4194, 37.7749],
    [-122.4100, 37.7800]
  ],
  "elevation": true
}
```

### Response (GeoJSON)
```json
{
  "type": "FeatureCollection",
  "features": [{
    "geometry": {
      "type": "LineString",
      "coordinates": [
        [-122.4194, 37.7749, 10.5],
        [-122.4195, 37.7750, 11.2],
        ...
      ]
    },
    "properties": {
      "summary": {
        "distance": 1609.34,
        "duration": 1200
      },
      "ascent": 50.5,
      "descent": 20.2
    }
  }]
}
```

### Retrofit Interface
```kotlin
interface OpenRouteServiceApi {
    @POST("v2/directions/{profile}/geojson")
    suspend fun getDirections(
        @Path("profile") profile: String,
        @Header("Authorization") apiKey: String,
        @Body request: ORSDirectionsRequest
    ): ORSFeatureCollection
}
```

---

## 🎨 UI Components (Compose)

### Example: Route Card
```kotlin
@Composable
fun RouteCard(route: Route, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(route.name, style = MaterialTheme.typography.titleLarge)
            Row {
                Icon(Icons.Default.Route, "Distance")
                Text("${route.distanceMiles} mi")
                Icon(Icons.Default.Terrain, "Elevation")
                Text("${route.elevationFeet} ft")
            }
            Badge(text = route.difficulty, color = difficultyColor(route))
        }
    }
}
```

**Key Compose Concepts**:
- `@Composable`: Function that describes UI
- `remember`: Survives recomposition
- `LazyColumn`: Efficient scrolling list (only renders visible items)
- `collectAsState()`: Observes Flow, triggers recomposition

---

## 📦 Dependencies (build.gradle.kts)

```kotlin
dependencies {
    // Compose UI
    implementation("androidx.compose.ui:ui:1.7.5")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.3")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.4")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // OSMDroid Maps
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    
    // Location Services
    implementation("com.google.android.gms:play-services-location:21.3.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

---

## ⚙️ Setup Instructions

### For Your Partner/Team

**1. Clone repository**
```bash
git clone https://github.com/jakedcl/pathbuilder.git
cd pathbuilder
```

**2. ⚠️ IMPORTANT: Add API key (Required!)**

The app **will NOT build** without this step!

```bash
# Copy the template
cp gradle.properties.template gradle.properties

# Edit gradle.properties
# Replace YOUR_API_KEY_HERE with your actual API key
```

**Get a free API key** (takes 2 minutes):
1. Go to https://openrouteservice.org/dev/#/signup
2. Sign up with email
3. Confirm email
4. Copy your API key from dashboard
5. Paste it in `gradle.properties` where it says `YOUR_API_KEY_HERE`

**Why?** `gradle.properties` contains your personal API key and is gitignored for security. Each developer needs their own key.

**3. Open in Android Studio**
- File → Open → Select `pathbuilder` folder
- Wait for Gradle sync (2-5 minutes)
- If sync fails, check that `gradle.properties` has a real API key!
- Click Run (Shift+F10)

**4. Grant Location Permission**
- When app opens, allow location access
- This lets the map center on your location

**5. Requirements**:
- Android Studio (Hedgehog or newer)
- Android SDK API 34
- Emulator (Pixel 4+, API 34) or physical device

---

## 📝 Project Stats

- **Total Lines**: ~3,477 lines of Kotlin
- **Screens**: 5 (Map, Routes, Create, Stats, Settings)
- **ViewModels**: 4 (Map, CreateRoute, RouteList, Stats)
- **Database Tables**: 1 (routes)
- **API Endpoints**: 1 (OpenRouteService directions)
- **Largest File**: CreateRouteViewModel.kt (410 lines)

---

## 🎓 Presentation Talking Points

### Architecture Benefits
- **MVVM**: Separates UI from logic, easier to test
- **Repository Pattern**: Abstract data sources (could add cloud sync later)
- **Dependency Injection**: AppContainer provides ViewModels

### Technical Highlights
- **Reactive UI**: StateFlow makes UI update automatically
- **Async Programming**: Coroutines for non-blocking API calls
- **Type Safety**: Room checks SQL at compile time
- **Modern UI**: Compose is declarative (describe what, not how)

### Challenges Solved
- **API Failures**: Graceful fallback to manual mode
- **Complex State**: Undo/redo with snapshot pattern
- **Data Persistence**: Type converters for complex JSON structures
- **Map Integration**: OSMDroid for free, offline-capable maps

### Design Decisions
- **OSMDroid over Google Maps**: Free, no credit card required
- **Room over raw SQLite**: Less boilerplate, type-safe
- **Compose over XML**: Modern, less code, easier maintenance
- **OpenRouteService**: Free tier sufficient, includes elevation

---

## 🔒 API Key Security

**Problem**: Can't commit API keys to public repos

**Solution**:
1. Store key in `gradle.properties` (gitignored)
2. Provide `gradle.properties.template` (committed)
3. Inject key at build time via `BuildConfig.ORS_API_KEY`
4. Each user gets their own free key

---

**Repository**: https://github.com/jakedcl/pathbuilder

**Built for CSC 438 Mobile Development**
