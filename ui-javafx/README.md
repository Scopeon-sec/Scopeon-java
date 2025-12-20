# ui-javafx

Desktop GUI for viewing vulnerability findings and configuring scans.

## Purpose

This module provides a rich graphical user interface for the desktop product. It connects to the local `desktop-agent` via localhost API to display findings, manage scans, and configure settings.

## Responsibilities

### Findings Viewer
- Display vulnerability findings with severity, CVSS scores, and details
- Filter by severity (CRITICAL, HIGH, MEDIUM, LOW)
- Sort by date, severity, package, or host
- Search and quick filtering
- Export findings to CSV/JSON

### Vulnerability Details
- CVE information and descriptions
- CVSS score breakdown
- Affected package versions
- Remediation suggestions
- Links to external advisories (NVD, vendor sites)

### Scan Management
- View scan history and results
- Trigger manual scans
- Configure scan schedules
- Monitor scan progress

### Configuration
- Scan schedule settings (daily, weekly, custom)
- Severity filters for notifications
- Package manager selection
- Update CVE database
- Notification preferences

### System Tray Integration
- Quick access from desktop-agent tray icon
- "Open Dashboard" action
- Show recent findings summary

## Target Users

- Desktop product users
- Developers and power users
- Anyone needing visual vulnerability reports

## Dependencies

- `persistence-jpa`: Read local H2 database (direct access)
- desktop-agent API client: REST API calls to 127.0.0.1
- JavaFX 21+ with modern controls
- Charts library for trend visualization

## UI Design

### Main Views
1. **Dashboard**: Overview with severity counts, trends, scan status
2. **Findings**: Detailed table with filtering and search
3. **Hosts**: List of scanned systems (for future multi-host support)
4. **Scans**: Scan history and execution logs
5. **Settings**: Configuration and preferences

### Visual Elements
- Severity badges with color coding (red, orange, yellow, blue)
- Trend charts for vulnerability counts over time
- Notification center with history
- Modern, clean design following platform conventions

## Architecture

### Communication with Agent
- **Primary**: REST API polling (ETag-based for efficiency)
- **Alternative**: WebSocket for real-time updates (future enhancement)
- **Authentication**: Local token stored securely

### Data Access
- Read-only access to local H2 database
- No direct writes (agent owns data)
- Cache API responses to reduce polling

### JavaFX Structure
- FXML for view definitions
- MVVM pattern with property bindings
- Async loading for large datasets
- Responsive design for different screen sizes

## Packaging

- **Bundled with desktop-agent**: Single installer includes both
- **Separate launch**: Can start UI independently of agent
- **Native look**: Follows platform UI guidelines (Windows, macOS, Linux)
- **Size**: ~30-50MB (included in desktop-agent package)

## User Workflow

### First Launch
1. Agent starts in system tray
2. User clicks "Open Dashboard"
3. UI connects to localhost API
4. Shows initial scan progress
5. Displays findings when scan completes

### Daily Use
1. Agent sends notification for new findings
2. User clicks notification or tray icon
3. UI opens to findings view
4. User reviews, acknowledges, or exports findings

### Configuration
1. Open Settings view
2. Adjust scan schedule or filters
3. Changes saved via API to agent
4. Agent applies new configuration

## Features

### Current
- Basic findings table
- Severity filtering
- Manual scan triggering
- Configuration UI

### Planned
- Interactive charts and dashboards
- Finding acknowledgment and notes
- Package dependency visualization
- Compare scans over time
- Multi-host support (for users with VMs)

## Platform Support

- **Windows**: Native Windows 10+ look and feel
- **macOS**: Follows macOS design guidelines
- **Linux**: GTK theme integration

## Development

### Prerequisites
- Java 21+
- JavaFX 21+ SDK
- Running desktop-agent instance for testing

### Running Locally
```bash
./gradlew :ui-javafx:run
```

### Building
```bash
./gradlew :ui-javafx:build
```

## Status

⏳ **Planned** - Will be implemented after desktop-agent core functionality is complete

## Next Steps

1. Design UI mockups and user flows
2. Implement main dashboard view
3. Create findings table with filtering
4. Add API client for agent communication
5. Build configuration UI
6. Implement scan history viewer
