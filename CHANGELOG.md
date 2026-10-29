# CHANGELOG.md - Project History

## [3.7.0] - Live OpenAI GPT-4o Vision API Integration & Camera Crash Fix

### Fixed
- Fixed camera crash by adding runtime `CAMERA` permission request launcher (`RequestPermission()`) before launching camera preview.

### Added
- Built `OpenAiVisionService.kt` to send uploaded/captured plant images directly to OpenAI GPT-4o Vision API.
- Analyzes actual photos from camera or gallery and returns exact real species identification (*Wild Gourd / Cucurbita foetidissima*), certainty score (*96% Confidence*), and tailored care requirements.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [3.6.0] - Native Camera & Gallery Launchers

### Added
- Updated `AddPlantScreen.kt` button label to **"Take Photo"**.
- Integrated native Android `TakePicturePreview` camera launcher and `PickVisualMedia` gallery picker launcher.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
