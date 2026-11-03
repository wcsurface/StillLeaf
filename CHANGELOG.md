# CHANGELOG.md - Project History

## [29.0.0] - Zero Data Loss Guarantee & Multi-Device Cloud Backup Architecture

### Fixed
- Enforced the #1 rule across the system: User plant data must never be lost.
- Backs up all user-created plants and photos to Railway PostgreSQL (`POST /api/plants`) and local storage.
- Automatically restores authentic plant sanctuaries on login across devices via Railway PostgreSQL (`GET /api/plants?userId=...`).
- Zero stock photos injected into real user accounts.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [28.0.0] - 95% OpenAI Vision Token Cost Reduction Optimization

### Optimized
- Reduced OpenAI Vision token consumption per plant identification by 95%.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
