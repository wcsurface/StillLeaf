# CHANGELOG.md - Project History

## [11.0.0] - Direct Launcher Icon Enforcement & Version Bump (v1.9, Build 10)

### Fixed
- Removed adaptive XML override folder (`mipmap-anydpi`) that caused Moto Launcher to render the Android bug placeholder icon.
- Forced home screen launcher to render direct high-density PNG icon assets (`ic_launcher.png`).
- Refined `ic_splash_logo.xml` vector paths for the white botanical leaf emblem.
- Bumped `versionCode = 10` (`versionName = "1.9"`).
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [10.9.0] - Android 12+ SplashScreen Bitmap Casting Crash Resolution (v1.8, Build 9)

### Fixed
- Fixed startup crash caused by `windowSplashScreenAnimatedIcon` pointing to a PNG file.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
