package br.com.techne.lyceum.academic.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "class_group")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassGroup extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "enrolled_students", nullable = false)
    private Integer enrolledStudents = 0;

    @Column(name = "vacancy_limit", nullable = false)
    private Integer vacancyLimit;

    @Column(name = "open_for_enrollment", nullable = false)
    private Boolean openForEnrollment = true;

    @PrePersist
    void generatePublicId() {
        if (publicId == null) {
            publicId = UUID.randomUUID();
        }
        if (enrolledStudents == null) {
            enrolledStudents = 0;
        }
        if (openForEnrollment == null) {
            openForEnrollment = true;
        }
    }
}
