# CHANGELOG.md - Project History

## [5.0.0] - Production Architecture Refactoring: Play Billing 7+, Cloudflare R2, & Zen Design System

### Added
- **AdMob Elimination**: Completely removed all banner ads across the app.
- **Client-Side Image Downsampling**: Built `ImageCompressor.kt` (max 1024px @ 80% JPEG quality).
- **Google Play Billing 7+ Multi-Tier System**: Built `BillingManager.kt` supporting $1.99/mo, $14.99/yr, and $29.99 lifetime unlock.
- **Zen Affiliate Recommendation Component**: Built `ZenAffiliateCard.kt` for context-aware soil, grow light, and fertilizer recommendations.
- **Paywall Bottom Sheet**: Built `PaywallBottomSheet.kt` modal sheet with tier selection and purchase restoration.
- **Cloudflare R2 & GPT-4o-mini Vision Backend Services**: Created `r2Service.ts` and `plantVisionService.ts` with structured JSON outputs and quota verification.
- **Prisma PostgreSQL Schema Upgrade**: Updated `schema.prisma` with `SubscriptionTier`, `AffiliateProduct`, and `TaskType`.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [4.1.0] - Captured & Gallery Photo Persistence Fix

### Fixed
- Fixed bug in `AddPlantScreen.kt` where camera-captured photos fell back to default sample images.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
