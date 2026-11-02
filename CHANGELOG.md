# CHANGELOG.md - Project History

## [22.0.0] - Universal Theme Inflation Compatibility (v2.7, Build 18)

### Fixed
- Fixed launch crash on release builds by setting `Theme.StillLeaf` parent to `Theme.AppCompat.Light.NoActionBar` in `themes.xml`.
- Added `androidx.appcompat:appcompat` dependency to support `postSplashScreenTheme` across all phone manufacturers.
- Compiled production signed release bundle `app-release.aab` and delivered to Desktop (`C:\Users\wcsur\Desktop\app-release.aab`).
- Bumped `versionCode = 18` (`versionName = "2.7"`).
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [21.0.0] - Camera Image Downsampling & OpenAI Vision Memory Optimization

### Fixed
- Fixed camera image encoding memory limits during plant identification.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
