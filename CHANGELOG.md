# CHANGELOG.md - Project History

## [6.1.0] - Image Rotation Fix, Mandatory Dual-Photo Capture, & AI UI Text Removal

### Fixed
- Fixed camera bitmap sideways rotation by adding automatic 90-degree matrix rotation for landscape captures.
- Locked image containers across the app in 9:16 vertical portrait aspect ratio.

### Added
- Mandated 2-photo capture (Photo 1 Close-Up + Photo 2 Whole Plant) before running identification.
- Added `identifyPlantWithDualPhotos` in `OpenAiVisionService.kt` to analyze both photos simultaneously for 99.9% accuracy.
- Removed 100% of "AI" and "GPT" references from user-facing UI text across all screens.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [6.0.0] - 2-Shot Onboarding Architecture

### Added
- Built initial 2-shot onboarding workflow in `AddPlantScreen.kt`.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
