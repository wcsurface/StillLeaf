# CHANGELOG.md - Project History

## [10.7.0] - Startup Crash Resolution & Version Bump (v1.5, Build 6)

### Fixed
- Restored `installSplashScreen()` invocation before `super.onCreate(savedInstanceState)` in `MainActivity.kt` to resolve `Theme.SplashScreen` startup theme exception.
- Added defensive `try-catch` error boundaries around all startup initializers (`BackgroundMusicManager`, `BillingManager`, `UserSessionManager`).
- Bumped `versionCode = 6` (`versionName = "1.5"`).
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [10.6.0] - Railway Backend Server Vision Proxy & Live AI Identification

### Added
- Created `identifyPlantFromDualBase64` server endpoint on Railway backend.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
