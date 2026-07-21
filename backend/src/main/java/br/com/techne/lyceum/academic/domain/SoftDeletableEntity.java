package br.com.techne.lyceum.academic.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class SoftDeletableEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    protected void setCreatedAt() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public void markAsDeleted() {
        this.deletedAt = Instant.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
