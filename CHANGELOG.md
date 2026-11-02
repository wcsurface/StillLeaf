# CHANGELOG.md - Project History

## [16.0.0] - Moto Launcher Database Cache Invalidation (`ic_still_leaf_launch_v2`)

### Fixed
- Fixed Moto Launcher database caching issue where the old `STILL LEAF < PRO >` icon was retained.
- Renamed launcher icon resource to `@mipmap/ic_still_leaf_launch_v2` in `AndroidManifest.xml` across all density folders.
- Forced Moto Launcher (`com.motorola.launcher3`) to purge cached icon bitmaps and display exact flat muted sage green leaf emblem icon.
- Bumped `versionCode = 14` (`versionName = "2.3"`).
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [15.0.0] - Exact Muted Sage Green Background (#859375) Icon & Matching Splash Screen

### Fixed
- Updated `colors.xml` with exact background color `#859375`.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
