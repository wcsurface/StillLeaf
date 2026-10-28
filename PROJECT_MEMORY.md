# PROJECT_MEMORY.md - Technical Knowledge Base & System Architecture

## 1. System Topology & Architecture

```
[ Android Client (Kotlin / Compose / M3) ]
       │
       ├────────► [ Edge API Gateway (Vercel / Next.js) ]
       │                │ (Auth, OG Image Gen, App Links)
       │                ▼
       ├────────► [ Core Services (Railway / Fastify / Node.js) ]
       │                │
       │                ├──► [ PostgreSQL (Prisma ORM) ]
       │                ├──► [ Redis + BullMQ (Notification Crons) ]
       │                └──► [ OpenAI Vision API (GPT-4o) ]
       │
       └────────► [ Cloudflare R2 Object Storage ] (Direct S3 Pre-signed Uploads)
```

---

## 2. Technology Stack

### Android Client
- **Language & Framework**: Kotlin 2.2+, Jetpack Compose, Material 3
- **Architecture**: Clean Architecture + MVI/MVVM
- **Dependencies**:
  - `androidx.compose.material3`
  - `androidx.camera:camera-compose` & `camera-lifecycle` (CameraX)
  - `io.coil-kt.coil3:coil-compose`
  - `com.android.billingclient:billing-ktx` (v7+)
  - `com.google.android.gms:play-services-ads` (AdMob)
  - `com.google.firebase:firebase-messaging` (FCM)

### Backend Services
- **Framework**: Node.js / Express / Fastify / TypeScript
- **Database**: PostgreSQL with Prisma ORM
- **Queue/Scheduler**: BullMQ + Redis
- **Storage**: Cloudflare R2 (S3-compatible via `@aws-sdk/client-s3`)
- **AI Engine**: `@openai/openai` (GPT-4o Vision server-side execution)

---

## 3. Database Schema (Prisma PostgreSQL)
- `User`: `id`, `email`, `fcmToken`, `subscriptionStatus` (`FREE` | `PRO`), `createdAt`, `updatedAt`
- `Plant`: `id`, `userId`, `nickname`, `botanicalName`, `commonName`, `careRequirements` (JSON), `createdAt`, `updatedAt`
- `PlantPhoto`: `id`, `plantId`, `photoUrl`, `takenAt`, `note`, `isQuarterlyMilestone`
- `CareLog`: `id`, `plantId`, `taskType` (`WATER`, `FEED`, `ROTATE`, `TRIM`, `PHOTO`), `completedAt`
- `SharedCard`: `id`, `plantId`, `slug`, `ogImageUrl`, `isPublic`, `views`, `createdAt`

---

## 4. UI/UX Design System Tokens
- **Background**: `#F9F8F5` (Creamy Off-White)
- **Surface**: `#FFFFFF`
- **Surface Variant**: `#F2EFE9`
- **Primary Sage**: `#6B8E7B`
- **Dark Sage / Forest**: `#2C3E35`
- **Muted Olive**: `#8F9779`
- **Earth Warm Accent**: `#D4A373`
- **Terracotta Soft**: `#C87D55`
- **Border / Stroke**: `#E5E0D8`
- **Card Corners**: `16.dp`–`24.dp`

---

## 5. Security & Hardening Strategy
1. **API Key Isolation**: Client contains no OpenAI keys, database connection strings, or service tokens.
2. **Pre-signed Uploads**: R2 upload URLs are generated on backend with strict 5-minute expiry and mime-type restriction (`image/jpeg`).
3. **Receipt Validation**: Google Play IAP purchase tokens are verified with Google Play Developer API before setting `subscriptionStatus = PRO`.
4. **Rate Limiting**: API Gateway limits upload and identification endpoints (e.g. max 10 requests/min per IP/user).
