# CHANGELOG.md - Project History

## [1.1.0] - Platform Configuration & GitHub Push

### Added
- Configured `.gitignore` to strictly exclude `.env`, `backend/.env`, and secret keys.
- Created `backend/.env` with provided OpenAI, Cloudflare R2 S3 (`9dada...`), JWT secret, and platform tokens.
- Created `backend/.env.example` safe configuration template.
- Built Fastify/Express server entry point (`backend/src/server.ts`), routes (`plantRoutes.ts`, `billingRoutes.ts`), `package.json`, and `tsconfig.json`.
- Created private GitHub repository `https://github.com/wcsurface/StillLeaf`.
- Pushed all 115 objects and 70 project source files to `https://github.com/wcsurface/StillLeaf.git` (`main` branch).

## [1.0.0-alpha] - Initial Project Foundation

### Added
- Created permanent project memory system (`AGENTS.md`, `PROJECT_MEMORY.md`, `CURRENT_STATUS.md`, `REQUIREMENTS.md`, `CHANGELOG.md`).
- Designed Prisma PostgreSQL database schema for Users, Plants, Photos, Care Logs, and Shared Cards.
- Implemented Node.js/TypeScript backend Plant Vision Service with Cloudflare R2 S3 pre-signed upload URL generation and OpenAI GPT-4o Vision analysis (`plantVisionService.ts`).
- Created Zen-inspired Material 3 theme (`StillLeafTheme.kt`, `Color.kt`, `Type.kt`, `Shape.kt`).
- Developed `PlantDetailScreen.kt` with care task actions, haptic feedback, and 3-month horizontal growth gallery.
- Built `BillingManager.kt` using Google Play Billing Library v7+ for `still_leaf_unlimited_pro_499` upgrade flow and entitlement tracking.
- Created `BannerAdView.kt` Compose component with billing state check to render AdMob banner ads conditionally.
