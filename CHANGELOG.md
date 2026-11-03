# CHANGELOG.md - Project History

## [35.0.0] - Railway TypeScript Build Error Resolution

### Fixed
- Added exported alias `identifyPlantFromImageKey` in `plantVisionService.ts`.
- Resolves Railway `npm run build` (`tsc && prisma generate`) compilation error.
- Railway triggers successful deployment for live backend server vision proxy.
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.

## [34.0.0] - Server-Side Railway Vision Proxy Pipeline

### Added
- Routed plant identification requests through Railway backend server proxy (`POST /api/plants/identify-base64`).
- Pushed live to GitHub repository `https://github.com/wcsurface/StillLeaf`.
