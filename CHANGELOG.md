# CHANGELOG.md - Project History

## [61.0.0] - EXIF Rotation Auto-Correction (v4.6, Build 37)

### Fixed
- Implemented `getExifRotationDegrees` in `AddPlantScreen.kt` using `ExifInterface` to auto-rotate sideways camera/gallery photos into upright portrait orientation.
- Restored original clean card height on Home screen (`PlantListScreen.kt`).
- Installed directly onto connected wireless debug device via ADB (`app:installDebug`).
- Delivered fresh signed release bundle `app-release.aab` for Version 4.6 (Build 37) to Desktop (`C:\Users\wcsur\Desktop\app-release.aab`).
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [60.0.0] - Strict Zero-Fallback User Photo Enforcement (v4.5, Build 36)

### Fixed
- Completely removed all stock fallback URLs from `ImageModelUtils.kt`, `AddPlantScreen.kt`, and `PlantPersistenceManager.kt`.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
