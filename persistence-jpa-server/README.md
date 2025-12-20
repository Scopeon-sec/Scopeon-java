# persistence-jpa-server

Server-specific JPA entities and persistence infrastructure for the enterprise product.

## Purpose

This module extends the shared `persistence-jpa` module with server-specific entities that add:
- Multi-tenancy support (organizations, teams)
- User authentication and authorization
- Audit trails (who created/modified entities)
- Soft delete functionality
- Session management

## Entities

### ServerHost
Extends `Host` from `core` with server-specific fields:
- User ownership and access control
- Audit fields (createdBy, updatedBy, deletedBy)
- Organization and team associations
- Soft delete support
- Admin notes and technical details

### ServerUser
Standalone user entity for web authentication:
- Username/email authentication
- Password hashing and security policies
- Account locking and failed login tracking
- Role-based access control (USER, ADMIN, MANAGER)
- Host ownership relationship

### UserSession
Web session tracking:
- Token-based authentication
- Session expiration and validation
- IP address and user agent tracking
- Active/inactive session states

## Dependencies

- `core`: Domain model and matching engine
- `persistence-jpa`: Shared JPA entities and repositories

## Usage

### Desktop Product
**Does not use this module.** Desktop agent depends only on `persistence-jpa` for local H2 database without user/auth concepts.

### Server Product
Depends on this module for:
- PostgreSQL/MySQL database with multi-user support
- Web authentication and authorization
- Fleet management with user ownership
- Audit trails and compliance

## Database Support

- PostgreSQL (recommended for production)
- MySQL (alternative)
- H2 (for testing only)

Server entities use features like:
- JSONB columns (PostgreSQL-specific for technical details)
- UUIDs for primary keys
- Optimistic locking for concurrent updates
