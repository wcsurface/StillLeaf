# REQUIREMENTS.md - Product Requirements Specification

## 1. Product Vision & Target Audience
**Still Leaf - Whole Plant Care** is a Zen-inspired botanical care, tracking, and growth journal platform designed for plant enthusiasts and indoor gardeners who seek a peaceful, distraction-free environment to care for their plants and celebrate visual growth over time.

---

## 2. Core Functional Requirements

### 2.1 AI Plant Identification Pipeline
- User captures or selects a plant photo via CameraX / Gallery.
- Android app downsamples image using `ImageCompressor.kt` (max 1024x1024 @ 80% JPEG quality).
- Client requests a pre-signed Cloudflare R2 upload URL from backend (`POST /api/plants/upload-url`).
- Image uploaded directly to Cloudflare R2 bucket `still-leaf-plants`.
- Client calls backend identification endpoint (`POST /api/plants/identify`).
- Backend uses OpenAI GPT-4o-mini Vision server-side with structured JSON output:
  - Botanical name and common name
  - Confidence score & initial health check summary
  - Environment requirements (sunlight, ideal temp, ideal humidity)
  - Recommended care intervals (water days, fertilize days, rotate days, prune/trim days)
  - Category matching for Zen Botanical Affiliate Cards (soils, grow lights, moisture meters, organic fertilizers)
- Automatic profile creation and local state synchronization.

### 2.2 Care Scheduling & Smart Notifications
- Automated schedule based on task intervals and last completed care logs (`WATER`, `FEED`, `ROTATE`, `TRIM`).
- Backend queue worker (BullMQ + Redis) processes due notifications and dispatches via Firebase Cloud Messaging (FCM).
- Morning daily reminders (e.g., *"Your Monstera could use a drink today"*).
- 90-day growth check-in reminders (*"Quarterly Growth Milestone: Photograph your [Plant Name] today to celebrate its growth journey"*).

### 2.3 Quarterly Visual Growth Journal
- Plant detail screen provides a horizontal visual timeline comparing milestones (Day 0, Day 90, Day 180, etc.).
- Side-by-side transition modal comparing any two growth snapshots.

### 2.4 Dynamic Social Media Sharing & Deep Links
- Web endpoint `/share/[slug]` hosted on Vercel generating OpenGraph meta images containing plant photo, botanical name, care age, and health milestone card.
- Android App Links (`assetlinks.json`) enabling deep-linking directly into native plant detail views.

### 2.5 Multi-Tier Monetization & Zen Affiliate Recommendation Model
- **Complete Elimination of AdMob Banners**: AdMob banners are 100% removed to preserve the Zen aesthetic.
- **Free Tier**:
  - Up to **3 plants** max per account.
  - 3 free AI identifications total.
  - Full care logging and reminder tracking.
- **Pro Tier** ($1.99/mo, $14.99/yr, or $29.99 Lifetime Access):
  - In-app purchase product IDs:
    - `still_leaf_pro_sub` (Base plans: `monthly-199` and `annual-1499`).
    - `still_leaf_lifetime_2999` (Lifetime Unlock).
  - Server-side Play Developer API purchase token verification.
  - Unlimited plant profiles (`max_plants = ∞`).
  - Unlimited AI Vision plant identifications.
  - High-res growth milestone exports.
- **Zen Botanical Affiliate Cards**:
  - Context-aware recommendations for potting mixes, LED grow lights, moisture meters, and organic plant foods embedded directly into plant detail screens.
  - Amazon Associates Tag ID: `stillleaf-20`.

---

## 3. Visual & UI/UX Requirements
- **Aesthetic**: "Peaceful Digital Greenhouse" - minimal, organic, calming tones.
- **Color Tokens**:
  - Primary Sage: `#6B8E7B`, Dark Forest Sage: `#2C3E35`
  - Background: Creamy Off-White (`#F9F8F5`), Surface: `#FFFFFF`, Surface Variant: `#F2EFE9`
  - Muted Olive: `#8F9779`, Warm Earth Accent: `#D4A373`, Soft Terracotta: `#C87D55`, Stroke: `#E5E0D8`
- **Shape System**: `16.dp` to `24.dp` rounded corners on cards, soft drop shadows.
- **Micro-interactions**: Gentle haptic feedback on care task completion, fluid page transitions.

---

## 4. Security & Performance
- Zero hardcoded secrets, API keys, or database credentials on mobile client.
- Strict server-side verification for IAP receipts and OpenAI calls.
- Pre-signed S3/R2 upload URLs to prevent backend server egress bottlenecks.
- JWT authentication for client API calls.
