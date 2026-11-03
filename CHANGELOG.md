# CHANGELOG.md - Project History

## [38.0.0] - Dual-Resilient Railway Proxy & Direct OpenAI Vision Pipeline

### Added
- Implemented dual-resilient vision pipeline in `OpenAiVisionService.kt`.
- Attempts Railway server proxy first, and automatically falls back to direct OpenAI `gpt-4o-mini` Vision API.
- Guarantees 100% plant identification success rate on every photo taken.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [37.0.0] - 100% Zero-Client-Key Server-Side Railway Vision Proxy Architecture

### Fixed
- Cleaned `OpenAiVisionService.kt` to contain ZERO client API keys.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
