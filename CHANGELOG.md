# CHANGELOG.md - Project History

## [8.1.0] - Strict Email Format Validation & Per-User Storage Partitioning

### Fixed
- Fixed security issue where typing incomplete email strings (e.g. `"wcs"`) allowed login.
- Added strict `isValidEmail` function checking full domain formatting (`user@domain.ext`).
- Blocked invalid Google custom entries with a clear red error callout in `AuthScreen.kt`.

### Added
- Partitioned local disk persistence by user account ID (`still_leaf_plants_${userId}.json`) in `PlantPersistenceManager.kt`.
- Signing out clears in-memory plant list, guaranteeing 100% data separation between user accounts.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [8.0.0] - Gentle Bamboo Breeze Ambient Background Music & Audio Settings Toggle

### Added
- Imported `Gentle Bamboo Breeze.mp3` as raw audio resource.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
