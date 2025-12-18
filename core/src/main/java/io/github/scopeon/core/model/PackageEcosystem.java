package io.github.scopeon.core.model;

/** Represents the ecosystem or package manager for an installed package. */
public enum PackageEcosystem {
  // Language ecosystems
  MAVEN("maven"), // Java
  GRADLE("gradle"), // Java
  NPM("npm"), // Node.js
  PIP("pip"), // Python / PyPI
  RUBYGEMS("rubygems"), // Ruby
  CARGO("cargo"), // Rust
  NUGET("nuget"), // .NET

  // OS package managers
  DEB("deb"), // Debian/Ubuntu (dpkg)
  APT("apt"), // Debian/Ubuntu (apt)
  RPM("rpm"), // RHEL/Fedora/CentOS
  YUM("yum"), // RHEL/Fedora (yum)
  DNF("dnf"), // Fedora (dnf)
  APK("apk"), // Alpine Linux
  CHOCOLATEY("chocolatey"), // Windows
  WINGET("winget"), // Windows

  // Platform stores
  HOMEBREW("homebrew"), // macOS Homebrew
  MACPKG("macpkg"), // macOS .pkg
  MSI("msi"), // Windows MSI (Uninstall registry)
  SNAP("snap"), // Snap Store
  FLATPAK("flatpak"), // Flatpak

  // Container/image ecosystems
  DOCKER("docker"), // Docker/OCI images
  OCI("oci"), // OCI images

  // SBOM/binary-based (file without package manager metadata)
  GENERIC("generic");

  private final String value;

  PackageEcosystem(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  /**
   * Converts a string to a PackageEcosystem enum value. If the string doesn't match any known
   * ecosystem, returns GENERIC.
   *
   * @param value the string value to convert
   * @return the corresponding PackageEcosystem, or GENERIC if not found
   */
  public static PackageEcosystem fromString(String value) {
    if (value == null) {
      return GENERIC;
    }
    for (PackageEcosystem ecosystem : PackageEcosystem.values()) {
      if (ecosystem.value.equalsIgnoreCase(value)) {
        return ecosystem;
      }
    }
    return GENERIC;
  }

  @Override
  public String toString() {
    return value;
  }
}
