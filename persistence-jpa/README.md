# persistence-jpa

Shared JPA persistence layer for base domain entities.

## Purpose

This module provides JPA entity mappings and repositories for the core domain model. It's designed to work with multiple database backends and is used by both desktop and server products.

## Responsibilities

### Entity Mappings
- `Host`: JPA mapping for machine/system tracking
- `InstalledPackage`: Package installation records with host relationships

### Repository Layer
- Base repository interfaces and implementations
- Query methods for common operations
- Relationship management (Host ↔ InstalledPackage)

### Database Configuration
- H2 embedded database support (desktop)
- PostgreSQL support (server)
- MySQL support (server alternative)
- Schema generation and migration helpers

## Used By

### Desktop Product
- **desktop-agent**: H2 embedded file database for single-user local storage
- **ui-javafx**: Reads local H2 database for display

### Server Product
- **persistence-jpa-server**: Base entities extended with server-specific fields
- Accessed via PostgreSQL or MySQL

## Dependencies

- `core`: Domain model
- Jakarta Persistence API (JPA)
- Jakarta Validation API
- Hibernate ORM
- HikariCP (connection pooling)

## Database Support

| Database | Usage | Notes |
|----------|-------|-------|
| H2 | Desktop product | Embedded file-based, no server required |
| PostgreSQL | Server product | Recommended for production |
| MySQL | Server product | Alternative option |

## Design Principles

1. **Database-agnostic**: Works with H2, PostgreSQL, and MySQL
2. **Minimal dependencies**: Only JPA entities, no server-specific concepts
3. **Inheritance support**: Uses JOINED table strategy for entity extensions
4. **Lazy loading**: Optimized for performance with relationships

## Notes

- Desktop product uses this module directly with H2
- Server product extends entities via `persistence-jpa-server` module
- No user, authentication, or multi-tenancy concepts (server adds those)
