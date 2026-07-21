package br.com.techne.lyceum.academic.service;

import br.com.techne.lyceum.academic.dto.CreateEnrollmentRequest;
import br.com.techne.lyceum.academic.dto.EnrollmentDTO;
import java.util.List;
import java.util.UUID;

public interface EnrollmentService {

    List<EnrollmentDTO> getEnrollments();

    EnrollmentDTO createEnrollment(CreateEnrollmentRequest request);

    EnrollmentDTO confirmEnrollment(UUID publicId);

    EnrollmentDTO cancelEnrollment(UUID publicId);

    List<EnrollmentDTO> getEnrollmentsByStudent(UUID studentPublicId);

    List<EnrollmentDTO> getEnrollmentsByClassGroup(UUID classGroupPublicId);
}
