# CHANGELOG.md - Project History

## [3.6.0] - Native Camera & Gallery Launchers

### Added
- Updated `AddPlantScreen.kt` button label to **"Take Photo"**.
- Integrated native Android `TakePicturePreview` camera launcher and `PickVisualMedia` gallery picker launcher.
- Displays selected or captured plant photo live in preview box and triggers OpenAI Vision AI identification.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [3.5.0] - Cleared Seed & Sample Data for Functional Testing

### Added
- Cleared initial sample plant list in `MainActivity.kt` so the app starts with a clean slate (`plantList = emptyList()`).
- Designed a serene empty state card in `PlantListScreen.kt` with a direct **"+ Add Your First Plant"** button launching the AI identification flow.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
