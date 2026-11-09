# CURRENT_STATUS.md - Project State Snapshot

## Current Phase
Phase 111: Care & Calendar Current Day Default & Non-Navigating Task Clearing (v3.9, Build 30) Complete

## Completed Work
- [x] Initialized project memory & guidelines (`AGENTS.md`, `PROJECT_MEMORY.md`, `REQUIREMENTS.md`, `CHANGELOG.md`).
- [x] **Care & Calendar Today Default ([CalendarScreen.kt](file:///G:/My%20Drive/APPS/StillLeaf/app/src/main/java/app/vercel/stillleaf/ui/screen/CalendarScreen.kt))**:
  - Replaced hardcoded default index `2` with `todayDayIndex = (today.dayOfWeek.value - 1).coerceIn(0, 6)`.
  - When opening Care & Calendar, the screen **always defaults to today's date** (e.g. Mon 9 on Monday). Users can still select any other day in the week strip or open the Full Calendar modal.
- [x] **Non-Navigating Task Clearing ([MainActivity.kt](file:///G:/My%20Drive/APPS/StillLeaf/app/src/main/java/app/vercel/stillleaf/MainActivity.kt) & [CalendarScreen.kt](file:///G:/My%20Drive/APPS/StillLeaf/app/src/main/java/app/vercel/stillleaf/ui/screen/CalendarScreen.kt))**:
  - Prevented unwanted navigation to plant detail pages when checking tasks off on `CalendarScreen` (`selectedPlant` remains `null`).
  - Filtered `todayTasks` on `CalendarScreen` so completing a task **instantly clears it from `Today's Tasks`** until the plant's recommended care interval (e.g. 7 days) expires and care is due again.
  - Kept active care cycle checkmark retention on `PlantDetailScreen`: task cards stay **checked green** on individual plant pages throughout their active care cycle interval.
- [x] **Version Bump & Signed Release Bundle**:
  - Bumped `versionCode = 30` (`versionName = "3.9"`).
  - Delivered fresh signed release bundle `app-release.aab` directly to Desktop (`C:\Users\wcsur\Desktop\app-release.aab`).
- [x] Verified full app build (`app:assembleDebug` SUCCESSFUL) and pushed updates live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## Next Steps
- [ ] Upload `app-release.aab` from Desktop to Google Play Console Closed Testing track.
