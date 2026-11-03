# CHANGELOG.md - Project History

## [40.0.0] - Base64.URL_SAFE Decoding Flag Integration

### Fixed
- Updated `getActiveApiKey()` in `OpenAiVisionService.kt` to pass `Base64.URL_SAFE` flag to handle Base64 strings containing `_` or `-` characters.
- Resolves Base64 decoding exception, enabling live OpenAI Vision API calls on every plant photo taken.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [39.0.0] - Content Resolver 1-Read ByteArray Camera Encoding Stream Fix

### Fixed
- Fixed Content Resolver stream closure bug in `OpenAiVisionService.kt`.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
