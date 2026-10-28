# REQUIREMENTS.md - Product Requirements Specification

## 1. Product Vision & Target Audience
**Still Leaf - Whole Plant Care** is a Zen-inspired botanical care, tracking, and growth journal platform designed for plant enthusiasts and indoor gardeners who seek a peaceful, distraction-free environment to care for their plants and celebrate visual growth over time.

---

## 2. Core Functional Requirements

### 2.1 AI Plant Identification Pipeline
- User captures or selects a plant photo via CameraX / Gallery.
- Android app requests a pre-signed Cloudflare R2 upload URL from backend (`POST /api/plants/upload-url`).
- Image compressed to JPEG and uploaded directly to R2 bucket.
- Client calls backend identification endpoint (`POST /api/plants/identify`).
- Backend uses OpenAI Vision (GPT-4o / GPT-4o-mini) server-side with structured JSON output:
  - Botanical name and common name
  - Confidence score & initial health check summary
  - Environment requirements (sunlight, ideal temp, ideal humidity)
  - Recommended care intervals (water days, fertilize days, rotate days, prune/trim days)
- Automatic profile creation and local state synchronization.

### 2.2 Care Scheduling & Smart Notifications
- Automated schedule based on task intervals and last completed care logs (`WATER`, `FEED`, `ROTATE`, `TRIM`).
- Backend queue worker (BullMQ + Redis) processes due notifications and dispatches via Firebase Cloud Messaging (FCM).
- Morning daily reminders (e.g., *"Your Monstera could use a drink today"*).
- 90-day growth check-in reminders (*"Quarterly Growth Milestone: Photograph your [Plant Name] today to celebrate its growth journey"*).

### 2.3 Quarterly Visual Growth Journal
- Plant detail screen provides a horizontal visual timeline comparing milestones (Day 0, Day 90, Day 180, etc.).
- Side-by-side transition modal comparing any two growth snapshots.

### 2.4 Dynamic Social Media Sharing
- Web endpoint `/share/[slug]` hosted on Vercel generating OpenGraph meta images containing plant photo, botanical name, care age, and health milestone card.
- Android App Links (`assetlinks.json`) enabling deep-linking directly into native plant detail views.

### 2.5 Monetization & Ad Strategy
- **Free Tier**:
  - Maximum of **2 plants** per account.
  - Non-intrusive adaptive banner ad displayed at bottom of main screen using Google Mobile Ads (AdMob).
- **Pro Tier** ($4.99 One-Time Purchase / Lifetime Access):
  - In-app purchase product ID: `still_leaf_unlimited_pro_499`.
  - Server-side Play Developer API purchase verification.
  - Unlimited plant profiles (`max_plants = ∞`).
  - Automatic suppression of all banner ads.

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
