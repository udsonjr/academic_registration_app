package br.com.techne.lyceum.academic.dto;

import br.com.techne.lyceum.academic.domain.EnrollmentStatus;
import java.util.UUID;

public record EnrollmentDTO(
        UUID publicId, UUID studentPublicId, UUID classGroupPublicId, EnrollmentStatus status) {}
