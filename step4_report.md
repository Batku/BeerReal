
# Step 4 Report

## BeerReal

## Team Members

Hendrik Aarma<br>
Gloria Rannik<br>
Kevin Näälik<br>
Tamor Tomson<br>

## Testing Strategy
- Unit tests present (e.g. ValidationUtils caption rules).
- One Compose instrumentation test for navigation (Profile → Settings → Back).
- Test dependencies (JUnit, AndroidX, Compose test libs) and runner configured.
- Coverage still minimal: no tests for networking, location, persistence, or broader UI flows.

## APK Build Process
- Gradle (Kotlin DSL), Android app module with Compose enabled.
- SDK: min 31 / target 36; versionCode 1, versionName 1.0.
- Dependencies include Navigation, Lifecycle, Maps/Location, Room, DataStore, OkHttp, Coroutines.
- Release build currently without minification (per script).
- Signing: production signing is configured

## Known Limitations / Gaps
- No backend api - limited functionality and mock data
