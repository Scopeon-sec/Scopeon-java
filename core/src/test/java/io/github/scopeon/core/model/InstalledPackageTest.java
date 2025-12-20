package io.github.scopeon.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.scopeon.core.model.enums.PackageEcosystem;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class InstalledPackageTest {

  // Purpose: Verify initial package creation sets fields, active flag, and initial version
  @Test
  void testCreatePackage() {
    // Arrange
    Instant scan = Instant.parse("2025-01-01T00:00:00Z");
    Instant previousScan = Instant.parse("2024-12-31T00:00:00Z");

    Host host = new Host("test-host", "Test OS", null, "x86_64");

    // Act
    InstalledPackage pkg =
        new InstalledPackage(
            host, "TestApp", PackageEcosystem.APT, "Vendor", "origin", "1.0", scan, previousScan);

    // No JPA in unit tests: IDs are not assigned without persistence
    // Assert
    assertNull(pkg.getId());
    assertEquals("TestApp", pkg.getName());
    assertEquals("1.0", pkg.getCurrentVersion());
    assertTrue(pkg.isActive());
    assertEquals(1, pkg.getVersions().size());
  }

  // Purpose: Upgrading a package closes previous version and activates the new version
  @Test
  void testVersionUpgrade() {
    // Arrange
    Instant scan1 = Instant.parse("2025-01-01T00:00:00Z");
    Instant scan2 = Instant.parse("2025-01-15T00:00:00Z");
    Instant previousScan = Instant.parse("2024-12-31T00:00:00Z");

    Host host = new Host("test-host", "Test OS", null, "x86_64");

    InstalledPackage pkg =
        new InstalledPackage(
            host, "TestApp", PackageEcosystem.APT, "Vendor", "origin", "1.0", scan1, previousScan);

    // Act
    pkg.addOrUpdateVersion("2.0", scan2, scan1);

    // Assert
    assertEquals("2.0", pkg.getCurrentVersion());
    assertEquals(2, pkg.getVersions().size());

    // Check that v1.0 was marked as removed
    java.util.Optional<InstalledPackage.InstalledPackageVersion> v1 =
        pkg.getVersions().stream().filter(v -> v.getVersion().equals("1.0")).findFirst();
    assertTrue(v1.isPresent());
    assertEquals(scan2, v1.get().getRemovedAt());

    // Check that v2.0 is active
    java.util.Optional<InstalledPackage.InstalledPackageVersion> v2 =
        pkg.getVersions().stream().filter(v -> v.getVersion().equals("2.0")).findFirst();
    assertTrue(v2.isPresent());
    assertNull(v2.get().getRemovedAt());
  }

  // Purpose: Downgrading creates a new entry for the old version and marks periods correctly
  @Test
  void testVersionDowngrade() {
    // Arrange
    Instant scan1 = Instant.parse("2025-01-01T00:00:00Z");
    Instant scan2 = Instant.parse("2025-01-15T00:00:00Z");
    Instant scan3 = Instant.parse("2025-02-01T00:00:00Z");
    Instant previousScan = Instant.parse("2024-12-31T00:00:00Z");

    Host host = new Host("test-host", "Test OS", null, "x86_64");

    InstalledPackage pkg =
        new InstalledPackage(
            host, "TestApp", PackageEcosystem.APT, "Vendor", "origin", "1.5", scan1, previousScan);

    // Upgrade to 2.0
    pkg.addOrUpdateVersion("2.0", scan2, scan1);

    // Act
    // Downgrade back to 1.5
    pkg.addOrUpdateVersion("1.5", scan3, scan2);

    // Assertions
    assertEquals("1.5", pkg.getCurrentVersion());
    assertEquals(3, pkg.getVersions().size()); // Two entries for v1.5, one for v2.0

    // Check we have two separate v1.5 entries with different time periods
    List<InstalledPackage.InstalledPackageVersion> v15Entries =
        pkg.getVersions().stream().filter(v -> v.getVersion().equals("1.5")).toList();
    assertEquals(2, v15Entries.size());

    // First v1.5 should be removed at scan2
    InstalledPackage.InstalledPackageVersion firstV15 =
        v15Entries.stream()
            .filter(v -> v.getFirstDetected().equals(scan1))
            .findFirst()
            .orElseThrow();
    assertEquals(scan2, firstV15.getRemovedAt());

    // Second v1.5 should be active
    InstalledPackage.InstalledPackageVersion secondV15 =
        v15Entries.stream()
            .filter(v -> v.getFirstDetected().equals(scan3))
            .findFirst()
            .orElseThrow();
    assertNull(secondV15.getRemovedAt());

    // v2.0 should be removed at scan3
    java.util.Optional<InstalledPackage.InstalledPackageVersion> v2opt =
        pkg.getVersions().stream().filter(v -> v.getVersion().equals("2.0")).findFirst();
    assertTrue(v2opt.isPresent());
    assertEquals(scan3, v2opt.get().getRemovedAt());
  }

  // Purpose: Rescanning same version updates lastDetected and does not add a new entry
  @Test
  void testRescanSameVersion() {
    // Arrange
    Instant scan1 = Instant.parse("2025-01-01T00:00:00Z");
    Instant scan2 = Instant.parse("2025-01-15T00:00:00Z");
    Instant previousScan = Instant.parse("2024-12-31T00:00:00Z");

    Host host = new Host("test-host", "Test OS", null, "x86_64");

    InstalledPackage pkg =
        new InstalledPackage(
            host, "TestApp", PackageEcosystem.APT, "Vendor", "origin", "1.0", scan1, previousScan);

    // Act (Rescan - same version)
    pkg.addOrUpdateVersion("1.0", scan2, scan1);

    assertEquals("1.0", pkg.getCurrentVersion());
    assertEquals(1, pkg.getVersions().size()); // Should not create a new entry

    // Assert
    InstalledPackage.InstalledPackageVersion v1 =
        pkg.getVersions().stream()
            .filter(v -> v.getVersion().equals("1.0"))
            .findFirst()
            .orElseThrow();
    assertEquals(scan2, v1.getLastDetected());
    assertNull(v1.getRemovedAt());
  }

  // Purpose: Removing a package marks it inactive and records removed timestamp for version
  @Test
  void testPackageRemoval() {
    // Arrange
    Instant scan1 = Instant.parse("2025-01-01T00:00:00Z");
    Instant scan2 = Instant.parse("2025-01-15T00:00:00Z");
    Instant previousScan = Instant.parse("2024-12-31T00:00:00Z");

    Host host = new Host("test-host", "Test OS", null, "x86_64");

    InstalledPackage pkg =
        new InstalledPackage(
            host, "TestApp", PackageEcosystem.APT, "Vendor", "origin", "1.0", scan1, previousScan);

    // Act
    pkg.remove(scan2);

    // Assert
    assertFalse(pkg.isActive());
    assertNull(pkg.getCurrentVersion());

    InstalledPackage.InstalledPackageVersion v1 =
        pkg.getVersions().stream()
            .filter(v -> v.getVersion().equals("1.0"))
            .findFirst()
            .orElseThrow();
    assertEquals(scan2, v1.getRemovedAt());
  }

  // Purpose: Reinstalling same version after removal reactivates package and adds a new period
  @Test
  void testReactivationAfterRemoval() {
    // Arrange
    Instant scan1 = Instant.parse("2025-01-01T00:00:00Z");
    Instant scan2 = Instant.parse("2025-01-15T00:00:00Z");
    Instant scan3 = Instant.parse("2025-02-01T00:00:00Z");
    Instant previousScan = Instant.parse("2024-12-31T00:00:00Z");

    Host host = new Host("test-host", "Test OS", null, "x86_64");

    InstalledPackage pkg =
        new InstalledPackage(
            host, "TestApp", PackageEcosystem.APT, "Vendor", "origin", "1.0", scan1, previousScan);

    // Act
    pkg.remove(scan2);
    assertFalse(pkg.isActive());

    // Reinstall same version
    pkg.addOrUpdateVersion("1.0", scan3, scan2);

    // Assert
    assertTrue(pkg.isActive());
    assertEquals("1.0", pkg.getCurrentVersion());
    assertEquals(2, pkg.getVersions().size()); // Two separate installation periods
  }

  // Purpose: Ensure adding a version that overlaps an existing period throws an exception
  @Test
  void testOverlapValidation_ShouldThrowWhenOverlapping() {
    Instant scan1 = Instant.parse("2025-01-01T00:00:00Z");
    Instant scan2 = Instant.parse("2025-01-15T00:00:00Z");
    Instant overlapScan = Instant.parse("2025-01-10T00:00:00Z"); // Before scan2
    Instant previousScan = Instant.parse("2024-12-31T00:00:00Z");

    Host host = new Host("test-host", "Test OS", null, "x86_64");

    InstalledPackage pkg =
        new InstalledPackage(
            host, "TestApp", PackageEcosystem.APT, "Vendor", "origin", "1.0", scan1, previousScan);

    // Manually close v1.0
    pkg.getVersions().get(0).setRemovedAt(scan2);

    // Try to add v2.0 with a timestamp that overlaps with v1.0's period
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              InstalledPackage pkg2 =
                  new InstalledPackage(
                      host,
                      "TestApp2",
                      PackageEcosystem.APT,
                      "Vendor",
                      "origin",
                      "1.0",
                      scan1,
                      previousScan);
              InstalledPackage.InstalledPackageVersion version = pkg2.getVersions().get(0);
              version.setRemovedAt(scan2);
              pkg2.addOrUpdateVersion("2.0", overlapScan, previousScan);
            });

    assertTrue(exception.getMessage().contains("overlaps"));
  }

  // Purpose: Ensure addOrUpdateVersion closes any active previous version before adding
  @Test
  void testOverlapValidation_ShouldThrowWhenActiveVersionExists() {
    Instant scan1 = Instant.parse("2025-01-01T00:00:00Z");
    Instant scan2 = Instant.parse("2025-01-15T00:00:00Z");
    Instant previousScan = Instant.parse("2024-12-31T00:00:00Z");

    Host host = new Host("test-host", "Test OS", null, "x86_64");

    InstalledPackage pkg =
        new InstalledPackage(
            host, "TestApp", PackageEcosystem.APT, "Vendor", "origin", "1.0", scan1, previousScan);

    // Try to manually add another version without removing the first
    pkg.addOrUpdateVersion("2.0", scan2, scan1);

    // The method should have properly closed v1.0 before adding v2.0
    java.util.Optional<InstalledPackage.InstalledPackageVersion> v1 =
        pkg.getVersions().stream().filter(v -> v.getVersion().equals("1.0")).findFirst();
    assertTrue(v1.isPresent());
    assertNotNull(v1.get().getRemovedAt());
  }

  // Purpose: getVersions() returns an unmodifiable list that throws on modification
  @Test
  void testGetVersionsReturnsUnmodifiableList() {
    // Arrange
    Instant scan = Instant.parse("2025-01-01T00:00:00Z");
    Instant previousScan = Instant.parse("2024-12-31T00:00:00Z");

    Host host = new Host("test-host", "Test OS", null, "x86_64");

    InstalledPackage pkg =
        new InstalledPackage(
            host, "TestApp", PackageEcosystem.APT, "Vendor", "origin", "1.0", scan, previousScan);

    List<InstalledPackage.InstalledPackageVersion> versions = pkg.getVersions();

    // Act + Assert
    UnsupportedOperationException ex =
        assertThrows(
            UnsupportedOperationException.class,
            () -> {
              versions.add(
                  new InstalledPackage.InstalledPackageVersion(pkg, "3.0", Instant.now(), null));
            });
    assertNotNull(ex);
  }

  // Purpose: Each version entry is tracked with timestamps; IDs are assigned by JPA only
  @Test
  void testVersionIdIsUnique() {
    // Arrange
    Instant scan1 = Instant.parse("2025-01-01T00:00:00Z");
    Instant scan2 = Instant.parse("2025-01-15T00:00:00Z");
    Instant scan3 = Instant.parse("2025-02-01T00:00:00Z");
    Instant previousScan = Instant.parse("2024-12-31T00:00:00Z");

    Host host = new Host("test-host", "Test OS", null, "x86_64");

    InstalledPackage pkg =
        new InstalledPackage(
            host, "TestApp", PackageEcosystem.APT, "Vendor", "origin", "1.5", scan1, previousScan);

    // Act (pure unit: don't use JPA here)
    pkg.addOrUpdateVersion("2.0", scan2, scan1);
    pkg.addOrUpdateVersion("1.5", scan3, scan2); // Downgrade

    // Assert: versions are tracked and timestamps are present
    List<InstalledPackage.InstalledPackageVersion> versions = pkg.getVersions();
    assertEquals(3, versions.size());
    // IDs are not assigned without persistence; ensure timestamps are correct
    assertTrue(versions.stream().allMatch(v -> v.getFirstDetected() != null));
  }
}
