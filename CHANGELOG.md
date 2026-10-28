# CHANGELOG.md - Project History

## [1.3.0] - Mockup Recreation & Lakki Reddy Typography

### Added
- Integrated **Lakki Reddy** downloadable font via `androidx.compose.ui.text.googlefonts` for the logo header and top navigation buttons.
- Recreated user's mockup homepage layout:
  - **STILL LEAF** logo text in Lakki Reddy + `"Healthy plants. Happier days."` slogan.
  - Floating top navigation pill container with **Home**, **Add**, **Calendar**, and **Settings** tabs with Lakki Reddy typography.
  - **My Plants** header + `All`, `Indoor`, `Outdoor` interactive filter chips.
  - 2-Column plant card grid matching mockup items (*Monstera Deliciosa*, *Snake Plant*, *Basil*, *Lavender*) with custom location badges and color-coded health status tags (*Healthy*, *Needs Water*, *Needs Fertilizer*).

## [1.2.0] - Main Plant Sanctuary Dashboard & Back Navigation

### Added
- Developed `PlantListScreen.kt` featuring a 2-column grid layout of user plants, capacity counters (Free 2/2 vs Pro Member badge), and an AI Camera scanner FAB button.
- Updated `PlantDetailScreen.kt` with a circular top-left back button (`←`) to navigate back to the main sanctuary dashboard seamlessly.
- Integrated `Crossfade` animation and Android `BackHandler` in `MainActivity.kt` to allow tapping any plant card to view its details and tapping back (or back gesture) to return to the plant gallery.

## [1.1.0] - Platform Configuration & GitHub Push

### Added
- Configured `.gitignore` to strictly exclude `.env`, `backend/.env`, and secret keys.
- Created `backend/.env` with provided OpenAI, Cloudflare R2 S3 (`9dada...`), JWT secret, and platform tokens.
- Created `backend/.env.example` safe configuration template.
- Built Fastify/Express server entry point (`backend/src/server.ts`), routes (`plantRoutes.ts`, `billingRoutes.ts`), `package.json`, and `tsconfig.json`.
- Created private GitHub repository `https://github.com/wcsurface/StillLeaf`.
- Pushed all project source files to `https://github.com/wcsurface/StillLeaf.git` (`main` branch).

## [1.0.0-alpha] - Initial Project Foundation

### Added
- Created permanent project memory system (`AGENTS.md`, `PROJECT_MEMORY.md`, `CURRENT_STATUS.md`, `REQUIREMENTS.md`, `CHANGELOG.md`).
- Designed Prisma PostgreSQL database schema for Users, Plants, Photos, Care Logs, and Shared Cards.
- Implemented Node.js/TypeScript backend Plant Vision Service with Cloudflare R2 S3 pre-signed upload URL generation and OpenAI GPT-4o Vision analysis (`plantVisionService.ts`).
- Created Zen-inspired Material 3 theme (`StillLeafTheme.kt`, `Color.kt`, `Type.kt`, `Shape.kt`).
- Developed `PlantDetailScreen.kt` with care task actions, haptic feedback, and 3-month horizontal growth gallery.
- Built `BillingManager.kt` using Google Play Billing Library v7+ for `still_leaf_unlimited_pro_499` upgrade flow and entitlement tracking.
- Created `BannerAdView.kt` Compose component with billing state check to render AdMob banner ads conditionally.
