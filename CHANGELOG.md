# CHANGELOG.md - Project History

## [7.1.0] - Calendar Due-Date Calculation & Monthly Grid Indicator Fix

### Fixed
- Fixed calendar calculation logic in `CalendarScreen.kt`:
  - Previously, `FullCalendarModal` drew green indicator dots on every day of the month as long as tasks existed.
  - Implemented `getNextDueDate(plant, taskType)` function that calculates exact future due dates based on care intervals (`waterIntervalDays`, `feedIntervalDays`, `rotateIntervalDays`).
  - Completed tasks advance their due date into the future, clearing today's pending state and placing green indicator dots strictly on future due dates.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [7.0.0] - Care Task Completion Persistence

### Fixed
- Fixed bug where checked calendar care tasks reset upon next login or process restart.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
