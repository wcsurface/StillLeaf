# CHANGELOG.md - Project History

## [3.5.0] - Cleared Seed & Sample Data for Functional Testing

### Added
- Cleared initial sample plant list in `MainActivity.kt` so the app starts with a clean slate (`plantList = emptyList()`).
- Designed a serene empty state card in `PlantListScreen.kt` with a direct **"+ Add Your First Plant"** button launching the AI identification flow.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [3.4.0] - Banner Ad Container Height Fix

### Fixed
- Replaced hardcoded `50.dp` height constraint on `BannerAdView.kt` with `wrapContentHeight()` and vertical padding.
- Fixed clipping of the `Test Ad` badge and adaptive ad text edges.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
