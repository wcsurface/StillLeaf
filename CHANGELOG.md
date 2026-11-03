# CHANGELOG.md - Project History

## [26.0.0] - Strict Per-User Subscription Status Tracking

### Fixed
- Cleaned `UserSessionManager.kt` to remove all hardcoded developer/test subscription overrides.
- Enforced strict per-user account subscription status tracking (`sub_status_${userId}`) bound to user ID in local storage and Railway PostgreSQL.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [25.0.0] - Version 2.9 (Build 20) Signed Release App Bundle (.aab)

### Added
- Compiled signed production release bundle `app-release.aab` (Version 2.9, Build 20) and delivered to Desktop (`C:\Users\wcsur\Desktop\app-release.aab`).
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
