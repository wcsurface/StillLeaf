# CHANGELOG.md - Project History

## [4.0.0] - Pro Entitlement Persistence & Restore Purchases Feature

### Added
- Added an explicit **"Restore Purchases"** button in `SettingsScreen.kt` that queries Google Play Billing for active in-app purchases.
- Verified dual entitlement recovery architecture: purchases are bound both to the user's Google Play Store account and their Still Leaf PostgreSQL user account on Railway.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [3.9.0] - AI Result Card Clean-up

### Removed
- Removed the `"OpenAI GPT-4o Verified"` text label from the plant identification result card in `AddPlantScreen.kt`.
- Updated scanning state subtext to *"Identifying botanical features..."*.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
