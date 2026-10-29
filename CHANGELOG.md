# CHANGELOG.md - Project History

## [6.3.0] - Automated Local Disk Storage & Account Persistence

### Added
- Created `PlantPersistenceManager.kt` to auto-save and restore plant profiles, photos, and care logs on local device disk storage (`still_leaf_plants.json`).
- Prevents plant data loss during app rebuilds, updates, or process restarts.
- Confirmed PostgreSQL user account isolation (`userId` foreign key binding) for cloud data sync.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [6.2.0] - AuthScreen Header Streamlining

### Removed
- Removed the `"Sign in to access your Botanical Sanctuary"` pill badge from `AuthScreen.kt`.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
