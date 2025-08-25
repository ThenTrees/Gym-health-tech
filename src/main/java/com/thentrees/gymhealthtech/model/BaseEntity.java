package com.thentrees.gymhealthtech.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

@Getter
@Setter
@MappedSuperclass
@SQLDelete(
    sql =
        "UPDATE #{#entityName} SET deleted_at = NOW(), is_deleted = true WHERE id = ? AND version = ?")
@Where(clause = "is_deleted = false")
public abstract class BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @Column(name = "deleted_at")
  private OffsetDateTime deletedAt;

  @Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
  private Boolean isDeleted = false;

  @Version
  @Column(name = "version", nullable = false)
  private Long version = 0L;

  @Column(name = "created_by")
  private UUID createdBy;

  @Column(name = "updated_by")
  private UUID updatedBy;

  @PrePersist
  protected void onCreate() {
    if (isDeleted == null) {
      isDeleted = false;
    }
    if (version == null) {
      version = 0L;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    // Có thể thêm logic validation hoặc audit ở đây
  }

  // Utility methods
  public boolean isNew() {
    return this.id == null;
  }

  public void markAsDeleted() {
    this.isDeleted = true;
    this.deletedAt = OffsetDateTime.now();
  }

  public void restore() {
    this.isDeleted = false;
    this.deletedAt = null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BaseEntity that = (BaseEntity) o;
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
