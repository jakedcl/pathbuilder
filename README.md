# PathBuilder

An Android app for creating and managing hiking/driving routes with smart route calculation and elevation tracking.

## Features

- **Map View**: View all saved routes on an interactive map with OpenStreetMap/Satellite layers
- **Route Builder**: Create custom routes by placing pins on the map
  - Smart routing using OpenRouteService API (auto-snaps to roads/trails)
  - Manual mode for straight-line routes
  - Real-time elevation and distance calculation
  - Undo/Redo functionality
- **Route Management**: Save, view, and filter routes
- **Statistics**: Track your total miles, elevation gain, and routes
- **Filters**: Filter routes by type (walk/drive), difficulty, and elevation gain

## Setup Instructions

### Prerequisites
- Android Studio (latest version recommended)
- Android SDK API 34+
- An OpenRouteService API key (free)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/jakedcl/pathbuilder.git
   cd pathbuilder
   ```

2. **Configure API Key**
   - Copy `gradle.properties.template` to `gradle.properties`
     ```bash
     cp gradle.properties.template gradle.properties
     ```
   - Get a free API key from [OpenRouteService](https://openrouteservice.org/dev/#/signup)
   - Open `gradle.properties` and replace `YOUR_API_KEY_HERE` with your actual API key:
     ```
     ORS_API_KEY=your_actual_api_key_here
     ```

3. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned `pathbuilder` directory
   - Click "OK"

4. **Sync and Build**
   - Android Studio will automatically sync Gradle
   - Wait for the sync to complete
   - Build the project (Build → Make Project)

5. **Run the App**
   - Connect an Android device or start an emulator
   - Click the "Run" button or press Shift+F10
   - Grant location permissions when prompted

## Technologies Used

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room
- **Networking**: Retrofit + OkHttp
- **Maps**: OSMDroid (OpenStreetMap)
- **Routing**: OpenRouteService API
- **Navigation**: Jetpack Navigation Compose
- **Dependency Injection**: Manual DI with AppContainer

## Project Structure

```
app/src/main/java/com/example/pathbuilder/
├── app/                    # Application setup (AppContainer, Navigation)
├── data/                   # Repository interfaces and implementations
├── database/               # Room database entities, DAOs, and converters
├── model/                  # Data models (Route, Waypoint)
├── network/                # Retrofit API interfaces
├── ui/                     # Compose UI screens and components
│   ├── components/         # Reusable UI components
│   ├── create/            # Route creation screen
│   ├── map/               # Main map screen
│   ├── routes/            # Saved routes list
│   ├── settings/          # Settings screen
│   └── stats/             # Statistics screen
└── viewmodel/             # ViewModels for each screen
```

## API Key Security

⚠️ **Important**: Never commit your `gradle.properties` file with your actual API key to a public repository. This file is gitignored for security reasons.

## Compatibility

This project works on:
- ✅ Windows
- ✅ macOS
- ✅ Linux

Android Studio and Gradle are cross-platform, so projects created on one OS work on all others.

## License

This project was created as a class project for CSC 438 Mobile Development.

## Credits

- Maps: [OpenStreetMap](https://www.openstreetmap.org/)
- Routing: [OpenRouteService](https://openrouteservice.org/)
- Satellite imagery: USGS

