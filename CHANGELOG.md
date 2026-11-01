# CHANGELOG.md - Project History

## [8.9.0] - Indoor/Outdoor Environment Selector & Required Location Validation

### Added
- Added Indoor / Outdoor Environment Toggle on `AddPlantScreen.kt` and `EditPlantDialog` in `PlantDetailScreen.kt`.
- Added an empty, required location detail input field on both screens.
- Added strict validation preventing saving if location detail is left blank.
- Formats plant location string as `"${Indoor/Outdoor} • ${Location Detail}"`.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [8.8.0] - App Launcher Icon Force Cache Invalidation & Version Bump (v1.1)

### Fixed
- Fixed Moto Launcher / Android launcher icon caching issue by bumping `versionCode = 2` (`versionName = "1.1"`).
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
