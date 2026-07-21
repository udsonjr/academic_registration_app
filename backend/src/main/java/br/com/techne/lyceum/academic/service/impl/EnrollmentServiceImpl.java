package br.com.techne.lyceum.academic.service.impl;

import br.com.techne.lyceum.academic.domain.ClassGroup;
import br.com.techne.lyceum.academic.domain.Enrollment;
import br.com.techne.lyceum.academic.domain.EnrollmentStatus;
import br.com.techne.lyceum.academic.domain.Student;
import br.com.techne.lyceum.academic.dto.CreateEnrollmentRequest;
import br.com.techne.lyceum.academic.dto.EnrollmentDTO;
import br.com.techne.lyceum.academic.repository.ClassGroupRepository;
import br.com.techne.lyceum.academic.repository.EnrollmentRepository;
import br.com.techne.lyceum.academic.repository.StudentRepository;
import br.com.techne.lyceum.academic.service.EnrollmentService;
import br.com.techne.lyceum.academic.shared.exception.ConflictException;
import br.com.techne.lyceum.academic.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private static final Set<EnrollmentStatus> ACTIVE_STATUSES =
            Set.of(EnrollmentStatus.PENDING, EnrollmentStatus.CONFIRMED);

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final ClassGroupRepository classGroupRepository;

    @Override
    @Transactional
    public EnrollmentDTO createEnrollment(CreateEnrollmentRequest request) {
        Student student = findStudentByPublicId(request.studentPublicId());
        ClassGroup classGroup = findClassGroupByPublicId(request.classGroupPublicId());

        if (!classGroup.getOpenForEnrollment()) {
            throw new ConflictException(
                    "CLASS_GROUP_NOT_OPEN",
                    "Class group is not open for enrollment. publicId: "
                            + request.classGroupPublicId());
        }

        if (enrollmentRepository.existsByStudentIdAndClassGroupIdAndStatusIn(
                student.getId(), classGroup.getId(), ACTIVE_STATUSES)) {
            throw new ConflictException(
                    "ENROLLMENT_ALREADY_EXISTS",
                    "Student already has an active enrollment in this class group");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setClassGroup(classGroup);
        enrollment.setStatus(EnrollmentStatus.PENDING);

        return toDto(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional
    public EnrollmentDTO confirmEnrollment(UUID publicId) {
        Enrollment enrollment = findEnrollmentByPublicId(publicId);

        if (enrollment.getStatus() != EnrollmentStatus.PENDING) {
            throw new ConflictException(
                    "INVALID_ENROLLMENT_STATUS",
                    "Only pending enrollments can be confirmed. Current status: "
                            + enrollment.getStatus());
        }

        ClassGroup classGroup = enrollment.getClassGroup();
        if (classGroup.getEnrolledStudents() >= classGroup.getVacancyLimit()) {
            throw new ConflictException(
                    "CLASS_GROUP_FULL",
                    "Class group has no available vacancies. publicId: "
                            + classGroup.getPublicId());
        }

        classGroup.setEnrolledStudents(classGroup.getEnrolledStudents() + 1);
        classGroupRepository.save(classGroup);

        enrollment.setStatus(EnrollmentStatus.CONFIRMED);
        return toDto(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional
    public EnrollmentDTO cancelEnrollment(UUID publicId) {
        Enrollment enrollment = findEnrollmentByPublicId(publicId);

        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            throw new ConflictException(
                    "INVALID_ENROLLMENT_STATUS", "Enrollment is already cancelled");
        }

        if (enrollment.getStatus() == EnrollmentStatus.CONFIRMED) {
            ClassGroup classGroup = enrollment.getClassGroup();
            classGroup.setEnrolledStudents(classGroup.getEnrolledStudents() - 1);
            classGroupRepository.save(classGroup);
        }

        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        return toDto(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getEnrollmentsByStudent(UUID studentPublicId) {
        Student student = findStudentByPublicId(studentPublicId);
        return enrollmentRepository.findAllByStudentId(student.getId()).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getEnrollmentsByClassGroup(UUID classGroupPublicId) {
        ClassGroup classGroup = findClassGroupByPublicId(classGroupPublicId);
        return enrollmentRepository.findAllByClassGroupId(classGroup.getId()).stream()
                .map(this::toDto)
                .toList();
    }

    private Enrollment findEnrollmentByPublicId(UUID publicId) {
        return enrollmentRepository
                .findByPublicId(publicId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "ENROLLMENT_NOT_FOUND",
                                        "Enrollment not found for publicId: " + publicId));
    }

    private Student findStudentByPublicId(UUID publicId) {
        return studentRepository
                .findByPublicId(publicId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "STUDENT_NOT_FOUND",
                                        "Student not found for publicId: " + publicId));
    }

    private ClassGroup findClassGroupByPublicId(UUID publicId) {
        return classGroupRepository
                .findByPublicId(publicId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "CLASS_GROUP_NOT_FOUND",
                                        "Class group not found for publicId: " + publicId));
    }

    private EnrollmentDTO toDto(Enrollment enrollment) {
        return new EnrollmentDTO(
                enrollment.getPublicId(),
                enrollment.getStudent().getPublicId(),
                enrollment.getClassGroup().getPublicId(),
                enrollment.getStatus());
    }
}
