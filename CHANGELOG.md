# CHANGELOG.md - Project History

## [50.0.0] - Single-Tap Task Toggle Fix & Zero Data Loss Protection

### Fixed
- Fixed double-tap cancellation bug on `CareTaskActionCard` in `PlantDetailScreen.kt` by removing duplicate `.clickable` modifier on inner checkmark `Box`.
- Tapping care task action cards on plant detail pages now instantly checks off watering, feeding, rotating, and pruning tasks on the first tap.
- 100% Zero Data Loss: All existing plant data, photos, care logs, and locations preserved.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [49.0.0] - PostgreSQL Auto-User Sync & Email Cloud Restoration Architecture

### Fixed
- Implemented `POST /api/plants/sync` auto-user upsert endpoint in `plantRoutes.ts`.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
