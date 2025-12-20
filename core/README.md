# core

Domain model and CVE matching engine for Scopeon.

## Purpose

This module contains the pure business logic and domain model shared across all products. It has no dependencies on persistence, UI, or server frameworks, ensuring the matching logic is consistent everywhere.

## Responsibilities

### Domain Entities
- `Host`: Machine/system tracking
- `InstalledPackage`: Software package information with version and ecosystem
- `Vulnerability`: CVE/security advisory data
- `Scan`: Scan execution records
- `Finding`: Vulnerability matches on specific packages
- `Notification`: User-facing alerts and notifications

### CVE Matching Engine
- Version comparison and normalization across ecosystems
- Package-to-vulnerability matching logic
- CVSS scoring and severity classification
- Version range parsing and evaluation

### Utilities
- Fingerprinting algorithms
- ETag generation for efficient caching
- DTOs for cross-module communication

## Used By

- **desktop-agent**: Local CVE matching for offline scanning
- **server**: Authoritative matching for fleet management
- **ui-javafx**: Domain model for display (via persistence layer)

## Dependencies

**None** - This module is deliberately dependency-free:
- No Spring Framework
- No UI libraries
- No persistence frameworks (just JPA annotations for entity mapping)

## Design Principles

1. **Framework-agnostic**: Pure business logic, testable without infrastructure
2. **Consistent matching**: Same algorithm used by desktop and server products
3. **Ecosystem-aware**: Handles version schemes for dpkg, rpm, npm, pip, Maven, etc.
4. **Performance-focused**: Efficient matching for large package inventories

## Testing

All matching logic should be thoroughly unit tested to ensure consistency across products. Version comparison edge cases are especially critical.

## Future Enhancements

- Support for additional package ecosystems
- Enhanced version range parsing (semver, distro-specific schemes)
- Integration with more vulnerability databases
- Exploit maturity and patch availability tracking
