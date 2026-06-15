# Moises Challenge

<img width="320" height="714" alt="image" src="https://github.com/user-attachments/assets/3359b551-c316-472d-b786-1dac984203f6" />


Android music search app built with Kotlin and Jetpack Compose. The app searches songs through the public iTunes Search API, stores results locally with Room, and presents a simple music browsing flow with recently played songs, song details, and album details.

## Features

- Search songs from iTunes with debounced input.
- Paginated search results with local caching.
- Recently played list backed by Room.
- Song details screen with artwork, metadata, preview playback, timeline seeking, and 10-second forward/backward controls.
- Album details screen populated from the iTunes lookup API.
- Loading, empty, and error states for the main screens.
- Compose previews for key UI states.

## Tech Stack

- Kotlin
- Jetpack Compose and Material 3
- AndroidX Navigation Compose
- Hilt for dependency injection
- Retrofit, OkHttp, and Kotlinx Serialization for networking
- Room for local persistence
- Coil for artwork loading
- Coroutines and Flow for async state
- JUnit, MockK, Turbine, Robolectric, and Room testing utilities

## Requirements

- Android Studio with Android Gradle Plugin 9.2.1 support
- JDK 11 or newer
- Android SDK 36
- Network access for iTunes API requests

The project uses the Gradle wrapper, so a separate Gradle installation is not required.

## Getting Started

Clone the project and open it in Android Studio:

```bash
git clone <repository-url>
cd moises-challenge
```

Let Android Studio sync the Gradle project, then run the `app` configuration on an emulator or physical device.

You can also build from the command line:

```bash
./gradlew assembleDebug
```

Install the debug build on a connected device:

```bash
./gradlew installDebug
```

## Testing

Run the JVM unit test suite:

```bash
./gradlew test
```

Run instrumented Android tests on a connected emulator or device:

```bash
./gradlew connectedAndroidTest
```

## Project Structure

```text
app/src/main/java/com/lucasbueno/moises_challenge
├── data
│   ├── local        # Room database, DAOs, entities, and local data source
│   ├── remote       # iTunes API, DTOs, and remote data source
│   └── repository   # MusicRepository implementation
├── di               # Hilt modules
├── domain
│   ├── cache        # Recently played cache recycling
│   ├── model        # Domain models and cache policy
│   └── repository   # Repository contract
└── presentation
    ├── component    # Reusable Compose UI components
    ├── feature      # Splash, songs, song details, and album screens
    ├── navigation   # Navigation routes and host
    └── theme        # App theme, colors, type, and dimensions
```

## Data Flow

`SongsViewModel`, `SongDetailsViewModel`, and `AlbumViewModel` depend on the `MusicRepository` domain contract. `MusicRepositoryImpl` coordinates the remote iTunes data source and the Room-backed local data source:

1. Search requests fetch songs from iTunes and cache them locally.
2. UI screens collect Room `Flow`s so cached changes update the UI automatically.
3. Starting a song preview marks it as recently played.
4. Album screens refresh album tracks through the iTunes lookup endpoint and cache the returned songs.

## Preview Playback

The player screen uses the `previewUrl` returned by the iTunes Search API and plays it with Android `MediaPlayer`. Playback is local to the app: the iTunes API provides song metadata and preview audio URLs, but it does not provide playback commands, queue state, or full-track streaming.

Supported player actions:

- Play and pause the preview audio.
- Seek with the timeline slider.
- Jump backward or forward by 10 seconds.
- Display elapsed and remaining preview time.

If a song result does not include a preview URL, or the preview cannot be loaded, the player controls are disabled and the screen shows a preview unavailable message.

## Notes

- The app does not require API keys; it uses `https://itunes.apple.com/`.
- Room schema output is stored under `app/schemas`.
- iTunes preview audio is short promotional audio, not full song playback.
