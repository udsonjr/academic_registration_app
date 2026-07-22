package br.com.techne.lyceum.academic.service.impl;

import br.com.techne.lyceum.academic.domain.ClassGroup;
import br.com.techne.lyceum.academic.domain.Enrollment;
import br.com.techne.lyceum.academic.domain.EnrollmentStatus;
import br.com.techne.lyceum.academic.domain.User;
import br.com.techne.lyceum.academic.dto.CreateEnrollmentRequest;
import br.com.techne.lyceum.academic.dto.EnrollmentDTO;
import br.com.techne.lyceum.academic.repository.ClassGroupRepository;
import br.com.techne.lyceum.academic.repository.EnrollmentRepository;
import br.com.techne.lyceum.academic.repository.UserRepository;
import br.com.techne.lyceum.academic.security.SecurityUtils;
import br.com.techne.lyceum.academic.service.EnrollmentService;
import br.com.techne.lyceum.academic.shared.exception.ConflictException;
import br.com.techne.lyceum.academic.shared.exception.ForbiddenException;
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
    private final UserRepository userRepository;
    private final ClassGroupRepository classGroupRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getEnrollments() {
        if (!SecurityUtils.isAdmin()) {
            return getEnrollmentsByUser(SecurityUtils.currentUserPublicId());
        }
        return enrollmentRepository.findAll().stream().map(EnrollmentDTO::from).toList();
    }

    @Override
    @Transactional
    public EnrollmentDTO createEnrollment(CreateEnrollmentRequest request) {
        if (!SecurityUtils.isAdmin()
                && !SecurityUtils.currentUserPublicId().equals(request.userPublicId())) {
            throw new ForbiddenException(
                    "ACCESS_DENIED", "Students can only create enrollments for themselves");
        }

        User user = userRepository.getByPublicIdOrThrow(request.userPublicId());
        ClassGroup classGroup =
                classGroupRepository.getByPublicIdOrThrow(request.classGroupPublicId());

        if (!classGroup.getOpenForEnrollment()) {
            throw new ConflictException(
                    "CLASS_GROUP_NOT_OPEN",
                    "Class group is not open for enrollment. publicId: "
                            + request.classGroupPublicId());
        }

        if (enrollmentRepository.existsByUserIdAndClassGroupIdAndStatusIn(
                user.getId(), classGroup.getId(), ACTIVE_STATUSES)) {
            throw new ConflictException(
                    "ENROLLMENT_ALREADY_EXISTS",
                    "User already has an active enrollment in this class group");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setClassGroup(classGroup);
        enrollment.setStatus(EnrollmentStatus.PENDING);

        return EnrollmentDTO.from(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional
    public EnrollmentDTO confirmEnrollment(UUID publicId) {
        SecurityUtils.requireAdmin();

        Enrollment enrollment = enrollmentRepository.getByPublicIdOrThrow(publicId);

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
        return EnrollmentDTO.from(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional
    public EnrollmentDTO cancelEnrollment(UUID publicId) {
        Enrollment enrollment = enrollmentRepository.getByPublicIdOrThrow(publicId);

        if (!SecurityUtils.isAdmin()
                && !SecurityUtils.currentUserPublicId()
                        .equals(enrollment.getUser().getPublicId())) {
            throw new ForbiddenException(
                    "ACCESS_DENIED", "Students can only cancel their own enrollments");
        }

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
        return EnrollmentDTO.from(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getEnrollmentsByUser(UUID userPublicId) {
        if (!SecurityUtils.isAdmin() && !SecurityUtils.currentUserPublicId().equals(userPublicId)) {
            throw new ForbiddenException(
                    "ACCESS_DENIED", "Students can only view their own enrollments");
        }

        User user = userRepository.getByPublicIdOrThrow(userPublicId);
        return enrollmentRepository.findAllByUserId(user.getId()).stream()
                .map(EnrollmentDTO::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getEnrollmentsByClassGroup(UUID classGroupPublicId) {
        SecurityUtils.requireAdmin();
        ClassGroup classGroup = classGroupRepository.getByPublicIdOrThrow(classGroupPublicId);
        return enrollmentRepository.findAllByClassGroupId(classGroup.getId()).stream()
                .map(EnrollmentDTO::from)
                .toList();
    }
}
