# CURRENT_STATUS.md - Project State Snapshot

## Current Phase
Phase 117: EXIF Rotation Auto-Correction & Version Bump (v4.6, Build 37) Complete

## Completed Work
- [x] Initialized project memory & guidelines (`AGENTS.md`, `PROJECT_MEMORY.md`, `REQUIREMENTS.md`, `CHANGELOG.md`).
- [x] **EXIF Rotation Auto-Correction ([AddPlantScreen.kt](file:///G:/My%20Drive/APPS/StillLeaf/app/src/main/java/app/vercel/stillleaf/ui/screen/AddPlantScreen.kt))**:
  - Found the exact reason why camera photos appeared sideways: Android phone cameras (Motorola, Samsung, Pixel) write EXIF orientation tags (`ORIENTATION_ROTATE_90`, `270`, etc.) while raw JPEG bitmaps decode in landscape.
  - Implemented `getExifRotationDegrees`: Reads `ExifInterface` from camera/gallery photo streams and automatically rotates the bitmap by `90°`, `180°`, or `270°` into upright portrait orientation.
  - Enforced `height > width` aspect ratio validation so all captured plant photos render **100% upright in vertical portrait format**.
- [x] **Pushed To Connected Wireless Debug Device**: Built and installed directly onto your wireless debug device via ADB (`app:installDebug`).
- [x] **Version Bump & Signed Release Bundle**:
  - Bumped `versionCode = 37` (`versionName = "4.6"`).
  - Delivered fresh signed release bundle `app-release.aab` directly to Desktop (`C:\Users\wcsur\Desktop\app-release.aab`).
- [x] Verified full app build (`app:assembleDebug` SUCCESSFUL) and pushed updates live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## Next Steps
- [ ] Upload `app-release.aab` from Desktop to Google Play Console Closed Testing track.
