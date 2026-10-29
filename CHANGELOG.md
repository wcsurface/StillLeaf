# CHANGELOG.md - Project History

## [7.8.0] - Unified Cross-Screen Care Task State Synchronization

### Fixed
- Fixed bug where tasks checked on `CalendarScreen` did not show as checked on `PlantDetailScreen` (and vice-versa).
- Updated `PlantDetailScreen.kt` to compute completion status directly from `plant.recentLogs` for today's date.
- Connected task action cards to `onTaskToggled` callback in `MainActivity.kt` so toggles on either page persist to local storage and update both screens in real time simultaneously.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [7.7.0] - Front Camera Clearance & Bottom-Anchored Legal Footer

### Changed
- Increased top padding of `StillLeafTopBar.kt` to `52.dp` to clear the front-facing camera cutout on every screen.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
