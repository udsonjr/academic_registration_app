package br.com.techne.lyceum.academic.service;

import br.com.techne.lyceum.academic.dto.CreateEnrollmentRequest;
import br.com.techne.lyceum.academic.dto.EnrollmentDTO;
import br.com.techne.lyceum.academic.dto.PageResponse;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface EnrollmentService {

    PageResponse<EnrollmentDTO> getEnrollments(Pageable pageable);

    EnrollmentDTO createEnrollment(CreateEnrollmentRequest request);

    EnrollmentDTO confirmEnrollment(UUID publicId);

    EnrollmentDTO cancelEnrollment(UUID publicId);

    PageResponse<EnrollmentDTO> getEnrollmentsByUser(UUID userPublicId, Pageable pageable);

    PageResponse<EnrollmentDTO> getEnrollmentsByClassGroup(
            UUID classGroupPublicId, Pageable pageable);
}
