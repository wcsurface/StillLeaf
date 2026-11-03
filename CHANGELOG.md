# CHANGELOG.md - Project History

## [36.0.0] - Synchronous Loading Spinner State Reset

### Fixed
- Fixed UI state bug in `AddPlantScreen.kt` where previous identification cards were retained when capturing new photos.
- Synchronously resets `aiResult = null` and sets `isAnalyzing = true` in `runIdentification()` to guarantee the animated loading spinner displays instantly.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [35.0.0] - Railway TypeScript Build Error Resolution

### Fixed
- Added exported alias `identifyPlantFromImageKey` in `plantVisionService.ts`.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
