# server

Enterprise fleet management server for centralized vulnerability scanning.

## Purpose

This module implements the server-side infrastructure for the enterprise product. It manages a fleet of lightweight agents, performs authoritative CVE matching, and provides web-based dashboards for security teams.

## Responsibilities

### Vulnerability Feed Management
- Ingest from multiple sources (NVD, OSV, GitHub Advisory Database)
- Normalize and deduplicate vulnerability data
- Keep CVE database up-to-date (real-time webhooks + periodic polling)
- Track exploit availability and patch information

### Fleet Management
- Agent registration and authentication
- Track agent status, heartbeats, and health
- Push scan commands to agent fleet
- Collect package inventory from agents

### CVE Matching (Authoritative)
- Use `core` matching engine for consistent results
- Match against complete fleet inventory
- Generate findings for all registered hosts
- Track finding lifecycle (new, acknowledged, resolved, false positive)

### Communication
- REST API for agent uploads (package deltas)
- WebSocket connections for server push (findings, commands)
- TLS encryption with token or mTLS authentication

### Web UI
- Dashboard with fleet overview
- Vulnerability reports and trends
- Host and package inventory
- User and team management
- Compliance reporting

### Multi-Tenancy
- Organization and team support
- User authentication and authorization
- Role-based access control (USER, ADMIN, MANAGER)
- Audit trails for all actions

## Target Users

- Corporate IT teams
- Security operations centers (SOCs)
- Compliance and audit teams
- DevOps teams managing container fleets

## Dependencies

- `core`: Domain model and matching engine
- `persistence-jpa`: Base JPA entities
- `persistence-jpa-server`: Server-specific entities (users, sessions)
- Spring Boot
- Spring Security
- Spring WebSocket
- PostgreSQL or MySQL

## Architecture

### Components
- **Feed Ingestion Service**: Background job for CVE database updates
- **Agent API**: REST + WebSocket for agent communication
- **Matching Service**: Authoritative CVE matching via `core`
- **Notification Service**: Alert distribution to users and agents
- **Web UI**: React or Thymeleaf-based admin interface
- **Authentication**: Spring Security with JWT or session-based

### Database Schema
- Users and authentication (ServerUser, UserSession)
- Fleet hosts (ServerHost extends Host)
- Packages and findings (inherited from persistence-jpa)
- Organizations and teams (multi-tenancy)
- Audit logs

## Deployment

### Production
- **Packaging**: Docker image or Spring Boot JAR
- **Database**: PostgreSQL (recommended) or MySQL
- **Reverse Proxy**: Nginx or Traefik for TLS termination
- **Scaling**: Horizontal scaling with load balancer
- **Message Queue**: Optional Redis/RabbitMQ for agent notifications

### Development
- H2 for local testing
- Embedded Spring Boot server
- Mock agent connections

## Security Model

### Agent Communication
- TLS encryption required
- Token-based auth or mutual TLS (mTLS)
- Agent cannot perform matching (security boundary)
- Server is authoritative source of truth

### User Authentication
- Bcrypt password hashing
- Account lockout after failed attempts
- Session management with expiration
- Role-based access control

### Data Protection
- Audit trails for all modifications
- Soft delete support
- Multi-tenant isolation
- Compliance-ready logging

## API Endpoints

### Agent API
- `POST /api/agents/register`: Agent registration
- `POST /api/agents/{id}/packages`: Upload package inventory
- `WS /api/agents/{id}/stream`: WebSocket for server push

### Web API
- `GET /api/hosts`: Fleet inventory
- `GET /api/findings`: Vulnerability findings
- `GET /api/vulnerabilities`: CVE database
- `POST /api/scans`: Trigger manual scan

## Configuration

- Database connection (PostgreSQL/MySQL)
- Feed sources and update schedules
- Authentication provider (local, LDAP, OAuth)
- Notification channels (email, Slack, webhooks)
- Multi-tenancy settings

## Status

📝 **Planned (Phase 2)** - Will be implemented after desktop product is complete

## Next Steps

1. Define OpenAPI specification for agent API
2. Implement feed ingestion service
3. Create agent registration and authentication
4. Build matching service using `core`
5. Develop web UI for fleet management
6. Implement WebSocket push notifications
