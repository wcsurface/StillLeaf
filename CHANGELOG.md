# CHANGELOG.md - Project History

## [7.7.0] - Front Camera Clearance & Bottom-Anchored Legal Footer

### Changed
- Increased top padding of `StillLeafTopBar.kt` to `52.dp` to clear the front-facing camera cutout on every screen.
- Updated `PlantListScreen.kt`, `AuthScreen.kt`, `AddPlantScreen.kt`, `CalendarScreen.kt`, and `SettingsScreen.kt` to anchor `StillLeafFooter` at the very bottom edge of the screen display.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [7.6.0] - Custom Leaf Notification Icon & Title Update

### Changed
- Added custom leaf emblem icon `ic_notification.png` to `res/drawable/`.
- Updated `CareNotificationReceiver.kt` to use `R.drawable.ic_notification` as the small notification bar icon and changed title to `"🌿 Plant Care"`.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
