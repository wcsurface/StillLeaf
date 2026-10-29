# CHANGELOG.md - Project History

## [3.1.0] - Authentication Entry Points & Direct Login Navigation

### Added
- Added a primary **"Sign In"** button to the top-right header of `PlantListScreen.kt` when in guest session mode.
- Added a **"Sign In or Register Account"** action button to the account card in `SettingsScreen.kt`.
- Clicking either button immediately launches `AuthScreen.kt` for email/password login or Google Sign-In.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [3.0.0] - Database-Backed Secure Authentication (Email/Password & Google Sign-In)

### Added
- Added `passwordHash`, `displayName`, `avatarUrl`, and `authProvider` fields to Prisma PostgreSQL `User` schema.
- Built backend authentication routes in `authRoutes.ts` with bcrypt password hashing, JWT session token generation, and Google OAuth endpoints.
- Implemented native Android `AuthScreen.kt` featuring Sign In / Create Account tabs, email/password fields, official **"Continue with Google"** social button, and guest preview access.
- Integrated session management in `MainActivity.kt` and account profile card with **Sign Out** button in `SettingsScreen.kt`.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
