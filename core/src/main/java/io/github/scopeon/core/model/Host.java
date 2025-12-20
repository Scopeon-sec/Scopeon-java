package io.github.scopeon.core.model;

import io.github.scopeon.core.model.enums.OsFamily;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/**
 * Base host entity for machine tracking and package management.
 *
 * <p>This is an entity with JOINED inheritance. Desktop product uses this directly for single-user,
 * local host tracking. Server product extends this as ServerHost in persistence-jpa module to add
 * multi-tenancy, ownership, audit trails, and team management features.
 */
@Entity
@Table(name = "hosts")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public class Host {
  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(nullable = false, updatable = false)
  @Setter(AccessLevel.NONE)
  private UUID id;

  @NotNull
  @Column(nullable = false, unique = true)
  private String hostname;

  @Column(name = "operating_system")
  private String operatingSystem;

  @Column(name = "os_family")
  private OsFamily osFamily;

  private String architecture;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  // Installed packages on this host
  @OneToMany(
      mappedBy = "host",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Getter(lombok.AccessLevel.NONE)
  private final List<InstalledPackage> installedPackages = new ArrayList<>();

  protected Host() {}

  public Host(@NonNull String hostname, String os, OsFamily osFamily, String architecture) {
    this.hostname = hostname;
    this.operatingSystem = os;
    this.architecture = architecture;
    this.osFamily = osFamily != null ? osFamily : OsFamily.UNKNOWN;
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  public List<InstalledPackage> getInstalledPackages() {
    return java.util.Collections.unmodifiableList(installedPackages);
  }
}
