# CHANGELOG.md - Project History

## [2.0.0] - Complete Still Leaf Application Redesign

### Added
- Implemented full brand color system: Warm Ivory (`#F7F4E9`), Soft Cream (`#F0EDDF`), Pure Cream (`#FBF9F1`), Primary Olive (`#596B45`), Deep Botanical Green (`#34452F`), Sage Green (`#788765`), Soft Moss (`#A3AE8E`), Pale Sage (`#E5E9DA`), Healthy Green (`#587C4B`), Watering Amber (`#D88935`), Attention Terracotta (`#B96D4D`).
- Combined signature Lakki Reddy font for brand header and navigation labels with clean modern sans-serif typography.
- Rebuilt Home Screen (`PlantListScreen.kt`) with brand header, tagline, floating navigation pill container (**Home**, **Add**, **Calendar**, **Settings**), `All`/`Indoor`/`Outdoor` filter pills, and 2-column botanical cards with health status indicators.
- Created Care & Calendar Screen (`CalendarScreen.kt`) with 7-day horizontal date selector, Primary Olive selected date pill, progress counter pill (`3/5`), task cards list with thumbnails, locations, and interactive circular completion checkbox controls, and wide "View Full Calendar" pill button.
- Created Identify & Add Plant Screen (`AddPlantScreen.kt`) with camera frame, AI Vision analysis state, plant result details, location selector, and "Save to Sanctuary" action.
- Created Settings Screen (`SettingsScreen.kt`) with Pro upgrade banner ($4.99), notification toggles, and security overview.
- Updated Plant Detail Screen (`PlantDetailScreen.kt`) with back button (`←`), environment chips, interactive task cards with haptic feedback, and 3-month growth gallery with comparison modal.
- Verified build compatibility with `gradle_build("app:assembleDebug")` and pushed all changes live to GitHub (`main` branch).
