package io.github.scopeon.core.model;

import io.github.scopeon.core.model.enums.PackageEcosystem;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/** Represents a package (software) detected on a system. */
@Entity
@Table(
    name = "installed_packages",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_pkg_host_name_ecosystem",
          columnNames = {"host_id", "name", "ecosystem"})
    },
    indexes = {
      @Index(name = "idx_name_ecosystem", columnList = "name, ecosystem"),
    })
@Getter
@Setter
public class InstalledPackage {
  @Id @NotNull private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "host_id")
  private Host host;

  private String name;

  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  private PackageEcosystem ecosystem;

  private boolean active;
  private String origin;
  private String vendor;

  @Lob private String metadataJson;

  /** History of observed versions for this package. */
  @OneToMany(
      mappedBy = "pkg",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  @Getter(lombok.AccessLevel.NONE) // Provide custom getter to return unmodifiable list
  private final List<InstalledPackageVersion> versions = new ArrayList<>();

  /**
   * Returns an unmodifiable view of the version history. Modifications must go through
   * addOrUpdateVersion() to ensure validation.
   */
  public List<InstalledPackageVersion> getVersions() {
    return java.util.Collections.unmodifiableList(versions);
  }

  protected InstalledPackage() {}

  public InstalledPackage(
      @NonNull Host host,
      @NonNull String name,
      PackageEcosystem ecosystem,
      String vendor,
      String origin,
      @NonNull String version,
      @NonNull Instant scan,
      Instant previousScan) {
    this.id = UUID.randomUUID();
    this.host = host;
    this.name = name;
    this.ecosystem = ecosystem;
    this.vendor = vendor;
    this.origin = origin;
    this.active = true;

    addVersionEntry(version, scan, previousScan);
  }

  public void addOrUpdateVersion(String version, Instant scan, Instant previousScan) {
    if (active == false) {
      this.active = true;
    }

    InstalledPackageVersion currentVersion = getPreviousVersion();
    if (currentVersion != null && currentVersion.getVersion().equals(version)) {
      // Same version still installed - update last detected timestamp
      currentVersion.setLastDetected(scan);
    } else {
      // Different version - add new entry (upgrade or downgrade)
      addVersionEntry(version, scan, previousScan);
    }
  }

  /**
   * Returns the most recently detected version that has not been removed.
   *
   * @return the current version string, or null if none is active
   */
  public String getCurrentVersion() {
    InstalledPackageVersion prev = getPreviousVersion();
    return prev != null ? prev.getVersion() : null;
  }

  /**
   * Marks the package as removed at the given scan time.
   *
   * @param scan the time at which the package was detected as removed
   */
  public void remove(Instant scan) {
    InstalledPackageVersion prevVersion = getPreviousVersion();
    if (prevVersion != null) {
      prevVersion.setRemovedAt(scan);
    }
    this.active = false;
  }

  // Helper method to add a new version entry
  private void addVersionEntry(String version, Instant scan, Instant previousScan) {
    // Handle previous version if it exists
    InstalledPackageVersion prevVersion = getPreviousVersion();
    if (prevVersion != null && !prevVersion.getVersion().equals(version)) {
      prevVersion.setRemovedAt(scan);
    }

    // Validate no overlapping periods (two versions cannot be installed at the same time)
    validateNoOverlap(version, scan);

    InstalledPackageVersion pkgVersion =
        new InstalledPackageVersion(this, version, scan, previousScan);
    this.versions.add(pkgVersion);
  }

  /**
   * Validates that the new version's installation period does not overlap with any existing
   * version.
   *
   * @throws IllegalStateException if there's an overlap detected
   */
  private void validateNoOverlap(String newVersion, Instant newFirstDetected) {
    for (InstalledPackageVersion existing : versions) {
      // Skip if same version (downgrade scenario creates new entry)
      if (existing.getVersion().equals(newVersion)) {
        continue;
      }

      Instant existingStart = existing.getFirstDetected();
      Instant existingEnd = existing.getRemovedAt(); // null means still active

      // Check for overlap: new version starts before existing version ends
      if (existingEnd == null) {
        // Existing version still active - should have been marked as removed
        throw new IllegalStateException(
            String.format(
                "Cannot add version %s at %s: version %s is still active (not removed)",
                newVersion, newFirstDetected, existing.getVersion()));
      }

      if (newFirstDetected.isBefore(existingEnd)) {
        throw new IllegalStateException(
            String.format(
                "Cannot add version %s at %s: overlaps with version %s (installed %s - %s)",
                newVersion, newFirstDetected, existing.getVersion(), existingStart, existingEnd));
      }
    }
  }

  private InstalledPackageVersion getPreviousVersion() {
    return versions.stream().filter(v -> v.getRemovedAt() == null).findFirst().orElse(null);
  }

  /** A record of a specific version of a package and its detection timeline. */
  @Entity
  @Table(
      name = "installed_package_versions",
      indexes = {
        @Index(name = "idx_pkg_version", columnList = "pkg_id, version"),
        @Index(name = "idx_removed_at", columnList = "removed_at"),
      })
  @Getter
  @Setter
  public static class InstalledPackageVersion {
    @Id @NotNull private String id; // deterministic: pkg.id + ":" + version (set externally)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pkg_id", nullable = false)
    private InstalledPackage pkg;

    @NotNull private String version;

    // date when we know it was not on the system yet (nullable)
    @Column(name = "first_not_detected")
    private Instant firstNotDetected;

    // when version was first observed (UTC)
    @Column(name = "first_detected")
    @NotNull
    private Instant firstDetected;

    // last observed timestamp (UTC)
    @Column(name = "last_detected")
    @NotNull
    private Instant lastDetected;

    // when we know for sure it was removed (nullable)
    @Column(name = "removed_at")
    private Instant removedAt;

    public InstalledPackageVersion(
        @NonNull InstalledPackage pkg,
        @NonNull String version,
        Instant detected,
        Instant firstNotDetected) {
      this.id = pkg.getId().toString() + ":" + version + ":" + detected.toEpochMilli();
      this.pkg = pkg;
      this.version = version;
      this.firstDetected = detected;
      this.lastDetected = detected;
      this.firstNotDetected = firstNotDetected;
    }
  }
}
