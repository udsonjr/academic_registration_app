package br.com.techne.lyceum.academic.dto;

import br.com.techne.lyceum.academic.domain.Enrollment;
import br.com.techne.lyceum.academic.domain.EnrollmentStatus;
import java.time.Instant;
import java.util.UUID;

public record EnrollmentDTO(
        UUID publicId,
        UserDTO user,
        ClassGroupDTO classGroup,
        EnrollmentStatus status,
        Instant createdAt) {

    public static EnrollmentDTO from(Enrollment enrollment) {
        return new EnrollmentDTO(
                enrollment.getPublicId(),
                UserDTO.from(enrollment.getUser()),
                ClassGroupDTO.from(enrollment.getClassGroup()),
                enrollment.getStatus(),
                enrollment.getCreatedAt());
    }
}
