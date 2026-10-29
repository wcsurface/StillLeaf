# CHANGELOG.md - Project History

## [3.0.0] - Database-Backed Secure Authentication (Email/Password & Google Sign-In)

### Added
- Added `passwordHash`, `displayName`, `avatarUrl`, and `authProvider` fields to Prisma PostgreSQL `User` schema.
- Built backend authentication routes in `authRoutes.ts` with bcrypt password hashing, JWT session token generation, and Google OAuth endpoints.
- Implemented native Android `AuthScreen.kt` featuring Sign In / Create Account tabs, email/password fields, official **"Continue with Google"** social button, and guest preview access.
- Integrated session management in `MainActivity.kt` and account profile card with **Sign Out** button in `SettingsScreen.kt`.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [2.6.0] - AI Plant Health Diagnosis & Calendar Care Integration

### Added
- Added **"Diagnose Plant Health"** button above Plant Management section in `PlantDetailScreen.kt`.
- Built `DiagnosePlantDialog` enabling users to type issue symptoms and upload/attach photos for OpenAI Vision analysis.
- Generates structured diagnosis titles, root cause explanations, certainty scores, and step-by-step treatment cures.
- Added calendar tracking option (*"Add Special Care Treatment Task to Calendar"*) that schedules long-term recovery tasks on `CalendarScreen.kt`.
- Added active diagnosis summary card on the plant profile.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
