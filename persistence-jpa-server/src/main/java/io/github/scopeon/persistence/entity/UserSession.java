package io.github.scopeon.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/**
 * User session entity for web authentication.
 *
 * <p>Tracks active user sessions for the server product. Each session has a unique token used for
 * authentication. Desktop product does not use sessions.
 */
@Entity
@Table(
    name = "user_sessions",
    indexes = {
      @Index(name = "idx_session_token", columnList = "session_token"),
      @Index(name = "idx_user_id", columnList = "user_id"),
      @Index(name = "idx_expires_at", columnList = "expires_at"),
    })
@Getter
@Setter
public class UserSession {
  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(nullable = false, updatable = false)
  @Setter(AccessLevel.NONE)
  private UUID id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private ServerUser user;

  @NotNull
  @Column(name = "session_token", nullable = false, unique = true, length = 255)
  private String sessionToken;

  @Column(name = "ip_address", length = 45)
  private String ipAddress; // IPv4 or IPv6

  @Column(name = "user_agent", columnDefinition = "TEXT")
  private String userAgent;

  @NotNull
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "last_accessed")
  private Instant lastAccessed;

  @NotNull
  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  protected UserSession() {}

  public UserSession(
      @NonNull ServerUser user,
      @NonNull String sessionToken,
      @NonNull Instant expiresAt,
      String ipAddress,
      String userAgent) {
    this.user = user;
    this.sessionToken = sessionToken;
    this.expiresAt = expiresAt;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.createdAt = Instant.now();
    this.lastAccessed = Instant.now();
  }

  /** Check if the session is currently valid (active and not expired). */
  public boolean isValid() {
    return isActive && expiresAt.isAfter(Instant.now());
  }

  /** Update the last accessed timestamp. */
  public void updateLastAccessed() {
    this.lastAccessed = Instant.now();
  }

  /** Invalidate the session. */
  public void invalidate() {
    this.isActive = false;
  }
}
