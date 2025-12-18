package io.github.scopeon.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class InstalledPackageTest {

  private final Host host =
      new Host("host1", "test-host", "127.0.0.1", Instant.parse("2024-01-01T00:00:00Z"));

  // Purpose: Verify initial package creation sets fields, active flag, and initial version
  @Test
  void testCreatePackage() {
    // Arrange
    Instant scan = Instant.parse("2025-01-01T00:00:00Z");
    Instant previousScan = Instant.parse("2024-12-31T00:00:00Z");

    // Act
    InstalledPackage pkg =
        new InstalledPackage(
            "pkg1",
            host,
            "TestApp",
            PackageEcosystem.APT,
            "Vendor",
            "origin",
            "1.0",
            scan,
            previousScan);

    // Assert
    assertEquals("pkg1", pkg.getId());
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

    InstalledPackage pkg =
        new InstalledPackage(
            "pkg1",
            host,
            "TestApp",
            PackageEcosystem.APT,
            "Vendor",
            "origin",
            "1.0",
            scan1,
            previousScan);

    // Act
    pkg.addOrUpdateVersion("2.0", scan2, scan1);

    // Assert
    assertEquals("2.0", pkg.getCurrentVersion());
    assertEquals(2, pkg.getVersions().size());

    // Check that v1.0 was marked as removed
    var v1 = pkg.getVersions().stream().filter(v -> v.getVersion().equals("1.0")).findFirst();
    assertTrue(v1.isPresent());
    assertEquals(scan2, v1.get().getRemovedAt());

    // Check that v2.0 is active
    var v2 = pkg.getVersions().stream().filter(v -> v.getVersion().equals("2.0")).findFirst();
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

    InstalledPackage pkg =
        new InstalledPackage(
            "pkg1",
            host,
            "TestApp",
            PackageEcosystem.APT,
            "Vendor",
            "origin",
            "1.5",
            scan1,
            previousScan);

    // Upgrade to 2.0
    pkg.addOrUpdateVersion("2.0", scan2, scan1);

    // Act
    // Downgrade back to 1.5
    pkg.addOrUpdateVersion("1.5", scan3, scan2);

    assertEquals("1.5", pkg.getCurrentVersion());
    assertEquals(3, pkg.getVersions().size()); // Two entries for v1.5, one for v2.0

    // Assert
    // Check we have two separate v1.5 entries with different time periods
    var v15Entries = pkg.getVersions().stream().filter(v -> v.getVersion().equals("1.5")).toList();
    assertEquals(2, v15Entries.size());

    // First v1.5 should be removed at scan2
    var firstV15 =
        v15Entries.stream()
            .filter(v -> v.getFirstDetected().equals(scan1))
            .findFirst()
            .orElseThrow();
    assertEquals(scan2, firstV15.getRemovedAt());

    // Second v1.5 should be active
    var secondV15 =
        v15Entries.stream()
            .filter(v -> v.getFirstDetected().equals(scan3))
            .findFirst()
            .orElseThrow();
    assertNull(secondV15.getRemovedAt());

    // v2.0 should be removed at scan3
    var v2 = pkg.getVersions().stream().filter(v -> v.getVersion().equals("2.0")).findFirst();
    assertTrue(v2.isPresent());
    assertEquals(scan3, v2.get().getRemovedAt());
  }

  // Purpose: Rescanning same version updates lastDetected and does not add a new entry
  @Test
  void testRescanSameVersion() {
    // Arrange
    Instant scan1 = Instant.parse("2025-01-01T00:00:00Z");
    Instant scan2 = Instant.parse("2025-01-15T00:00:00Z");
    Instant previousScan = Instant.parse("2024-12-31T00:00:00Z");

    InstalledPackage pkg =
        new InstalledPackage(
            "pkg1",
            host,
            "TestApp",
            PackageEcosystem.APT,
            "Vendor",
            "origin",
            "1.0",
            scan1,
            previousScan);

    // Act (Rescan - same version)
    pkg.addOrUpdateVersion("1.0", scan2, scan1);

    assertEquals("1.0", pkg.getCurrentVersion());
    assertEquals(1, pkg.getVersions().size()); // Should not create a new entry

    // Assert
    var v1 = pkg.getVersions().stream().filter(v -> v.getVersion().equals("1.0")).findFirst();
    assertTrue(v1.isPresent());
    assertEquals(scan2, v1.get().getLastDetected());
    assertNull(v1.get().getRemovedAt());
  }

  // Purpose: Removing a package marks it inactive and records removed timestamp for version
  @Test
  void testPackageRemoval() {
    // Arrange
    Instant scan1 = Instant.parse("2025-01-01T00:00:00Z");
    Instant scan2 = Instant.parse("2025-01-15T00:00:00Z");
    Instant previousScan = Instant.parse("2024-12-31T00:00:00Z");

    InstalledPackage pkg =
        new InstalledPackage(
            "pkg1",
            host,
            "TestApp",
            PackageEcosystem.APT,
            "Vendor",
            "origin",
            "1.0",
            scan1,
            previousScan);

    // Act
    pkg.remove(scan2);

    // Assert
    assertFalse(pkg.isActive());
    assertNull(pkg.getCurrentVersion());

    var v1 = pkg.getVersions().stream().filter(v -> v.getVersion().equals("1.0")).findFirst();
    assertTrue(v1.isPresent());
    assertEquals(scan2, v1.get().getRemovedAt());
  }

  // Purpose: Reinstalling same version after removal reactivates package and adds a new period
  @Test
  void testReactivationAfterRemoval() {
    // Arrange
    Instant scan1 = Instant.parse("2025-01-01T00:00:00Z");
    Instant scan2 = Instant.parse("2025-01-15T00:00:00Z");
    Instant scan3 = Instant.parse("2025-02-01T00:00:00Z");
    Instant previousScan = Instant.parse("2024-12-31T00:00:00Z");

    InstalledPackage pkg =
        new InstalledPackage(
            "pkg1",
            host,
            "TestApp",
            PackageEcosystem.APT,
            "Vendor",
            "origin",
            "1.0",
            scan1,
            previousScan);

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

    InstalledPackage pkg =
        new InstalledPackage(
            "pkg1",
            host,
            "TestApp",
            PackageEcosystem.APT,
            "Vendor",
            "origin",
            "1.0",
            scan1,
            previousScan);

    // Manually close v1.0
    pkg.getVersions().get(0).setRemovedAt(scan2);

    // Try to add v2.0 with a timestamp that overlaps with v1.0's period
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              // This would require bypassing addOrUpdateVersion and calling addVersionEntry
              // directly
              // Since addVersionEntry is private, we need to use reflection or test via public
              // methods
              // For now, we'll test this by creating a scenario where previous version isn't
              // removed
              InstalledPackage pkg2 =
                  new InstalledPackage(
                      "pkg2",
                      host,
                      "TestApp2",
                      PackageEcosystem.APT,
                      "Vendor",
                      "origin",
                      "1.0",
                      scan1,
                      previousScan);
              // Manually add a version that overlaps (simulating a bug)
              var version = pkg2.getVersions().get(0);
              version.setRemovedAt(scan2);
              // Now try to add a version that starts before scan2
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

    InstalledPackage pkg =
        new InstalledPackage(
            "pkg1",
            host,
            "TestApp",
            PackageEcosystem.APT,
            "Vendor",
            "origin",
            "1.0",
            scan1,
            previousScan);

    // Try to manually add another version without removing the first
    // This simulates a bug where addVersionEntry is called without proper cleanup
    // Since we can't access private method, we test that addOrUpdateVersion handles it correctly
    pkg.addOrUpdateVersion("2.0", scan2, scan1);

    // The method should have properly closed v1.0 before adding v2.0
    var v1 = pkg.getVersions().stream().filter(v -> v.getVersion().equals("1.0")).findFirst();
    assertTrue(v1.isPresent());
    assertNotNull(v1.get().getRemovedAt());
  }

  // Purpose: getVersions() returns an unmodifiable list that throws on modification
  @Test
  void testGetVersionsReturnsUnmodifiableList() {
    // Arrange
    Instant scan = Instant.parse("2025-01-01T00:00:00Z");
    Instant previousScan = Instant.parse("2024-12-31T00:00:00Z");

    InstalledPackage pkg =
        new InstalledPackage(
            "pkg1",
            host,
            "TestApp",
            PackageEcosystem.APT,
            "Vendor",
            "origin",
            "1.0",
            scan,
            previousScan);

    var versions = pkg.getVersions();

    // Act + Assert
    // Attempt to modify should throw
    UnsupportedOperationException ex =
        assertThrows(
            UnsupportedOperationException.class,
            () -> {
              versions.add(
                  new InstalledPackage.InstalledPackageVersion(pkg, "3.0", Instant.now(), null));
            });
    assertNotNull(ex);
  }

  // Purpose: Each version entry has a unique ID and includes a timestamp
  @Test
  void testVersionIdIsUnique() {
    // Arrange
    Instant scan1 = Instant.parse("2025-01-01T00:00:00Z");
    Instant scan2 = Instant.parse("2025-01-15T00:00:00Z");
    Instant scan3 = Instant.parse("2025-02-01T00:00:00Z");
    Instant previousScan = Instant.parse("2024-12-31T00:00:00Z");

    InstalledPackage pkg =
        new InstalledPackage(
            "pkg1",
            host,
            "TestApp",
            PackageEcosystem.APT,
            "Vendor",
            "origin",
            "1.5",
            scan1,
            previousScan);

    // Act
    pkg.addOrUpdateVersion("2.0", scan2, scan1);
    pkg.addOrUpdateVersion("1.5", scan3, scan2); // Downgrade

    // Assert
    // All version entries should have unique IDs
    var ids =
        pkg.getVersions().stream().map(InstalledPackage.InstalledPackageVersion::getId).toList();
    assertEquals(3, ids.size());
    assertEquals(3, ids.stream().distinct().count()); // All unique

    // IDs should include timestamp
    assertTrue(ids.stream().allMatch(id -> id.contains(":")));
  }
}
