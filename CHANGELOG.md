# CHANGELOG.md - Project History

## [3.2.0] - Mandatory Authentication Gate Enforcement

### Added
- Locked main sanctuary application content behind a mandatory login gate in `MainActivity.kt`.
- Removed guest preview bypass option from `AuthScreen.kt` so users must sign in or register via Email/Password or Continue with Google before entering the app.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [3.1.0] - Authentication Entry Points & Direct Login Navigation

### Added
- Added a primary **"Sign In"** button to the top-right header of `PlantListScreen.kt` when in guest session mode.
- Added a **"Sign In or Register Account"** action button to the account card in `SettingsScreen.kt`.
- Clicking either button immediately launches `AuthScreen.kt` for email/password login or Google Sign-In.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
