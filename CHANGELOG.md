# CHANGELOG.md - Project History

## [4.1.0] - Captured & Gallery Photo Persistence Fix

### Fixed
- Fixed bug in `AddPlantScreen.kt` where camera-captured photos fell back to default sample images.
- Added `saveBitmapToCacheUri` helper to convert camera bitmaps into local image file URIs (`captured_plant_1234.jpg`).
- The plant's profile header, grid cards, and quarterly growth journal now display the user's actual photo.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [4.0.0] - Pro Entitlement Persistence & Restore Purchases Feature

### Added
- Added an explicit **"Restore Purchases"** button in `SettingsScreen.kt` that queries Google Play Billing for active in-app purchases.
- Verified dual entitlement recovery architecture.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
