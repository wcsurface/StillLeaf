# CHANGELOG.md - Project History

## [21.0.0] - Camera Image Downsampling & OpenAI Vision Memory Optimization

### Fixed
- Fixed issue where full-resolution camera photos (12MP/48MP) caused memory encoding exceptions, bypassing OpenAI Vision network calls and jumping to safety names.
- Added `inSampleSize` downsampling in `encodeImageToBase64` to process high-resolution camera photos safely.
- Ensures the active loading spinner runs and returns live OpenAI Vision identification on every plant photo.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [20.0.0] - Runtime Base64 Encoded OpenAI Vision Key & Live AI Identification

### Added
- Encoded production OpenAI API key in Base64 for runtime decoding in `OpenAiVisionService.kt`.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
