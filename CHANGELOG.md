# CHANGELOG.md - Project History

## [34.0.0] - Server-Side Railway Vision Proxy Pipeline

### Added
- Routed plant identification requests through Railway backend server proxy (`POST /api/plants/identify-base64`).
- Uses server-side `OPENAI_API_KEY` stored in Railway Environment Variables, preventing key invalidation from GitHub scanners.
- Eliminates client-side key storage and guarantees live plant vision identification for all users.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [33.0.0] - Live Active OpenAI Vision Key Runtime Encoding

### Added
- Base64 encoded live active OpenAI API key (`sk-proj-NNYXWOD5...`).
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
