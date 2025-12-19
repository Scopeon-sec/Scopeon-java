package io.github.scopeon.core.model;

import io.github.scopeon.core.model.enums.HostStatus;
import io.github.scopeon.core.model.enums.NetworkExposure;
import io.github.scopeon.core.model.enums.OsFamily;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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

/** Represents a host machine that can have installed packages. */
@Entity
@Table(
    name = "hosts",
    indexes = {
      @Index(name = "idx_hostname", columnList = "hostname"),
    })
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

  @NotNull
  @Column(name = "ip_address", nullable = false)
  private String ipAddress;

  @Column(name = "operating_system")
  private String operatingSystem;

  @Enumerated(EnumType.STRING)
  @Column(name = "os_family")
  private OsFamily osFamily;

  private String architecture;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private HostStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "network_exposure")
  private NetworkExposure networkExposure;

  @Column(name = "last_heartbeat")
  private Instant lastHeartbeat;

  @Column(name = "last_scan")
  private Instant lastScan;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(columnDefinition = "jsonb")
  private String technicalDetails;

  // Installed packages on this host
  @OneToMany(
      mappedBy = "host",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Getter(lombok.AccessLevel.NONE)
  private final List<InstalledPackage> installedPackages = new ArrayList<>();

  protected Host() {}

  public Host(
      @NonNull String hostname,
      @NonNull String ip,
      String os,
      OsFamily osFamily,
      String architecture,
      HostStatus status,
      NetworkExposure networkExposure) {
    this.hostname = hostname;
    this.ipAddress = ip;
    this.operatingSystem = os;
    this.architecture = architecture;
    this.osFamily = osFamily != null ? osFamily : OsFamily.UNKNOWN;
    this.status = status != null ? status : HostStatus.UNKNOWN;
    this.networkExposure = networkExposure != null ? networkExposure : NetworkExposure.UNKNOWN;
  }

  public List<InstalledPackage> getInstalledPackages() {
    return java.util.Collections.unmodifiableList(installedPackages);
  }
}
