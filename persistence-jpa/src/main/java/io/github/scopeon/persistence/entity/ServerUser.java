package io.github.scopeon.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
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
 * Server-specific user entity with authentication fields.
 *
 * <p>Standalone entity used only by the server product for web-based authentication and
 * authorization. Includes user management, authentication, and host ownership.
 *
 * <p>Desktop product has no user concept (single-user local app).
 */
@Entity
@Table(
    name = "users",
    indexes = {
      @Index(name = "idx_username", columnList = "username"),
      @Index(name = "idx_email", columnList = "email"),
    })
@Getter
@Setter
public class ServerUser {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(nullable = false, updatable = false)
  @Setter(AccessLevel.NONE)
  private UUID id;

  @NotNull
  @Column(nullable = false, unique = true, length = 100)
  private String username;

  @Email
  @Column(unique = true, length = 255)
  private String email;

  @Column(name = "first_name", length = 100)
  private String firstName;

  @Column(name = "last_name", length = 100)
  private String lastName;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @NotNull
  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash; // bcrypt hash

  @Column(name = "role", length = 50, nullable = false)
  private String role = "USER"; // e.g., USER, ADMIN, MANAGER

  @Column(name = "status", length = 50, nullable = false)
  private String status = "ACTIVE"; // e.g., ACTIVE, LOCKED, PENDING_ACTIVATION

  // Security fields
  @Column(name = "last_login")
  private Instant lastLogin;

  @Column(name = "failed_login_attempts")
  private Integer failedLoginAttempts = 0;

  @Column(name = "account_locked_until")
  private Instant accountLockedUntil;

  @Column(name = "password_changed_at")
  private Instant passwordChangedAt;

  @Column(name = "must_change_password")
  private Boolean mustChangePassword = false;

  // Hosts owned by this user
  @OneToMany(
      mappedBy = "owner",
      cascade = CascadeType.ALL,
      orphanRemoval = false,
      fetch = FetchType.LAZY)
  @Getter(lombok.AccessLevel.NONE)
  private final List<ServerHost> hosts = new ArrayList<>();

  protected ServerUser() {}

  public ServerUser(@NonNull String username, @NonNull String passwordHash) {
    this.username = username;
    this.passwordHash = passwordHash;
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
    this.passwordChangedAt = Instant.now();
  }

  public ServerUser(
      @NonNull String username,
      @NonNull String passwordHash,
      String email,
      String firstName,
      String lastName) {
    this.username = username;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.passwordHash = passwordHash;
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
    this.passwordChangedAt = Instant.now();
  }

  public List<ServerHost> getHosts() {
    return java.util.Collections.unmodifiableList(hosts);
  }

  public void addHost(@NonNull ServerHost host) {
    if (!hosts.contains(host)) {
      hosts.add(host);
      host.setOwner(this);
    }
  }

  public void removeHost(@NonNull ServerHost host) {
    if (hosts.contains(host)) {
      hosts.remove(host);
      host.setOwner(null);
    }
  }

  /** Check if the account is currently locked. */
  public boolean isLocked() {
    return accountLockedUntil != null && accountLockedUntil.isAfter(Instant.now());
  }

  /** Increment failed login attempts. */
  public void recordFailedLogin() {
    this.failedLoginAttempts =
        (this.failedLoginAttempts == null ? 0 : this.failedLoginAttempts) + 1;
    this.setUpdatedAt(Instant.now());
  }

  /** Reset failed login attempts and update last login. */
  public void recordSuccessfulLogin() {
    this.failedLoginAttempts = 0;
    this.lastLogin = Instant.now();
    this.setUpdatedAt(Instant.now());
  }

  /**
   * Lock the account for a specified duration.
   *
   * @param durationMinutes Duration in minutes to lock the account
   */
  public void lockAccount(int durationMinutes) {
    this.accountLockedUntil = Instant.now().plusSeconds(durationMinutes * 60L);
    this.setUpdatedAt(Instant.now());
  }

  /** Unlock the account. */
  public void unlockAccount() {
    this.accountLockedUntil = null;
    this.failedLoginAttempts = 0;
    this.setUpdatedAt(Instant.now());
  }
}
