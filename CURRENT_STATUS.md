# CURRENT_STATUS.md - Project State Snapshot

## Current Phase
Phase 12: Secure Authentication System (Email/Password & Google Sign-In) Complete

## Completed Work
- [x] Initialized project memory & guidelines (`AGENTS.md`, `PROJECT_MEMORY.md`, `REQUIREMENTS.md`, `CHANGELOG.md`).
- [x] Updated Prisma PostgreSQL Schema ([schema.prisma](file:///G:/My%20Drive/APPS/StillLeaf/backend/prisma/schema.prisma)):
  - Added `passwordHash`, `displayName`, `avatarUrl`, and `authProvider` (`EMAIL` | `GOOGLE`) fields to `User` model.
- [x] Built Backend Authentication Service ([authRoutes.ts](file:///G:/My%20Drive/APPS/StillLeaf/backend/src/routes/authRoutes.ts)):
  - `POST /api/auth/register`: Hashes passwords securely with `bcryptjs` and returns signed 30-day JWT session tokens.
  - `POST /api/auth/login`: Compares bcrypt password hashes and returns signed JWT session tokens.
  - `POST /api/auth/google`: Authenticates/upserts users via Google OAuth credentials & returns signed JWT session tokens.
  - `GET /api/auth/me`: Verifies Bearer JWT tokens and returns current user profile.
- [x] Implemented Native Android Auth Screen ([AuthScreen.kt](file:///G:/My%20Drive/APPS/StillLeaf/app/src/main/java/app/vercel/stillleaf/ui/screen/AuthScreen.kt)):
  - Warm Ivory (`#F7F4E9`) brand background with leaf symbol and **STILL LEAF** logo in Lakki Reddy font.
  - **Sign In** | **Create Account** pill selector.
  - Email Address & Password input fields with toggle visibility icon.
  - **"Sign In with Email"** / **"Create Account"** primary button.
  - **"Continue with Google"** social button with official Google 'G' icon badge.
  - **"Continue as Guest Preview"** option for instant preview access.
- [x] Updated [SettingsScreen.kt](file:///G:/My%20Drive/APPS/StillLeaf/app/src/main/java/app/vercel/stillleaf/ui/screen/SettingsScreen.kt) with user account card and **Sign Out** action.
- [x] Verified full app build (`app:assembleDebug` SUCCESSFUL) and pushed updates live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## Next Steps
- [ ] Connect CameraX live preview & Railway server endpoints for live camera scan uploads.
