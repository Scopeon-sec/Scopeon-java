package io.github.scopeon.core.model.enums;

/**
 * Operating system family classification.
 *
 * <p>Used for grouping operating systems by package management and CVE matching strategies.
 */
public enum OsFamily {
  /** Debian-based distributions (Ubuntu, Debian, Mint, etc.) */
  DEBIAN,

  /** RPM-based distributions (RHEL, Fedora, CentOS, Rocky, Alma, etc.) */
  RPM,

  /** macOS operating system */
  MACOS,

  /** Windows operating system */
  WINDOWS,

  /** Alpine Linux (apk-based) */
  ALPINE,

  /** Arch Linux and derivatives (pacman-based) */
  ARCH,

  /** Other Linux distributions or unknown */
  LINUX_OTHER,

  /** Unknown or not yet determined */
  UNKNOWN
}
