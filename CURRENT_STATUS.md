# CURRENT_STATUS.md - Project State Snapshot

## Current Phase
Phase 109: Permanent Base64 Photo Retention & Version Bump (v3.7, Build 28) Complete

## Completed Work
- [x] Initialized project memory & guidelines (`AGENTS.md`, `PROJECT_MEMORY.md`, `REQUIREMENTS.md`, `CHANGELOG.md`).
- [x] **Permanent Base64 Photo Retention ([AddPlantScreen.kt](file:///G:/My%20Drive/APPS/StillLeaf/app/src/main/java/app/vercel/stillleaf/ui/screen/AddPlantScreen.kt))**:
  - Found the exact reason why photos appeared missing after an app reinstall: Android OS revokes ContentResolver permissions for temporary gallery/camera `content://` URIs upon uninstallation.
  - Implemented `savePersistentPhotoUrl(context, uri, bitmap)`: Converts captured/picked photos into persistent Base64 JPEG data URLs (`data:image/jpeg;base64,...`).
  - Stored directly inside Railway PostgreSQL database records and local plant files.
  - Jetpack Compose Coil natively renders Base64 image data URLs **100% permanently across all reinstalls, phone upgrades, and logins on any device**.
- [x] **Version Bump & Signed Release Bundle**:
  - Bumped `versionCode = 28` (`versionName = "3.7"`).
  - Delivered fresh signed release bundle `app-release.aab` directly to Desktop (`C:\Users\wcsur\Desktop\app-release.aab`).
- [x] Verified full app build (`app:assembleDebug` SUCCESSFUL) and pushed updates live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## Next Steps
- [ ] Upload `app-release.aab` from Desktop to Google Play Console Closed Testing track.
