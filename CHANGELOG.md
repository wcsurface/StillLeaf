# CHANGELOG.md - Project History

## [8.4.0] - Screen Rotation Uninterrupted Audio Playback Fix

### Fixed
- Fixed bug where rotating the phone screen restarted background music from the beginning.
- Configured `android:configChanges` on `MainActivity` in `AndroidManifest.xml` to prevent Activity destruction during screen orientation changes.
- Updated `BackgroundMusicManager.kt` to preserve exact millisecond playback position (`savedPlaybackPosition`) across lifecycle transitions.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [8.3.0] - Persistent User Authentication Sessions

### Added
- Created `UserSessionManager.kt` to persist authenticated user account sessions in `SharedPreferences`.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
