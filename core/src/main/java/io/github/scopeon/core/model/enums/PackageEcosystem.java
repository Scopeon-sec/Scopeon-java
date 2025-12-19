package io.github.scopeon.core.model.enums;

/**
 * Package ecosystem or package manager type.
 *
 * <p>Identifies the source and management system for installed packages, used for CVE matching and
 * package detection strategies.
 */
public enum PackageEcosystem {
  // Language-specific package managers
  /** Maven (Java) */
  MAVEN,

  /** Gradle (Java) */
  GRADLE,

  /** npm (Node.js / JavaScript) */
  NPM,

  /** pip (Python / PyPI) */
  PIP,

  /** RubyGems (Ruby) */
  RUBYGEMS,

  /** Cargo (Rust) */
  CARGO,

  /** NuGet (.NET) */
  NUGET,

  // Operating system package managers
  /** dpkg/deb (Debian, Ubuntu) */
  DEB,

  /** APT (Debian, Ubuntu) */
  APT,

  /** RPM (RHEL, Fedora, CentOS, Rocky, Alma) */
  RPM,

  /** yum (RHEL, CentOS) */
  YUM,

  /** dnf (Fedora, RHEL 8+) */
  DNF,

  /** apk (Alpine Linux) */
  APK,

  /** Chocolatey (Windows) */
  CHOCOLATEY,

  /** winget (Windows Package Manager) */
  WINGET,

  /** pacman (Arch Linux) */
  PACMAN,

  // Platform-specific stores and installers
  /** Homebrew (macOS) */
  HOMEBREW,

  /** macOS .pkg installer */
  MACPKG,

  /** Windows MSI installer (from registry) */
  MSI,

  /** Snap Store (Linux) */
  SNAP,

  /** Flatpak (Linux) */
  FLATPAK,

  // Container and image ecosystems
  /** Docker images */
  DOCKER,

  /** OCI (Open Container Initiative) images */
  OCI,

  // Fallback for unknown or file-based packages
  /** Generic or unknown package source */
  GENERIC
}
