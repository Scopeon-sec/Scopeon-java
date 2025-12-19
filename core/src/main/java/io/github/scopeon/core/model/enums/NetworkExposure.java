package io.github.scopeon.core.model.enums;

/**
 * Network exposure level for hosts and devices.
 *
 * <p>Indicates the network scope and reachability of a host, useful for risk assessment and
 * security policy enforcement.
 */
public enum NetworkExposure {
  /**
   * Isolated container network only.
   *
   * <p>Host is only accessible within a Docker/Podman network or similar isolated network segment.
   */
  CONTAINER_ONLY,

  /**
   * Local Area Network (LAN) only.
   *
   * <p>Host is accessible within the local network but not exposed to the internet (behind NAT,
   * firewall, or private network).
   */
  LAN_ONLY,

  /**
   * Exposed to the internet via public IP.
   *
   * <p>Host has a public-facing IP address and is directly reachable from the internet (e.g., DMZ,
   * cloud instances with public IPs).
   */
  INTERNET_EXPOSED,

  /**
   * Behind VPN or restricted access.
   *
   * <p>Host is accessible only through VPN, bastion hosts, or other controlled access mechanisms.
   */
  VPN_RESTRICTED,

  /**
   * Unknown or not yet determined.
   *
   * <p>Network topology has not been assessed or cannot be determined automatically.
   */
  UNKNOWN
}
