# CURRENT_STATUS.md - Project State Snapshot

## Current Phase
Phase 38: Security Card Removal, Legal Compliance Pages & Dynamic Copyright Footer Complete

## Completed Work
- [x] Initialized project memory & guidelines (`AGENTS.md`, `PROJECT_MEMORY.md`, `REQUIREMENTS.md`, `CHANGELOG.md`).
- [x] **Removed Security Card**: Removed the *"Security & Privacy / Cloudflare R2"* card from [SettingsScreen.kt](file:///G:/My%20Drive/APPS/StillLeaf/app/src/main/java/app/vercel/stillleaf/ui/screen/SettingsScreen.kt).
- [x] **Created Compliant Legal Screen ([LegalInfoScreen.kt](file:///G:/My%20Drive/APPS/StillLeaf/app/src/main/java/app/vercel/stillleaf/ui/screen/LegalInfoScreen.kt))**:
  - **Terms of Service**: Complete Google Play compliant terms covering Surface Apps ownership, botanical guidance disclaimers, Google Play subscription billing terms, and user responsibilities.
  - **Privacy Policy**: FTC & Google Play compliant policy covering account registration, Cloudflare R2 encrypted photo storage, zero selling of data, and deletion rights.
  - **FAQ**: Detailed answers covering 2-shot identification, Free vs. Pro limits ($1.99/mo, $14.99/yr, $29.99 lifetime), 90-day growth milestones, purchase restoration, and Surface Apps ownership.
- [x] **Footer Links & Dynamic Copyright ([StillLeafFooter.kt](file:///G:/My%20Drive/APPS/StillLeaf/app/src/main/java/app/vercel/stillleaf/ui/component/StillLeafFooter.kt))**:
  - Added clickable footer links (`Terms of Service` • `Privacy Policy` • `FAQ`) across Home, Add, Calendar, and Profile screens.
  - Added dynamic year changing copyright notice: `© ${Year.now().value} Surface Apps. All rights reserved.`.
- [x] Verified full app build (`app:assembleDebug` SUCCESSFUL) and pushed updates live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## Next Steps
- [ ] Continue build and testing mode.
