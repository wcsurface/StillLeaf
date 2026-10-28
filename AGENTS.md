# AGENTS.md - Development Operating Procedures for Still Leaf

This document defines the mandatory operating rules and instructions for any AI agent or software engineer working on **Still Leaf - Whole Plant Care**.

---

## 1. Context Recovery & Project Memory

* **Never rely on conversation history alone.** Before taking action, always inspect:
  - `AGENTS.md` (this file)
  - `CURRENT_STATUS.md` (current snapshot and next tasks)
  - `PROJECT_MEMORY.md` (architecture, schema, design system, decisions)
  - `REQUIREMENTS.md` (agreed product specifications)
  - `CHANGELOG.md` (historical records)
* Do not re-ask the user questions that are already answered in `REQUIREMENTS.md` or `PROJECT_MEMORY.md`.
* When starting a new development session, read `CURRENT_STATUS.md` first.

---

## 2. Code Inspection & Preservation Rules

* **Inspect Before Changing**: Always read and analyze existing files before modifying them. Do not assume file structure or contents.
* **Surgical & Targeted Edits**: Prefer precise updates (`replace_file_content` / `multi_replace_file_content`) over replacing entire files, unless writing a new file.
* **Preserve Functionality**: Do not delete existing working features, refactor unrelated modules, or break navigation unless explicitly instructed.
* **No Speculative Rewrites**: Do not replace custom UI components or real logic with generic placeholders or mock stubs.

---

## 3. Architecture & Code Quality Standards

* **Android Architecture**: Clean Architecture with MVI/MVVM, Jetpack Compose, Material 3, Hilt Dependency Injection, Coroutines/Flow, CameraX, and Google Play Billing Library.
* **UI/UX Aesthetics**: Maintain the **Zen Design System** ("Peaceful Digital Greenhouse").
  - Palette: Creamy Off-White (`#F9F8F5`), Primary Sage (`#6B8E7B`), Dark Sage (`#2C3E35`), Muted Olive (`#8F9779`), Terracotta (`#C87D55`), Warm Earth (`#D4A373`).
  - Cards: `16.dp`–`24.dp` rounded corners, soft elevation, fluid transitions, gentle haptic feedback on completion.
* **Backend Architecture**: Node.js / TypeScript (Fastify / Express / Next.js) + Prisma ORM + PostgreSQL + Redis / BullMQ + Cloudflare R2 + OpenAI Vision.
* **Security Rules**:
  - NEVER hardcode API keys, database credentials, or secret tokens in client code or repositories.
  - Server-side execution only for OpenAI Vision and Play Developer API verification.
  - Pre-signed URLs for direct Cloudflare R2 image uploads.

---

## 4. Documentation & Verification Duties

* **Update Memory**: After significant changes, always update `CURRENT_STATUS.md` and `CHANGELOG.md`. Update `PROJECT_MEMORY.md` or `REQUIREMENTS.md` if architectural or specification changes occur.
* **Build & Test Verification**: Verify build compatibility after code updates. Report accurate test/build outcomes.
