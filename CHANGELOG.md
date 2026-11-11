# CHANGELOG.md - Project History

## [64.0.0] - Doze Mode Idle Bypass Notification Alarm (v4.9, Build 40)

### Fixed
- Upgraded `scheduleDailyMorningAlarm` in `CareNotificationManager.kt` to use `AlarmManager.setAndAllowWhileIdle()` on Android M+, bypassing Android Doze Mode sleep throttling.
- Updated `CareNotificationReceiver.kt` to auto-reschedule the next morning's 8:00 AM alarm every day.
- Delivered fresh signed release bundle `app-release.aab` for Version 4.9 (Build 40) to Desktop (`C:\Users\wcsur\Desktop\app-release.aab`).
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [63.0.0] - Notification Permission Prompt & Verification (v4.8, Build 39)

### Added
- Added Android 13+ `POST_NOTIFICATIONS` runtime permission request.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
