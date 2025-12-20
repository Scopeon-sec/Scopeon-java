# agent (desktop-agent)

Background agent for the standalone desktop vulnerability scanner.

## Purpose

This module implements the core scanning engine for the desktop product. It runs as a background service, performing scheduled scans, matching vulnerabilities locally, and providing a localhost API for the UI.

## Responsibilities

### Scanning
- Schedule and execute periodic scans
- Spawn detector executables (dpkg, rpm, brew, npm, etc.)
- Parse detector output and normalize package data
- Track package changes between scans

### CVE Matching
- Use `core` matching engine for local vulnerability detection
- No server dependency - fully offline capable
- Maintain local CVE database (synchronized periodically when online)

### User Interface Integration
- System tray icon with status and quick actions
- Native desktop notifications for critical findings
- Notification history and management

### Local API
- Localhost REST API (127.0.0.1) for `ui-javafx`
- ETag-based polling for efficient updates
- Token-based authentication (local only)

### Data Persistence
- H2 embedded database for packages, findings, scans
- Local CVE database cache
- User preferences and configuration

## Target Users

- Desktop/laptop users
- Developers and power users
- Anyone needing offline vulnerability scanning

## Dependencies

- `core`: Domain model and matching engine
- `persistence-jpa`: H2 embedded database
- Detector executables (Rust binaries, spawned as subprocesses)
- JavaFX (for system tray integration)
- Spring Boot (optional, for REST API)

## Packaging

- **jlink/jpackage**: Self-contained native installers
- **Memory**: ~50-100MB RAM
- **Disk**: ~200MB (includes JRE, CVE database)
- **Platforms**: Windows, macOS, Linux

## Operation

### Startup
1. Initialize H2 database
2. Load CVE database
3. Start system tray icon
4. Start localhost API server
5. Schedule first scan

### Scan Execution
1. Detect OS and installed package managers
2. Spawn appropriate detector executables
3. Parse JSON output from detectors
4. Store packages in H2 database
5. Run CVE matching via `core` engine
6. Generate notifications for new findings
7. Update UI via API

### Offline Capability
- All scanning and matching works without internet
- CVE database synchronized when online (weekly/monthly)
- User can manually update CVE database

## Configuration

- Scan schedules (daily, weekly, on-demand)
- Severity filters (only notify on HIGH/CRITICAL)
- Package manager selection
- Notification preferences
- API token management

## Security

- All data stored locally under user control
- No cloud/server communication required
- API bound to 127.0.0.1 only
- Token-based API authentication
- User owns all vulnerability data

## Status

⏳ **In Progress** - Planned to merge agent-core functionality into this module

## Next Steps

1. Merge agent-core into desktop-agent
2. Implement system tray UI
3. Implement localhost REST API
4. Add detector subprocess spawning
5. Integrate CVE database synchronization
