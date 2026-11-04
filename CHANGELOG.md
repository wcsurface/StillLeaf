# CHANGELOG.md - Project History

## [49.0.0] - PostgreSQL Auto-User Sync & Email Cloud Restoration Architecture

### Fixed
- Implemented `POST /api/plants/sync` auto-user upsert endpoint in `plantRoutes.ts` to prevent foreign key constraint failures on plant saves.
- Updated `GET /api/plants` to search by email address, ensuring plant sanctuary restoration across all reinstalls and logins.
- Updated `PlantCloudSyncManager.kt` and `MainActivity.kt` to pass user email during cloud backup and restoration.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [48.0.0] - Instant PlantDetailScreen Task Completion Checkmark State Sync

### Fixed
- Updated `onTaskToggled` in `MainActivity.kt` to update `selectedPlant` state when care tasks are toggled.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
