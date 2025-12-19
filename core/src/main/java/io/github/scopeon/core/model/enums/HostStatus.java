package io.github.scopeon.core.model.enums;

/**
 * Lifecycle and operational status of a host.
 *
 * <p>Tracks whether a host is actively reporting, quarantined, decommissioned, etc.
 */
public enum HostStatus {
  /**
   * Host is actively reporting and available for scans.
   *
   * <p>Agent is running and heartbeats are current.
   */
  ACTIVE,

  /**
   * Host is idle or sleeping but still managed.
   *
   * <p>No recent heartbeat but not yet marked as offline permanently.
   */
  IDLE,

  /**
   * Host is offline or unreachable.
   *
   * <p>Extended period without heartbeat or explicit disconnect.
   */
  OFFLINE,

  /**
   * Host is sandboxed or isolated for testing/security.
   *
   * <p>May have restricted network access or be running in a test environment.
   */
  SANDBOXED,

  /**
   * Host is quarantined due to security policy.
   *
   * <p>Isolated from network or limited access due to critical vulnerabilities.
   */
  QUARANTINED,

  /**
   * Host is scheduled for decommissioning but still tracked.
   *
   * <p>Being phased out, pending removal from inventory.
   */
  DECOMMISSIONING,

  /**
   * Host has been removed from active management.
   *
   * <p>Decommissioned or no longer part of the fleet. Historical record only.
   */
  REMOVED,

  /**
   * Host status is unknown or not yet determined.
   *
   * <p>Initial state before first heartbeat or status update.
   */
  UNKNOWN
}
