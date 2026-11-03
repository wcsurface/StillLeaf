# CHANGELOG.md - Project History

## [48.0.0] - Instant PlantDetailScreen Task Completion Checkmark State Sync

### Fixed
- Updated `onTaskToggled` in `MainActivity.kt` to update `selectedPlant` state when care tasks are toggled on `PlantDetailScreen.kt`.
- Tapping task checkmark boxes on plant detail pages now instantly updates the UI state, fills the checkmark green, and persists care logs.
- Installed directly onto connected device via ADB (`app:installDebug`).
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [47.0.0] - 4 Requested UI/UX Updates & Release Bundle Delivery (v3.4, Build 25)

### Updated
- Added direct `.clickable` handler to checkmark circular box on `CareTaskActionCard`.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
