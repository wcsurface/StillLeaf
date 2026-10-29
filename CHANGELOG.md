# CHANGELOG.md - Project History

## [3.8.0] - Plant Sanctuary Reset for Live Testing

### Added
- Completely cleared all legacy sample plant generator functions in `MainActivity.kt`.
- Sanctuary initializes with an empty plant list (`emptyList()`), ready for live testing with real photos analyzed via OpenAI GPT-4o Vision.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [3.7.0] - Live OpenAI GPT-4o Vision API Integration & Camera Crash Fix

### Fixed
- Fixed camera crash by adding runtime `CAMERA` permission request launcher (`RequestPermission()`) before launching camera preview.

### Added
- Built `OpenAiVisionService.kt` to send uploaded/captured plant images directly to OpenAI GPT-4o Vision API.
- Analyzes actual photos from camera or gallery and returns exact real species identification, certainty score, and tailored care requirements.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
