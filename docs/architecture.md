# Scopeon — Architecture Overview

**Organization**: `io.github.scopeon`  
**Project**: scopeon

## Purpose

This document defines the **component responsibilities** and **architectural decisions** for Scopeon. 

## Products & Target Users

### Desktop Product (Consumer/Power Users)
**Standalone vulnerability scanner**
- Fully offline-capable
- Local CVE matching and database
- Rich desktop integration (tray, notifications)
- Components: `desktop-agent` + `ui-javafx`

### Enterprise Product (Corporate IT/Security Teams)
**Centralized fleet management**
- Server-managed agents across fleet
- Centralized vulnerability database and policies
- Low-resource agents for containers and endpoints
- Components: `server` + `agent-lite` fleet

## Architectural Decisions

### 1. Two Agent Binaries (Different Products, Not Just Implementations)

**desktop-agent** (Java)
- **Target**: Standalone consumer users, developers
- **Operates**: Fully offline, no server required
- **Matching**: Local CVE matching using `core` library
- **Resources**: ~50-100MB RAM (acceptable for desktop)
- **Security**: User owns all data locally

**agent-lite** (Rust)
- **Target**: Enterprise fleet endpoints, containers
- **Operates**: Server-dependent (by design)
- **Matching**: None - server is authoritative (security requirement)
- **Resources**: ~5-20MB RAM (critical for 1000s of endpoints)
- **Security**: Cannot perform matching, prevents tampering

**Rationale**: Different resource constraints and security models justify separate implementations. Language choice (Java vs. Rust) optimizes for each use case.

### 2. Shared Matching Logic (`core` Module)

Both `desktop-agent` and `server` use identical CVE matching logic from the `core` library to ensure consistent vulnerability detection regardless of deployment mode.

**Why not share with agent-lite?** Security boundary - enterprise agents must not perform matching to prevent tampering and ensure audit consistency.

### 3. Detectors as Rust CLI Executables

Platform-specific package detection (dpkg, rpm, brew, npm, etc.) implemented as standalone Rust binaries:
- **No Java dependency**: Work on any system without JRE
- **Cross-agent reuse**: Both Java and Rust agents spawn same detectors
- **Versionable**: Independent versioning and updates
- **Output**: Standard JSON schema to stdout

### 4. Split Persistence Layer

**`persistence-jpa`**: Shared base entities (Host, InstalledPackage)
- **desktop-agent**: H2 embedded file database
- **server**: PostgreSQL/MySQL (via `persistence-jpa-server`)

**`persistence-jpa-server`**: Server-specific entities and extensions
- Extends base entities with multi-tenancy, audit, and user management
- Used only by server product
- Adds ServerHost, ServerUser, UserSession entities

### 5. UI Only for Desktop Product

`ui-javafx` connects exclusively to `desktop-agent` local API. Enterprise users access server web UI (not part of this repository).

## Module Responsibilities

### `core/` (Java Library)
**Domain model and CVE matching engine**

**Responsibilities:**
- Domain entities: `InstalledPackage`, `Vulnerability`, `Scan`, `Finding`, `Notification`
- CVE matching engine: version comparison, normalization, CVSS scoring
- Pure utilities: fingerprinting, ETag generation, DTOs

**Used by**: `desktop-agent`, `server`  
**Dependencies**: None (no Spring, no UI, no persistence)  
**Why separate**: Ensures identical matching behavior across desktop and server

---

### `persistence-jpa/` (Java Library)
**Shared base JPA persistence layer**

**Responsibilities:**
- JPA entity mappings for `core` domain model (Host, InstalledPackage)
- Base repository interfaces and implementations
- Database bootstrap helpers (H2/PostgreSQL configuration)

**Used by**: `desktop-agent` (H2 embedded), `persistence-jpa-server` (base for server entities)  
**Dependencies**: `core`, JPA/Hibernate  
**Why separate**: Shares persistence code while keeping desktop free of server-specific entities

---

### `persistence-jpa-server/` (Java Library)
**Server-specific JPA entities and extensions**

**Responsibilities:**
- Server-specific entities: `ServerHost`, `ServerUser`, `UserSession`
- Multi-tenancy support (organizations, teams)
- Audit trails (createdBy, updatedBy, deletedBy)
- Soft delete functionality
- User authentication and session management

**Used by**: `server` (PostgreSQL/MySQL)  
**Dependencies**: `core`, `persistence-jpa`, JPA/Hibernate  
**Why separate**: Desktop product doesn't need user/auth/multi-tenancy concepts

---

### `detectors/` (Rust Executables) - *To Be Created*
**Platform-specific package detection**

**Responsibilities:**
- Detect installed packages per ecosystem (dpkg, rpm, brew, npm, pip, etc.)
- Output standard JSON schema to stdout
- Work without Java runtime dependency

**Used by**: `desktop-agent`, `agent-lite` (spawned as subprocesses)  
**Technology**: Rust (for static binaries, no runtime dependencies)  
**Schema**: Defined in `api-specs/detector-schema.json`

---

### `desktop-agent/` (Java Application)
**Background agent for standalone desktop product**

**Responsibilities:**
- Run scheduled/ad-hoc scans (spawn detectors)
- Persist packages and findings to local H2 database
- Perform local CVE matching using `core` library
- System tray UI: status, quick actions, notification history
- Native notifications: popups for critical findings
- Localhost API for `ui-javafx` (ETag-based polling)

**Target**: Desktop/laptop users, developers, power users  
**Dependencies**: `core`, `persistence-jpa`, detectors (spawned)  
**Packaging**: jlink/jpackage installer, ~50-100MB RAM  
**Runs**: Background process (tray), fully offline-capable

---

### `ui-javafx/` (JavaFX Application)
**Desktop UI for viewing findings and configuration**

**Responsibilities:**
- Display findings, vulnerabilities, scan history
- Configuration UI (scan schedules, severity filters)
- Manual scan triggers
- Connect to `desktop-agent` localhost API only

**Target**: Desktop product users  
**Dependencies**: `persistence-jpa` (reads local DB), desktop-agent API client  
**Does NOT**: Perform scanning or matching (agent owns this)

---

### `agent-lite/` (Rust Application) - *Separate Repository*
**Lightweight fleet agent for enterprise product**

**Responsibilities:**
- Run detectors, batch and upload package deltas to server (REST)
- Maintain persistent WebSocket connection for server push
- Optional desktop notifications/tray (when on user workstations)
- Headless operation (when in containers/servers)
- **No local matching** (security requirement)

**Target**: Enterprise endpoints, containers, VMs  
**Dependencies**: Detectors (spawned), API specs (generated client)  
**Packaging**: Static Rust binary (~5-20MB), Docker image  
**Runs**: Background daemon, server-dependent

---

### `server/` (Spring Boot Application)
**Enterprise fleet management server**

**Responsibilities:**
- Vulnerability feed ingestion (NVD, OSV, GitHub Advisory, etc.)
- Agent registration and fleet management
- Authoritative CVE matching using `core` library
- WebSocket/REST endpoints for agent communication
- Web-based admin UI and dashboards
- Push notifications/commands to agent fleet

**Target**: Enterprise IT/security teams  
**Dependencies**: `core`, `persistence-jpa` (PostgreSQL/MySQL)  
**Packaging**: Spring Boot JAR, Docker image  
**Database**: PostgreSQL or MySQL (configurable)

---

### `api-specs/` (Documentation) - *Placeholder*
**API contracts and schemas**

**Responsibilities:**
- OpenAPI specification (REST endpoints, autogenerated by Spring Boot)
- WebSocket message schemas (AsyncAPI)
- Detector JSON output schema
- Generated clients for agent-lite (Rust) and desktop-agent (Java)

**Status**: Placeholder - will be populated when server implementation begins

## Data Flow & Responsibilities

### Who Does What

| Capability | desktop-agent | agent-lite | server |
|------------|---------------|------------|--------|
| **Package detection** | ✅ (via detectors) | ✅ (via detectors) | ❌ |
| **CVE matching** | ✅ (local, using `core`) | ❌ (security boundary) | ✅ (authoritative, using `core`) |
| **Local database** | ✅ (H2 embedded) | ❌ (minimal cache only) | ✅ (PostgreSQL/MySQL) |
| **Native notifications** | ✅ (always) | ✅ (desktop mode only) | ❌ |
| **System tray** | ✅ (always) | Optional (desktop mode) | ❌ |
| **Offline operation** | ✅ (fully capable) | ❌ (server-dependent) | N/A |
| **Server communication** | ❌ (standalone) | ✅ (required) | N/A |

### Communication Protocols

**desktop-agent ↔ ui-javafx**
- **Protocol**: Localhost REST API (127.0.0.1)
- **Auth**: Local token
- **Pattern**: UI polls agent for updates (ETag-based)

**agent-lite ↔ server**
- **Upload**: REST (OpenAPI) - POST package deltas
- **Push**: WebSocket (persistent connection) - server sends findings/commands
- **Auth**: TLS + token or mTLS

**server ↔ feeds**
- **Protocol**: HTTPS REST
- **Sources**: NVD, OSV, GitHub Advisory Database
- **Pattern**: Periodic polling + webhook updates

## Security Model

### Desktop Product
- **No server dependency**: All data stays local
- **User controls data**: Local H2 database, file-based
- **Local API**: Bound to 127.0.0.1, token-protected

### Enterprise Product
- **Server is authoritative**: agent-lite cannot perform matching (prevents tampering)
- **Audit trail**: All matching decisions recorded server-side
- **Fleet consistency**: Single source of truth for vulnerability data
- **Communication**: TLS-only, token or mTLS authentication
- **Data sent**: Package metadata and fingerprints only (no binaries)

## Implementation Status

| Module | Status | Priority |
|--------|--------|----------|
| `core/` | ✅ In progress | High |
| `persistence-jpa/` | ✅ In progress | High |
| `persistence-jpa-server/` | ✅ Complete | High |
| `desktop-agent/` | ⏳ Next (merge agent-core) | High |
| `ui-javafx/` | ⏳ Planned | Medium |
| `detectors/` | 📝 To be created (Rust) | High |
| `agent-lite/` | 📝 To be created (Rust, separate repo) | Low |
| `server/` | 📝 Planned (Phase 2) | Low |
| `api-specs/` | 📝 Placeholder | Medium |

## Next Steps

1. **Finish `core/` module**: Complete domain model and matching engine
2. **Merge agent-core into desktop-agent**: Consolidate into single module
3. **Create first detector**: `detector-dpkg` in Rust with JSON schema
4. **Implement desktop-agent**: Background service + tray + localhost API
5. **Build ui-javafx**: Basic findings viewer connecting to local agent

## Further Reading

- [agent-details.md](agent-details.md) - Implementation details for agents
- [server-details.md](server-details.md) - Server architecture and APIs
- [deployment-scenarios.md](deployment-scenarios.md) - Use cases and deployment patterns
- [detector-schema.json](../api-specs/detector-schema.json) - Detector JSON output specification (to be created)