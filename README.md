# PathBuilder - Android Route Planning App

**A comprehensive route planning application for Android that allows users to create, save, and manage hiking and driving routes with intelligent route calculation and real-time elevation tracking.**

---

## 📱 Table of Contents

1. [Project Overview](#project-overview)
2. [App Features - Complete Breakdown](#app-features---complete-breakdown)
3. [Technical Architecture](#technical-architecture)
4. [Technologies & Libraries Used](#technologies--libraries-used)
5. [App Screens - Detailed](#app-screens---detailed)
6. [How Each Feature Works](#how-each-feature-works)
7. [Database Design](#database-design)
8. [API Integration](#api-integration)
9. [State Management](#state-management)
10. [Navigation System](#navigation-system)
11. [Setup Instructions](#setup-instructions)
12. [Code Organization](#code-organization)

---

## 🎯 Project Overview

**PathBuilder** is an Android mobile application built for CSC 438 Mobile Development. It demonstrates modern Android development practices using Kotlin and Jetpack Compose.

### What Does This App Do?

Users can:
- **View saved routes** on an interactive map with different layer options (Standard/Satellite/Topo)
- **Create custom routes** by placing pins on a map, which automatically calculates the best path between points
- **Track statistics** like total distance traveled, elevation gained, and number of routes created
- **Filter routes** by type (walking vs driving), difficulty level, and elevation gain
- **Save routes** to a local database for offline access

### Why Is This Useful?

- **Hikers** can plan trails and see elevation profiles
- **Drivers** can create custom driving routes
- **Outdoor enthusiasts** can track their adventures and statistics
- **Route planning** with automatic road/trail snapping makes planning realistic

---

## 📋 App Features - Complete Breakdown

### 1. **Interactive Map Display**
   - **What it does**: Shows an interactive map where you can pan, zoom, and view saved routes
   - **Technologies**: OSMDroid (OpenStreetMap library), Android Location Services
   - **Features**:
     - Real-time GPS location (blue dot shows your position)
     - Multiple map layers: Standard (street map), Satellite (aerial imagery), Topo (topographic)
     - Pinch-to-zoom and pan gestures
     - Permanent zoom controls (+/- buttons)
     - Displays all saved routes as blue lines
     - Click on routes to see details popup

### 2. **Smart Route Creation**
   - **What it does**: Create routes by tapping pins on the map, automatically connecting them with real roads/trails
   - **Technologies**: OpenRouteService API, Retrofit (for API calls), Kotlin Coroutines (for async operations)
   - **Features**:
     - **Smart Mode** (default): Uses OpenRouteService API to find actual roads/trails between pins
     - **Manual Mode**: Draws straight lines between pins (useful for off-trail routes)
     - **Route Types**: Choose between walking or driving routes (affects routing algorithm)
     - **Real-time calculations**: Distance, elevation gain, and estimated time update as you add pins
     - **Visual feedback**: Black lines show the route path, colored pins show waypoints (green for last pin, red for others)

### 3. **Undo/Redo Functionality**
   - **What it does**: Lets you undo/redo pin placements while building routes
   - **Technology**: Custom history stack implementation in ViewModel
   - **How it works**: Saves snapshots of route state (waypoints, geometry, distance, elevation) before each change

### 4. **Route Saving & Persistence**
   - **What it does**: Saves routes to a local database so they persist between app sessions
   - **Technologies**: Room Database (SQLite), Type Converters (for complex data)
   - **Data stored**: Route name, distance, elevation, difficulty, type, waypoints, full route geometry

### 5. **Advanced Filtering System**
   - **What it does**: Filter visible routes based on multiple criteria
   - **Filter options**:
     - **Type**: Walk or Drive
     - **Difficulty**: Easy, Moderate, Hard (calculated from distance + elevation)
     - **Elevation Range**: Flat (<100ft), Low (100-500ft), Medium (500-1500ft), High (>1500ft)
   - **Technology**: Kotlin collection filtering in ViewModel

### 6. **Search Functionality**
   - **What it does**: Search routes by name (case-insensitive)
   - **Technology**: Kotlin string matching with `contains()` function

### 7. **Statistics Dashboard**
   - **What it does**: Shows aggregate data about all your routes
   - **Metrics displayed**:
     - Total routes created
     - Total miles traveled across all routes
     - Total elevation gain (feet)
     - Average route distance
   - **Technology**: Kotlin collection operations (sum, average) on database data

### 8. **Route List View**
   - **What it does**: Displays all saved routes in a scrollable list with details
   - **Shows**: Route name, distance, elevation, estimated time, difficulty badge, type icon
   - **Technology**: LazyColumn (Compose's lazy list), Material3 Cards

### 9. **Difficulty Calculation Algorithm**
   - **What it does**: Automatically calculates route difficulty
   - **Formula**: `Difficulty Score = Distance (miles) + Elevation (feet) / 100`
   - **Logic**: Every 100 feet of elevation gain equals 1 mile of flat walking
   - **Thresholds**:
     - Easy: Score < 4
     - Moderate: Score 4-8
     - Hard: Score > 8
   - **Example**: 3 miles + 500ft elevation = score of 8.0 = Moderate

### 10. **Elevation Tracking**
   - **What it does**: Calculates total elevation gain for routes
   - **How it works**: 
     - Smart routes: Uses elevation data from OpenRouteService API
     - Manual routes: Calculates from waypoint elevations
     - Only counts uphill segments (ignores downhill for "gain")
   - **Unit**: Displayed in feet

### 11. **Map Layers**
   - **Standard**: OpenStreetMap street map (MAPNIK tile source)
   - **Satellite**: USGS satellite imagery (USGS_SAT tile source)
   - **Toggle button**: Switches between layers with one tap

### 12. **Bottom Navigation Bar**
   - **What it does**: Main navigation between app sections
   - **Tabs**:
     - **Map**: Main map view
     - **Routes**: Saved routes list
     - **Create**: Route builder
     - **Stats**: Statistics dashboard
     - **Settings**: App settings (placeholder)
   - **Technology**: Jetpack Navigation Compose

### 13. **Route Information Popups**
   - **What it does**: Shows detailed info when tapping a route line on the map
   - **Displays**: Name, distance, elevation, time, type, difficulty
   - **Technology**: OSMDroid Marker overlays with info windows

### 14. **Offline Storage**
   - **What it does**: All routes stored locally, works without internet
   - **Technology**: Room Database (local SQLite database)
   - **Note**: Creating routes requires internet for smart routing API

### 15. **Error Handling**
   - **What it does**: Gracefully handles API failures
   - **Behavior**: If OpenRouteService fails, falls back to manual (straight line) segment while preserving previous smart-routed segments
   - **User feedback**: "Calculating..." indicator shows when API is working

---

## 🏗️ Technical Architecture

### Architecture Pattern: **MVVM (Model-View-ViewModel)**

```
┌─────────────────────────────────────────────┐
│                    UI Layer                  │
│  (Jetpack Compose - Screens & Components)   │
│  - MapScreen.kt                              │
│  - CreateRouteScreen.kt                      │
│  - RoutesScreen.kt                           │
│  - StatsScreen.kt                            │
└─────────────────┬───────────────────────────┘
                  │ observes StateFlow
                  │ calls functions
┌─────────────────▼───────────────────────────┐
│              ViewModel Layer                 │
│  (Business Logic & State Management)         │
│  - MapViewModel                              │
│  - CreateRouteViewModel                      │
│  - RouteListViewModel                        │
│  - StatsViewModel                            │
└─────────────────┬───────────────────────────┘
                  │ calls Repository
                  │ launches Coroutines
┌─────────────────▼───────────────────────────┐
│              Repository Layer                │
│  (Data Abstraction)                          │
│  - RouteRepository (interface)               │
│  - LocalRouteRepository (implementation)     │
└─────────────────┬───────────────────────────┘
                  │ accesses Database/Network
                  │
        ┌─────────┴─────────┐
        │                   │
┌───────▼────────┐  ┌──────▼──────────┐
│  Room Database │  │  Retrofit APIs  │
│  (Local Data)  │  │  (Remote Data)  │
│  - RouteDao    │  │  - ORS API      │
│  - RouteDB     │  │                 │
└────────────────┘  └─────────────────┘
```

### Why MVVM?

1. **Separation of Concerns**: UI, business logic, and data access are separate
2. **Testability**: Each layer can be tested independently
3. **Lifecycle Aware**: ViewModels survive configuration changes (screen rotations)
4. **Reactive**: UI automatically updates when data changes (via StateFlow)

---

## 🛠️ Technologies & Libraries Used

### **Core Technologies**

| Technology | Purpose | Why We Use It |
|-----------|---------|---------------|
| **Kotlin** | Programming language | Modern, concise, null-safe, official Android language |
| **Jetpack Compose** | UI framework | Declarative UI (describe what you want, not how to build it), less code than XML views |
| **Android SDK 34** | Platform APIs | Access to device features (GPS, storage, etc.) |

### **Jetpack Libraries** (Google's official Android components)

| Library | Purpose | Key Features Used |
|---------|---------|-------------------|
| **Navigation Compose** | Navigate between screens | Type-safe navigation, back stack management |
| **Lifecycle & ViewModel** | Manage UI state | Survives configuration changes, lifecycle-aware |
| **Room** | Local database | SQLite wrapper, compile-time SQL verification, type-safe queries |

### **Networking Libraries**

| Library | Purpose | How It's Used |
|---------|---------|---------------|
| **Retrofit** | HTTP client | Makes API calls to OpenRouteService, converts JSON to Kotlin objects |
| **OkHttp** | HTTP engine | Powers Retrofit, handles connections |
| **Logging Interceptor** | Debug API calls | Logs request/response for debugging |
| **Gson** | JSON parsing | Converts JSON from API into Kotlin data classes |

### **Map Libraries**

| Library | Purpose | Features Used |
|---------|---------|---------------|
| **OSMDroid** | Map rendering | Display OpenStreetMap tiles, draw routes, handle gestures |
| **Google Play Services Location** | GPS location | Get user's current location for map centering |

### **Coroutines**

| Component | Purpose | Usage |
|-----------|---------|-------|
| **Kotlin Coroutines** | Async programming | Handle API calls, database operations without blocking UI |
| **Flow/StateFlow** | Reactive data streams | Emit data changes that UI observes and reacts to |

### **Third-Party APIs**

| API | Purpose | Cost |
|-----|---------|------|
| **OpenRouteService** | Route calculation | Free tier: 2000 requests/day, provides GeoJSON routes with elevation |
| **OpenStreetMap** | Map tiles | Free and open source |
| **USGS** | Satellite imagery | Free US government data |

---

## 📱 App Screens - Detailed

### **1. Map Screen** (`MapScreen.kt`)

**Purpose**: Main hub for viewing all saved routes on an interactive map

**UI Components**:
- **Top Card**:
  - Title: "PathBuilder"
  - Search bar with magnifying glass icon
  - Filter chips: "Filters", "Topo/Standard" toggle
  - Live route count display
- **Map View**:
  - Interactive OSMDroid map
  - Blue lines showing all routes
  - Your location (blue dot with accuracy circle)
  - Clickable routes (tap to see details)
- **FAB (Floating Action Button)**: "Create Route" button (bottom right)

**ViewModel**: `MapViewModel`
- Manages: search query, filters, visible routes, map layer
- Functions: `updateSearchQuery()`, `toggleType()`, `toggleDifficulty()`, `toggleElevationRange()`, `toggleLayer()`

**State Management**:
```kotlin
data class MapUiState(
    routes: List<Route>,           // All routes from database
    visibleRoutes: List<Route>,    // Filtered routes
    searchQuery: String,            // Search text
    selectedTypes: Set<String>,     // Active type filters
    selectedDifficulties: Set<String>, // Active difficulty filters
    selectedElevationRanges: Set<String>, // Active elevation filters
    layer: MapLayer                 // STANDARD or SATELLITE
)
```

---

### **2. Route Builder Screen** (`CreateRouteScreen.kt`)

**Purpose**: Create new routes by placing pins on a map

**UI Components**:
- **Header**: "Build a new route" title, calculating indicator
- **Map View**: Interactive map for pin placement
- **Controls Row**:
  - "Start New" button (resets route, shows type selection)
  - Undo button (⟲)
  - Redo button (⟳)
  - "Topo/Standard" layer toggle
  - "Manual mode" switch
  - Route type display
- **Stats Display**:
  - Distance (miles)
  - Elevation gain (feet with ↑ arrow)
  - Estimated time (minutes)
  - Warning if manual mode active
  - Success message when saved
- **Save Button**: Opens dialog to name and save route

**ViewModel**: `CreateRouteViewModel`
- Manages: waypoints, route geometry, stats, undo/redo stacks
- Functions: `addPin()`, `undo()`, `redo()`, `startNewRoute()`, `toggleManualMode()`, `saveRoute()`

**Dialogs**:
1. **Route Type Selection** (shows first, and after "Start New"):
   - "Walking Route" button
   - "Driving Route" button
   - Cannot be dismissed without selection
2. **Save Route Dialog**:
   - Text field for route name
   - Shows selected type
   - "Save" and "Cancel" buttons

**How Pin Placement Works**:
1. User taps map → `addPin()` called with lat/lng
2. If not manual mode: calls OpenRouteService API
3. API returns detailed path geometry with elevation
4. UI updates with new line and stats
5. State saved to undo stack

---

### **3. Routes List Screen** (`RoutesScreen.kt`)

**Purpose**: Browse all saved routes in a list format

**UI Components**:
- **Header**: "Your Routes" title with route count
- **Search Bar**: Filter routes by name
- **Route Cards** (for each route):
  - Route name (large, bold)
  - Distance + Elevation + Time (icon + value)
  - Type badge (🚶 Walk or 🚗 Drive)
  - Difficulty badge (color-coded: Easy=green, Moderate=orange, Hard=red)

**ViewModel**: `RouteListViewModel`
- Manages: route list, search query
- Functions: `updateSearchQuery()`
- Data source: Observes Room database Flow

**Layout**: `LazyColumn` (vertically scrolling list)

---

### **4. Statistics Screen** (`StatsScreen.kt`)

**Purpose**: Show aggregate statistics across all routes

**UI Components**:
- **Header**: "Statistics" title with trophy icon
- **Stat Cards** (4 cards in 2x2 grid):
  1. **Total Routes**: Count of saved routes
  2. **Total Distance**: Sum of all route distances (miles)
  3. **Total Elevation**: Sum of all elevation gain (feet)
  4. **Average Distance**: Mean route distance
- **Card Design**: Icon on left, metric name, large number, unit label

**ViewModel**: `StatsViewModel`
- Manages: calculated statistics
- Functions: Calculates totals and averages from route list
- Updates: Automatically when routes change

**Calculations**:
```kotlin
totalRoutes = routes.size
totalDistance = routes.sumOf { it.distanceMiles }
totalElevation = routes.sumOf { it.elevationFeet }
averageDistance = totalDistance / totalRoutes (or 0.0 if no routes)
```

---

### **5. Settings Screen** (`SettingsScreen.kt`)

**Purpose**: App configuration (currently placeholders)

**UI Components**:
- **Header**: "Settings" with gear icon
- **Buttons** (placeholder functionality):
  - "Edit Profile"
  - "Sign Out"
  - "Report a Bug"

**Note**: This screen demonstrates UI structure but doesn't have implemented functionality. In a real app, these would manage user preferences, authentication, etc.

---

## ⚙️ How Each Feature Works

### **Feature: Smart Route Calculation**

**User Action**: User taps 3 points on the map to create a route

**What Happens**:

1. **Pin 1 placed**:
   ```
   addPin(lat1, lng1)
   → Only 1 point, no route yet
   → Display: 0.00 mi, 0 ft, 0 min
   ```

2. **Pin 2 placed**:
   ```
   addPin(lat2, lng2)
   → Now 2 points! Call OpenRouteService API
   → Create request: { coordinates: [[lng1, lat1], [lng2, lat2]], elevation: true }
   → API returns: GeoJSON with detailed path and elevation data
   → Extract: ~50 points between pin 1 and 2 (following roads)
   → Calculate: Distance, elevation gain, estimated time
   → Display: 2.4 mi, 150 ft, 48 min
   → Draw: Black line following the road path
   ```

3. **Pin 3 placed**:
   ```
   addPin(lat3, lng3)
   → 3 points total, call API with all 3
   → API calculates: Pin1→Pin2→Pin3 path
   → Returns: ~100 total points with elevation
   → Updates: 4.8 mi, 380 ft, 96 min
   → Draws: Complete route path
   ```

**If API Fails** (no internet, point unreachable):
```
→ Keep previous smart route (Pin1→Pin2)
→ Add straight line from Pin2→Pin3
→ Update distance and elevation for that segment only
→ User sees: Smart route preserved + straight line added
```

**Technologies Used**:
- `Kotlin Coroutines`: Run API call in background without freezing UI
- `Retrofit`: Make HTTP POST request to OpenRouteService
- `viewModelScope.launch`: Safely handle async operation tied to screen lifecycle
- `StateFlow`: UI automatically redraws when state updates

---

### **Feature: Undo/Redo**

**How It Works**:

**Data Structures**:
```kotlin
undoStack: MutableList<RouteSnapshot>  // History of previous states
redoStack: MutableList<RouteSnapshot>  // States that were undone

data class RouteSnapshot(
    waypoints: List<Waypoint>,
    routeGeometry: List<Waypoint>,
    distanceMiles: Double,
    elevationFeet: Double,
    estimatedTimeMinutes: Int
)
```

**When User Adds Pin**:
```
1. Save current state to undoStack
2. Clear redoStack (can't redo after new action)
3. Add pin and update route
4. Enable undo button
```

**When User Clicks Undo**:
```
1. Save current state to redoStack
2. Pop last state from undoStack
3. Restore that state (waypoints, geometry, stats)
4. Update UI with restored state
5. Enable redo button
```

**When User Clicks Redo**:
```
1. Save current state to undoStack
2. Pop last state from redoStack
3. Restore that state
4. Update UI
```

**UI State**:
```kotlin
canUndo = undoStack.isNotEmpty()  // Enables/disables undo button
canRedo = redoStack.isNotEmpty()  // Enables/disables redo button
```

---

### **Feature: Route Filtering**

**How Filters Work**:

**User selects filters** in dialog:
- Types: Walk, Drive (can select both)
- Difficulties: Easy, Moderate, Hard (can select multiple)
- Elevation: Flat, Low, Medium, High (can select multiple)

**Filter Logic** (in `MapViewModel`):
```kotlin
fun filterRoutes(routes: List<Route>): List<Route> {
    return routes.filter { route ->
        // Search filter
        val matchesSearch = searchQuery.isBlank() || 
                           route.name.contains(searchQuery, ignoreCase = true)
        
        // Type filter
        val matchesType = selectedTypes.isEmpty() || 
                         selectedTypes.contains(route.type)
        
        // Difficulty filter
        val matchesDifficulty = selectedDifficulties.isEmpty() || 
                               selectedDifficulties.contains(route.difficulty)
        
        // Elevation filter
        val elevationCategory = when {
            route.elevationFeet < 100 -> "Flat"
            route.elevationFeet < 500 -> "Low"
            route.elevationFeet < 1500 -> "Medium"
            else -> "High"
        }
        val matchesElevation = selectedElevationRanges.isEmpty() || 
                              selectedElevationRanges.contains(elevationCategory)
        
        // Must match ALL filters
        matchesSearch && matchesType && matchesDifficulty && matchesElevation
    }
}
```

**Example**:
```
User selects: Type=Walk, Difficulty=Hard
Result: Shows only walking routes with Hard difficulty
Hidden: All driving routes, all Easy/Moderate routes
```

---

### **Feature: Database Persistence**

**Room Database Structure**:

**Entity** (Database Table):
```kotlin
@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String,
    val distanceMiles: Double,
    val elevationFeet: Double,
    val difficulty: String,
    val type: String,
    val estimatedTimeMinutes: Int,
    val waypoints: List<Waypoint>,        // Stored as JSON string
    val routeGeometry: List<Waypoint>     // Stored as JSON string
)
```

**DAO** (Database Access Object):
```kotlin
@Dao
interface RouteDao {
    @Query("SELECT * FROM routes")
    fun getAll(): Flow<List<RouteEntity>>   // Observable list
    
    @Insert
    suspend fun insert(route: RouteEntity)
    
    @Delete
    suspend fun delete(route: RouteEntity)
}
```

**Type Converters** (for complex types):
```kotlin
@TypeConverter
fun fromWaypointList(list: List<Waypoint>): String {
    return Gson().toJson(list)  // Convert to JSON string
}

@TypeConverter
fun toWaypointList(json: String): List<Waypoint> {
    return Gson().fromJson(json, List::class.java)  // Parse JSON
}
```

**Save Flow**:
```
User clicks "Save" 
→ CreateRouteViewModel.saveRoute()
→ Create RouteEntity from current state
→ Call repository.insertRoute(entity)
→ Repository calls dao.insert(entity)
→ Room writes to SQLite database file
→ Database Flow emits updated list
→ All screens observing routes automatically update
```

**Load Flow**:
```
App starts
→ ViewModels observe repository.getRoutes()
→ Repository returns dao.getAll() Flow
→ Room reads from database
→ Maps RouteEntity → Route (domain model)
→ UI receives updates via StateFlow
```

---

## 🗄️ Database Design

### **Database Name**: `route_database`
### **Version**: 3
### **Location**: `/data/data/com.example.pathbuilder/databases/route_database`

### **Table: `routes`**

| Column Name | Data Type | Constraints | Description |
|------------|-----------|-------------|-------------|
| `id` | INTEGER | PRIMARY KEY, AUTO INCREMENT | Unique route identifier |
| `name` | TEXT | NOT NULL | User-provided route name |
| `distanceMiles` | REAL | NOT NULL | Total distance in miles |
| `elevationFeet` | REAL | NOT NULL | Total elevation gain in feet |
| `difficulty` | TEXT | NOT NULL | "Easy", "Moderate", or "Hard" |
| `type` | TEXT | NOT NULL | "walk" or "drive" |
| `estimatedTimeMinutes` | INTEGER | NOT NULL | Calculated travel time |
| `waypoints` | TEXT | NOT NULL | JSON array of pin locations |
| `routeGeometry` | TEXT | NOT NULL | JSON array of detailed path points |

### **Example Database Row**:

```sql
INSERT INTO routes VALUES (
    1,
    'Mount Tam Hike',
    5.2,
    1200,
    'Hard',
    'walk',
    104,
    '[{"latitude":37.9235,"longitude":-122.5964,"elevationFeet":800},{"latitude":37.9280,"longitude":-122.5990,"elevationFeet":1500}]',
    '[{"latitude":37.9235,"longitude":-122.5964,"elevationFeet":800},{"latitude":37.9238,"longitude":-122.5966,"elevationFeet":815},...(98 more points)...,{"latitude":37.9280,"longitude":-122.5990,"elevationFeet":1500}]'
);
```

### **Why Two Coordinate Lists?**

1. **`waypoints`**: User-placed pins (typically 2-10 points)
   - Used to recreate the route if editing
   - Small and efficient

2. **`routeGeometry`**: Detailed path (50-200 points)
   - Follows actual roads/trails from OpenRouteService
   - Used to draw accurate route lines on map
   - Includes elevation data for every point

---

## 🌐 API Integration

### **OpenRouteService API**

**Base URL**: `https://api.openrouteservice.org/`

**Endpoint Used**: `POST /v2/directions/{profile}/geojson`

**Profiles**:
- `foot-walking`: For hiking/walking routes
- `driving-car`: For driving routes

**Request Format**:
```json
{
  "coordinates": [
    [-122.4194, 37.7749],   // [longitude, latitude]
    [-122.4100, 37.7800]
  ],
  "elevation": true,
  "instructions": false
}
```

**Response Format** (GeoJSON):
```json
{
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "geometry": {
        "type": "LineString",
        "coordinates": [
          [-122.4194, 37.7749, 10.5],  // [lon, lat, elevation_meters]
          [-122.4195, 37.7750, 11.2],
          // ... ~50-200 more points following roads
          [-122.4100, 37.7800, 15.8]
        ]
      },
      "properties": {
        "summary": {
          "distance": 1609.34,  // meters
          "duration": 1200      // seconds
        },
        "ascent": 50.5,         // meters climbed
        "descent": 20.2         // meters descended
      }
    }
  ]
}
```

**Retrofit Interface**:
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

**How We Use It**:
1. User places 2+ pins
2. Convert pins to coordinate array: `[[lng, lat], [lng, lat]]`
3. Make API call with Retrofit
4. Parse GeoJSON response
5. Extract coordinates and convert elevation (meters → feet)
6. Create Waypoint objects from coordinates
7. Update UI with route line and stats

**Error Handling**:
- Network error → Fall back to straight line
- API rate limit → Fall back to straight line
- Invalid coordinates → Show error message

**Rate Limits**:
- Free tier: 2000 requests/day
- ~500 requests/hour during development testing

---

## 🔄 State Management

### **StateFlow Pattern**

**What is State?**
State is the current data that the UI displays. Examples:
- List of routes
- Search query text
- Map zoom level
- Whether a button is enabled

**Why StateFlow?**
- **Reactive**: UI automatically updates when state changes
- **Lifecycle-safe**: Only updates active screens
- **Type-safe**: Kotlin ensures correct data types

**Pattern Used**:

```kotlin
// In ViewModel
private val _uiState = MutableStateFlow(MapUiState())  // Private, mutable
val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()  // Public, read-only

// To update state
fun updateSearchQuery(query: String) {
    _uiState.value = _uiState.value.copy(searchQuery = query)
}
```

**In Compose UI**:
```kotlin
@Composable
fun MapScreen(viewModel: MapViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    // UI automatically rebuilds when uiState changes
    Text(text = "Found ${uiState.visibleRoutes.size} routes")
}
```

### **State Flow Diagram**:

```
User Action (tap button)
    ↓
Composable calls ViewModel function
    ↓
ViewModel updates MutableStateFlow
    ↓
StateFlow emits new value
    ↓
Composable collectAsState() receives update
    ↓
Composable function recomposes (re-runs)
    ↓
UI updates on screen
```

---

## 🧭 Navigation System

### **Navigation Graph**

```
                ┌─────────────┐
                │  Main Scaffold  │
                │  (Bottom Nav)   │
                └────────┬────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
   ┌────▼────┐     ┌────▼────┐     ┌────▼────┐
   │   Map   │     │ Routes  │     │ Create  │
   │ Screen  │     │  Screen │     │  Screen │
   └─────────┘     └─────────┘     └────┬────┘
                         │               │
                         │          ┌────▼────┐
                         │          │  Stats  │
                         │          │  Screen │
                         │          └─────────┘
                         │
                    ┌────▼──────┐
                    │ Settings  │
                    │  Screen   │
                    └───────────┘
```

### **How Navigation Works**:

**1. Define Routes** (enum):
```kotlin
enum class AppScreen {
    MAP,
    ROUTES,
    CREATE,
    STATS,
    SETTINGS
}
```

**2. Set Up NavHost**:
```kotlin
NavHost(navController, startDestination = AppScreen.MAP.name) {
    composable(AppScreen.MAP.name) { MapScreen(...) }
    composable(AppScreen.ROUTES.name) { RoutesScreen(...) }
    composable(AppScreen.CREATE.name) { CreateRouteScreen(...) }
    composable(AppScreen.STATS.name) { StatsScreen(...) }
    composable(AppScreen.SETTINGS.name) { SettingsScreen(...) }
}
```

**3. Navigate Between Screens**:
```kotlin
// In bottom nav bar
BottomNavigationItem(
    selected = currentScreen == AppScreen.MAP,
    onClick = { navController.navigate(AppScreen.MAP.name) }
)
```

**Navigation Features**:
- **Back Stack**: Android back button works correctly
- **State Preservation**: Screens remember their state when navigated away
- **Deep Linking**: (Not implemented, but framework supports it)
- **Arguments**: Can pass data between screens (not used in this app)

---

## 💾 Code Organization

### **Package Structure**:

```
com.example.pathbuilder/
│
├── app/                          # Application-level setup
│   ├── AppContainer.kt           # Dependency injection container
│   ├── AppScreen.kt              # Navigation route enum
│   └── PathBuilderApp.kt         # Main app composable with NavHost
│
├── data/                         # Data layer
│   ├── RouteRepository.kt        # Repository interface (abstraction)
│   └── LocalRouteRepository.kt   # Room implementation of repository
│
├── database/                     # Room database
│   ├── RouteDao.kt               # Database queries
│   ├── RouteDatabase.kt          # Database instance
│   ├── RouteEntity.kt            # Database table entity
│   └── RouteTypeConverters.kt    # JSON converters for complex types
│
├── model/                        # Domain models
│   ├── Route.kt                  # Main route data class
│   └── Waypoint.kt               # Coordinate data class
│
├── network/                      # API services
│   └── OpenRouteServiceApi.kt    # Retrofit interface + data models
│
├── ui/                           # UI layer
│   ├── components/               # Reusable UI pieces
│   │   ├── BottomNavBar.kt       # Bottom navigation bar
│   │   ├── FilterDialog.kt       # Filter selection dialog
│   │   ├── MapboxMapView.kt      # OSMDroid map wrapper
│   │   └── RouteCard.kt          # Route list item card
│   │
│   ├── create/                   # Route creation screen
│   │   └── CreateRouteScreen.kt
│   │
│   ├── map/                      # Main map screen
│   │   └── MapScreen.kt
│   │
│   ├── routes/                   # Routes list screen
│   │   └── RoutesScreen.kt
│   │
│   ├── settings/                 # Settings screen
│   │   └── SettingsScreen.kt
│   │
│   ├── stats/                    # Statistics screen
│   │   └── StatsScreen.kt
│   │
│   └── theme/                    # App styling
│       ├── Color.kt              # Color definitions
│       ├── Theme.kt              # Material3 theme setup
│       └── Type.kt               # Typography (fonts, sizes)
│
├── viewmodel/                    # ViewModels (business logic)
│   ├── CreateRouteViewModel.kt   # Route creation logic
│   ├── MapViewModel.kt           # Map display logic
│   ├── RouteListViewModel.kt     # Routes list logic
│   └── StatsViewModel.kt         # Statistics calculations
│
└── MainActivity.kt               # App entry point
```

### **File Size Breakdown**:

| File | Lines of Code | Complexity |
|------|---------------|------------|
| `CreateRouteViewModel.kt` | 410 | High (API calls, undo/redo, calculations) |
| `MapViewModel.kt` | 167 | Medium (filtering, state management) |
| `MapboxMapView.kt` | 252 | High (OSMDroid integration, overlays) |
| `CreateRouteScreen.kt` | 244 | Medium (UI layout, dialogs) |
| `MapScreen.kt` | 129 | Low (mostly UI) |
| `OpenRouteServiceApi.kt` | 92 | Medium (API models) |
| `RouteCard.kt` | 80 | Low (UI component) |

**Total Project**: ~3477 lines of Kotlin code

---

## 🚀 Setup Instructions

### **For Users Cloning This Repository**

#### **Prerequisites**:
1. **Android Studio** (latest version)
   - Download: https://developer.android.com/studio
   - Works on: Windows, macOS, Linux

2. **OpenRouteService API Key** (free)
   - Sign up: https://openrouteservice.org/dev/#/signup
   - Free tier: 2000 requests/day
   - Takes 2 minutes to get

#### **Step-by-Step Setup**:

**1. Clone Repository**
```bash
git clone https://github.com/jakedcl/pathbuilder.git
cd pathbuilder
```

**2. Configure API Key**
```bash
# Copy the template
cp gradle.properties.template gradle.properties

# Edit gradle.properties
# Replace YOUR_API_KEY_HERE with your actual ORS API key
```

**3. Open in Android Studio**
- Launch Android Studio
- Click "Open an Existing Project"
- Navigate to `pathbuilder` folder
- Click "OK"

**4. Wait for Gradle Sync**
- Android Studio automatically downloads dependencies
- This takes 2-5 minutes on first run
- Watch the bottom status bar for progress

**5. Run the App**
- Click green "Run" button (or Shift+F10)
- Choose a device:
  - **Emulator**: Create one in Device Manager (Pixel 4, API 34)
  - **Physical Device**: Enable Developer Options + USB Debugging
- Wait for app to install and launch

**6. Grant Permissions**
- When app opens, grant location permissions
- This allows the map to center on your location

#### **Troubleshooting**:

| Error | Solution |
|-------|----------|
| "Plugin not found: com.google.devtools.ksp" | Update Gradle: `./gradlew clean build` |
| "ORS_API_KEY not found" | Make sure `gradle.properties` exists and has your key |
| Map tiles not loading | Check internet connection, disable VPN if active |
| "Cleartext traffic not permitted" | Add network security config (already done in this repo) |

---

## 🎨 Design Decisions

### **Why Jetpack Compose?**
- **Modern**: Google's recommended UI toolkit (2021+)
- **Declarative**: Describe what you want, not how to build it
- **Less Code**: ~40% less code than XML views
- **Preview**: See UI in Android Studio without running app
- **Type-Safe**: Compile-time checks for UI code

### **Why MVVM Architecture?**
- **Industry Standard**: Used by most professional Android apps
- **Separation**: UI and logic are separate (easier to test)
- **Lifecycle Aware**: ViewModels survive screen rotations
- **Scalable**: Easy to add new features without breaking existing code

### **Why Room Database?**
- **Type-Safe**: SQL queries checked at compile time
- **Less Boilerplate**: Less code than raw SQLite
- **Reactive**: Database changes automatically update UI
- **Migrations**: Built-in support for schema changes

### **Why OpenRouteService?**
- **Free**: 2000 requests/day (enough for development + light use)
- **Elevation Data**: Returns altitude for every point
- **Multiple Profiles**: Walking, driving, cycling, wheelchair
- **Open Source**: Based on OpenStreetMap data

### **Why OSMDroid (not Google Maps)?**
- **Free**: No API key required for map display
- **No Credit Card**: Google Maps requires billing account
- **Open Data**: Uses OpenStreetMap (community-maintained)
- **Offline Support**: Can download tiles for offline use

---

## 📊 Key Algorithms

### **1. Haversine Distance Formula**

**Purpose**: Calculate distance between two GPS coordinates

**Formula**:
```
a = sin²(Δlat/2) + cos(lat1) · cos(lat2) · sin²(Δlon/2)
c = 2 · atan2(√a, √(1−a))
distance = R · c
```

Where:
- R = Earth's radius (6,371 km or 3,959 miles)
- Δlat = difference in latitude (radians)
- Δlon = difference in longitude (radians)

**Code**:
```kotlin
fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0  // Earth radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2)
    
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c  // Distance in meters
}
```

### **2. Elevation Gain Calculation**

**Purpose**: Calculate total uphill elevation

**Algorithm**:
```kotlin
fun computeElevationGain(points: List<Waypoint>): Double {
    var gain = 0.0
    points.windowed(2).forEach { (p1, p2) ->
        val diff = p2.elevationFeet - p1.elevationFeet
        if (diff > 0) gain += diff  // Only count gains, ignore descents
    }
    return gain
}
```

**Example**:
```
Points: [100ft, 150ft, 120ft, 180ft]
Segments: 
  100→150 = +50 (gain)
  150→120 = -30 (descent, ignore)
  120→180 = +60 (gain)
Total: 50 + 60 = 110 feet elevation gain
```

### **3. Estimated Time Calculation**

**Purpose**: Estimate how long the route will take

**Formula**:
```
time = distance / speed
```

**Speeds**:
- Walking: 3 mph
- Driving: 30 mph (accounting for turns, stops)

**Code**:
```kotlin
fun estimateTravelMinutes(distanceMiles: Double, type: String): Int {
    val speedMph = if (type == "drive") 30.0 else 3.0
    return ((distanceMiles / speedMph) * 60).toInt()
}
```

**Example**:
```
Walking 6 miles:
  time = 6 miles / 3 mph = 2 hours = 120 minutes

Driving 6 miles:
  time = 6 miles / 30 mph = 0.2 hours = 12 minutes
```

---

## 🔧 Build Configuration

### **Gradle Files**:

**`build.gradle.kts` (app level)**:
- Minimum SDK: 24 (Android 7.0, covers 94% of devices)
- Target SDK: 34 (Android 14)
- Compile SDK: 34
- Kotlin version: 1.9.20
- Compose Compiler: 1.5.4

**Key Dependencies**:
```kotlin
dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    
    // Jetpack Compose
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui:1.7.5")
    implementation("androidx.compose.material3:material3")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.4")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Maps
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    
    // Icons
    implementation("androidx.compose.material:material-icons-extended")
}
```

---

## 🎓 Learning Outcomes

### **What This Project Demonstrates**:

1. **Modern Android Development**
   - Kotlin language features
   - Jetpack Compose declarative UI
   - Material Design 3

2. **Architecture Patterns**
   - MVVM (Model-View-ViewModel)
   - Repository pattern
   - Dependency injection

3. **Asynchronous Programming**
   - Kotlin Coroutines
   - Suspend functions
   - Flow and StateFlow

4. **Local Data Persistence**
   - Room database
   - Type converters
   - Flow-based updates

5. **Network Communication**
   - REST API integration
   - JSON parsing
   - Error handling

6. **Map Integration**
   - OSMDroid library
   - GPS location
   - Overlay drawing

7. **State Management**
   - Reactive UI updates
   - ViewModel state
   - Compose recomposition

8. **User Experience**
   - Navigation
   - Dialogs and forms
   - Loading states
   - Error messages

---

## 📈 Potential Improvements

### **Features That Could Be Added**:

1. **Route Editing**
   - Modify saved routes
   - Delete individual pins
   - Reorder waypoints

2. **Route Sharing**
   - Export routes as GPX files
   - Share via text/email
   - QR code generation

3. **Offline Maps**
   - Download map tiles
   - Cache routes locally
   - Use without internet

4. **User Authentication**
   - Firebase login
   - Cloud sync between devices
   - Social features

5. **Photos**
   - Add photos to waypoints
   - Gallery view
   - Camera integration

6. **Weather Integration**
   - Current conditions along route
   - Forecast for planned routes
   - Weather alerts

7. **Export Options**
   - PDF route cards
   - CSV data export
   - KML/KMZ for Google Earth

8. **Advanced Statistics**
   - Charts and graphs
   - Monthly summaries
   - Personal records

---

## 🔒 Security & Privacy

### **API Key Security**:
- **Storage**: API key in `gradle.properties` (gitignored)
- **Build Config**: Injected at compile time via BuildConfig
- **Not in Code**: Never hardcoded in source files
- **Public Repos**: Use template file, users provide own keys

### **User Data**:
- **Local Only**: All routes stored on device
- **No Cloud**: No data sent to our servers
- **No Tracking**: No analytics or user tracking
- **Permissions**: Only location (for map centering)

### **API Calls**:
- **HTTPS Only**: All network requests encrypted
- **Rate Limiting**: Respects OpenRouteService limits
- **Error Handling**: Graceful fallbacks on failures

---

## 📝 License & Attribution

**Project**: Created for CSC 438 Mobile Development

**Attribution**:
- **Map Tiles**: © OpenStreetMap contributors
- **Satellite Imagery**: USGS (US Geological Survey)
- **Routing**: OpenRouteService
- **Icons**: Material Design Icons

**Libraries**: See [Technologies & Libraries](#technologies--libraries-used) for full list

---

## 🎤 Presentation Tips

### **Suggested Slide Breakdown**:

1. **Title Slide**
   - App name, your name, course
   
2. **Problem Statement**
   - Why this app is useful
   - Target users

3. **App Demo**
   - Live demo or video
   - Show all 5 screens

4. **Architecture Overview**
   - MVVM diagram
   - Why MVVM?

5. **Key Technologies**
   - Kotlin, Compose, Room, Retrofit
   - One slide per major tech

6. **Database Design**
   - Table structure
   - Why Room?

7. **API Integration**
   - OpenRouteService explanation
   - Request/response flow

8. **Smart Features**
   - Undo/Redo
   - Smart routing
   - Difficulty calculation

9. **Challenges & Solutions**
   - API errors → fallback
   - State management → StateFlow
   - Complex UI → Compose

10. **Lessons Learned**
    - What you learned
    - What you'd do differently

11. **Future Improvements**
    - 3-5 potential features

12. **Thank You / Questions**
    - GitHub link
    - Demo time

---

## 📞 Support

**Issues**: Report bugs via GitHub Issues
**Questions**: Contact via course communication channels

**GitHub Repository**: https://github.com/jakedcl/pathbuilder

---

**Built with ❤️ for CSC 438 Mobile Development**
