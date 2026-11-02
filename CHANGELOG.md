# CHANGELOG.md - Project History

## [10.6.0] - Railway Backend Server Vision Proxy & Live AI Identification

### Added
- Created `identifyPlantFromDualBase64` server endpoint (`POST /api/plants/identify-base64`) on Railway backend.
- Updated `OpenAiVisionService.kt` in Android app to route dual-photo identification requests through Railway server.
- Executes OpenAI GPT-4o Vision server-side via Railway `OPENAI_API_KEY` environment variable.
- Enables live plant identification for all users without requiring a new Google Play App Bundle upload.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [10.5.0] - Photo Capture Slot Button Formatting & Version Bump (v1.4, Build 5)

### Fixed
- Fixed button label formatting and text padding on `AddPlantScreen.kt`.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
