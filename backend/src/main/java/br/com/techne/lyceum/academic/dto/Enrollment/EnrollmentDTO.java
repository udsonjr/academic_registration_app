package br.com.techne.lyceum.academic.dto;

import br.com.techne.lyceum.academic.domain.Enrollment;
import br.com.techne.lyceum.academic.domain.EnrollmentStatus;
import java.util.UUID;

public record EnrollmentDTO(
        UUID publicId, StudentDTO student, ClassGroupDTO classGroup, EnrollmentStatus status) {

    public static EnrollmentDTO from(Enrollment enrollment) {
        return new EnrollmentDTO(
                enrollment.getPublicId(),
                StudentDTO.from(enrollment.getStudent()),
                ClassGroupDTO.from(enrollment.getClassGroup()),
                enrollment.getStatus());
    }
}
