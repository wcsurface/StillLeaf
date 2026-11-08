# CHANGELOG.md - Project History

## [52.0.0] - Daily Care Alarm Scheduling & Task Cycle Logic (v3.6, Build 27)

### Added
- Configured recurring morning 8:00 AM care notifications in `CareNotificationManager.kt` and `CareNotificationReceiver.kt` with `BOOT_COMPLETED` auto-rescheduling.
- Implemented task clearing on `CalendarScreen.kt`: Checking off a task clears it from `Today's Tasks` until its recommended care interval (e.g. 7 days) has elapsed.
- Implemented active cycle checkmark retention on `PlantDetailScreen.kt`: Care task cards stay checked green throughout their active care cycle interval and uncheck when due again.
- Delivered fresh signed release bundle `app-release.aab` for Version 3.6 (Build 27) to Desktop (`C:\Users\wcsur\Desktop\app-release.aab`).
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [51.0.0] - Application Version Bump (v3.5, Build 26)

### Added
- Bumped `versionCode = 26` (`versionName = "3.5"`).
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
