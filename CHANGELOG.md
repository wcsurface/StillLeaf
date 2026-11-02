# CHANGELOG.md - Project History

## [9.0.0] - User Pro Subscription Entitlement Persistence & Account Sync

### Fixed
- Fixed bug where upgraded Pro status for user `wcsurface@gmail.com` reset upon app restart.
- Added `updateUserSubscriptionStatus` in `UserSessionManager.kt` to persist Pro subscription tier per account ID (`sub_status_${userId}`).
- Updated `MainActivity.kt` and `BillingManager.kt` to sync restored Pro status on app launch.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [8.9.0] - Indoor/Outdoor Environment Selector & Required Location Validation

### Added
- Added Indoor / Outdoor Environment Toggle and required location field validation on `AddPlantScreen.kt` and `PlantDetailScreen.kt`.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
