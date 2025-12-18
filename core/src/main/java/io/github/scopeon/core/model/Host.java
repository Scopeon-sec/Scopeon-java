package io.github.scopeon.core.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

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
  @Id @NotNull private UUID id;

  @NotNull
  @Column(nullable = false)
  private String hostname;

  private String ip;

  @Column(name = "created_at")
  private Instant createdAt;

  // Installed packages on this host
  @OneToMany(
      mappedBy = "host",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  @Getter(lombok.AccessLevel.NONE)
  private final List<InstalledPackage> installedPackages = new ArrayList<>();

  protected Host() {}

  public Host(@NonNull String hostname, String ip, Instant createdAt) {
    this.id = UUID.randomUUID();
    this.hostname = hostname;
    this.ip = ip;
    this.createdAt = createdAt != null ? createdAt : Instant.now();
  }

  public List<InstalledPackage> getInstalledPackages() {
    return java.util.Collections.unmodifiableList(installedPackages);
  }

  public void addInstalledPackage(InstalledPackage pkg) {
    if (pkg == null) return;
    pkg.setHost(this);
    this.installedPackages.add(pkg);
  }
}
