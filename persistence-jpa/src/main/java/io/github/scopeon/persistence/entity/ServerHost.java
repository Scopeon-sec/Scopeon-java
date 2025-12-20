package io.github.scopeon.persistence.entity;

import io.github.scopeon.core.model.Host;
import io.github.scopeon.core.model.enums.HostStatus;
import io.github.scopeon.core.model.enums.NetworkExposure;
import io.github.scopeon.core.model.enums.OsFamily;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Server-specific host entity with multi-tenancy and audit features.
 *
 * <p>Extends the base Host from core with server-specific fields for fleet management, audit
 * trails, and team collaboration. Used only by the server product for centralized host management.
 *
 * <p>Desktop product uses the base Host class directly without these fields.
 */
@Entity
@Table(
    name = "server_hosts",
    indexes = {
      @Index(name = "idx_hostname", columnList = "hostname"),
      @Index(name = "idx_owner_id", columnList = "owner_id"),
      @Index(name = "idx_created_by", columnList = "created_by"),
      @Index(name = "idx_deleted_at", columnList = "deleted_at"),
    })
@Getter
@Setter
public class ServerHost extends Host {

  @NotNull
  @Column(name = "ip_address", nullable = false)
  private String ipAddress;

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

  @Column(columnDefinition = "jsonb")
  private String technicalDetails;

  // Owner relation for server product - required in server context
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id", nullable = false)
  private ServerUser owner;

  // Audit fields - who created/updated this host
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by")
  private ServerUser createdBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "updated_by")
  private ServerUser updatedBy;

  // Soft delete support
  @Column(name = "deleted_at")
  private Instant deletedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "deleted_by")
  private ServerUser deletedBy;

  // Additional server-specific metadata
  @Column(name = "organization_id")
  private String organizationId; // For multi-tenant setups

  @Column(name = "team_id")
  private String teamId; // For team-based access control

  @Column(columnDefinition = "TEXT")
  private String notes; // Admin notes about this host

  protected ServerHost() {
    super();
  }

  public ServerHost(
      @NonNull String hostname,
      @NonNull String ip,
      String os,
      OsFamily osFamily,
      String architecture,
      HostStatus status,
      NetworkExposure networkExposure,
      ServerUser owner,
      ServerUser createdBy) {
    super(hostname, os, osFamily, architecture);
    this.ipAddress = ip;
    this.status = status != null ? status : HostStatus.UNKNOWN;
    this.networkExposure = networkExposure != null ? networkExposure : NetworkExposure.UNKNOWN;
    this.owner = owner;
    this.createdBy = createdBy;
    this.updatedBy = createdBy;
  }

  /** Check if the host is soft-deleted. */
  public boolean isDeleted() {
    return deletedAt != null;
  }

  /** Soft delete the host. */
  public void softDelete(ServerUser deletedBy) {
    this.deletedAt = Instant.now();
    this.deletedBy = deletedBy;
    this.setUpdatedAt(Instant.now());
    this.updatedBy = deletedBy;
  }

  /** Restore a soft-deleted host. */
  public void restore(ServerUser restoredBy) {
    this.deletedAt = null;
    this.deletedBy = null;
    this.setUpdatedAt(Instant.now());
    this.updatedBy = restoredBy;
  }

  /** Record an update by a specific user. */
  public void recordUpdate(ServerUser updatedBy) {
    this.setUpdatedAt(Instant.now());
    this.updatedBy = updatedBy;
  }
}
