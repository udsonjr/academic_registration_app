package br.com.techne.lyceum.academic.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.techne.lyceum.academic.domain.ClassGroup;
import br.com.techne.lyceum.academic.domain.Course;
import br.com.techne.lyceum.academic.domain.Enrollment;
import br.com.techne.lyceum.academic.domain.EnrollmentStatus;
import br.com.techne.lyceum.academic.domain.Subject;
import br.com.techne.lyceum.academic.domain.User;
import br.com.techne.lyceum.academic.domain.UserRole;
import br.com.techne.lyceum.academic.dto.CreateEnrollmentRequest;
import br.com.techne.lyceum.academic.dto.EnrollmentDTO;
import br.com.techne.lyceum.academic.repository.ClassGroupRepository;
import br.com.techne.lyceum.academic.repository.EnrollmentRepository;
import br.com.techne.lyceum.academic.repository.UserRepository;
import br.com.techne.lyceum.academic.security.UserPrincipal;
import br.com.techne.lyceum.academic.shared.exception.ConflictException;
import br.com.techne.lyceum.academic.shared.exception.ForbiddenException;
import br.com.techne.lyceum.academic.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceImplTest {

    @Mock private EnrollmentRepository enrollmentRepository;

    @Mock private UserRepository userRepository;

    @Mock private ClassGroupRepository classGroupRepository;

    @InjectMocks private EnrollmentServiceImpl enrollmentService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private User mockedUser() {
        User user = new User();
        user.setId(1L);
        user.setPublicId(UUID.randomUUID());
        user.setName("student1");
        user.setEmail("student1@example.com");
        user.setPassword("secret1");
        user.setRole(UserRole.STUDENT);
        return user;
    }

    private User mockedAdmin() {
        User user = new User();
        user.setId(99L);
        user.setPublicId(UUID.randomUUID());
        user.setName("admin");
        user.setEmail("admin@admin");
        user.setPassword("admin");
        user.setRole(UserRole.ADMIN);
        return user;
    }

    private void authenticateAs(User user) {
        UserPrincipal principal = new UserPrincipal(user);
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities()));
    }

    private ClassGroup mockedClassGroup(Integer enrolledStudents, Integer vacancyLimit) {
        Course course = new Course();
        course.setId(1L);
        course.setPublicId(UUID.randomUUID());
        course.setName("Computer Science");
        course.setActive(true);

        Subject subject = new Subject();
        subject.setId(1L);
        subject.setPublicId(UUID.randomUUID());
        subject.setName("Algorithms");
        subject.setCourse(course);

        ClassGroup classGroup = new ClassGroup();
        classGroup.setId(1L);
        classGroup.setPublicId(UUID.randomUUID());
        classGroup.setName("Group A");
        classGroup.setSubject(subject);
        classGroup.setEnrolledStudents(enrolledStudents);
        classGroup.setVacancyLimit(vacancyLimit);
        classGroup.setOpenForEnrollment(true);
        return classGroup;
    }

    private Enrollment mockedEnrollment(User user, ClassGroup classGroup, EnrollmentStatus status) {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(1L);
        enrollment.setPublicId(UUID.randomUUID());
        enrollment.setUser(user);
        enrollment.setClassGroup(classGroup);
        enrollment.setStatus(status);
        return enrollment;
    }

    @Test
    void getEnrollments_whenAdmin_returnsAll() {
        authenticateAs(mockedAdmin());
        User user = mockedUser();
        ClassGroup classGroup = mockedClassGroup(10, 40);
        Enrollment enrollment1 = mockedEnrollment(user, classGroup, EnrollmentStatus.PENDING);
        Enrollment enrollment2 = mockedEnrollment(user, classGroup, EnrollmentStatus.CONFIRMED);
        enrollment2.setId(2L);
        enrollment2.setPublicId(UUID.randomUUID());
        when(enrollmentRepository.findAll()).thenReturn(List.of(enrollment1, enrollment2));

        List<EnrollmentDTO> result = enrollmentService.getEnrollments();

        assertEquals(2, result.size());
        verify(enrollmentRepository).findAll();
    }

    @Test
    void getEnrollments_whenStudent_returnsOnlyOwn() {
        User user = mockedUser();
        authenticateAs(user);
        ClassGroup classGroup = mockedClassGroup(10, 40);
        Enrollment enrollment = mockedEnrollment(user, classGroup, EnrollmentStatus.PENDING);
        when(userRepository.getByPublicIdOrThrow(user.getPublicId())).thenReturn(user);
        when(enrollmentRepository.findAllByUserId(user.getId())).thenReturn(List.of(enrollment));

        List<EnrollmentDTO> result = enrollmentService.getEnrollments();

        assertEquals(1, result.size());
        assertEquals(user.getPublicId(), result.get(0).user().publicId());
        verify(enrollmentRepository, never()).findAll();
    }

    @Test
    void createEnrollment_whenValid_persistsWithPendingStatus() {
        User user = mockedUser();
        authenticateAs(user);
        ClassGroup classGroup = mockedClassGroup(0, 40);
        CreateEnrollmentRequest request =
                new CreateEnrollmentRequest(user.getPublicId(), classGroup.getPublicId());
        when(userRepository.getByPublicIdOrThrow(user.getPublicId())).thenReturn(user);
        when(classGroupRepository.getByPublicIdOrThrow(classGroup.getPublicId()))
                .thenReturn(classGroup);
        when(enrollmentRepository.existsByUserIdAndClassGroupIdAndStatusIn(
                        anyLong(), anyLong(), anyCollection()))
                .thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EnrollmentDTO result = enrollmentService.createEnrollment(request);

        assertEquals(user.getPublicId(), result.user().publicId());
        assertEquals(classGroup.getPublicId(), result.classGroup().publicId());
        assertEquals(EnrollmentStatus.PENDING, result.status());
    }

    @Test
    void createEnrollment_whenStudentCreatesForOther_throwsForbidden() {
        authenticateAs(mockedUser());
        CreateEnrollmentRequest request =
                new CreateEnrollmentRequest(UUID.randomUUID(), UUID.randomUUID());

        ForbiddenException ex =
                assertThrows(
                        ForbiddenException.class, () -> enrollmentService.createEnrollment(request));

        assertEquals("ACCESS_DENIED", ex.getCode());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void createEnrollment_whenClassGroupNotOpen_throwsConflict() {
        User user = mockedUser();
        authenticateAs(user);
        ClassGroup classGroup = mockedClassGroup(0, 40);
        classGroup.setOpenForEnrollment(false);
        CreateEnrollmentRequest request =
                new CreateEnrollmentRequest(user.getPublicId(), classGroup.getPublicId());
        when(userRepository.getByPublicIdOrThrow(user.getPublicId())).thenReturn(user);
        when(classGroupRepository.getByPublicIdOrThrow(classGroup.getPublicId()))
                .thenReturn(classGroup);

        ConflictException ex =
                assertThrows(
                        ConflictException.class, () -> enrollmentService.createEnrollment(request));

        assertEquals("CLASS_GROUP_NOT_OPEN", ex.getCode());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void createEnrollment_whenActiveEnrollmentExists_throwsConflict() {
        User user = mockedUser();
        authenticateAs(user);
        ClassGroup classGroup = mockedClassGroup(0, 40);
        CreateEnrollmentRequest request =
                new CreateEnrollmentRequest(user.getPublicId(), classGroup.getPublicId());
        when(userRepository.getByPublicIdOrThrow(user.getPublicId())).thenReturn(user);
        when(classGroupRepository.getByPublicIdOrThrow(classGroup.getPublicId()))
                .thenReturn(classGroup);
        when(enrollmentRepository.existsByUserIdAndClassGroupIdAndStatusIn(
                        anyLong(), anyLong(), anyCollection()))
                .thenReturn(true);

        ConflictException ex =
                assertThrows(
                        ConflictException.class, () -> enrollmentService.createEnrollment(request));

        assertEquals("ENROLLMENT_ALREADY_EXISTS", ex.getCode());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void createEnrollment_whenPreviousEnrollmentCancelled_allowsNewEnrollment() {
        User user = mockedUser();
        authenticateAs(user);
        ClassGroup classGroup = mockedClassGroup(0, 40);
        CreateEnrollmentRequest request =
                new CreateEnrollmentRequest(user.getPublicId(), classGroup.getPublicId());
        when(userRepository.getByPublicIdOrThrow(user.getPublicId())).thenReturn(user);
        when(classGroupRepository.getByPublicIdOrThrow(classGroup.getPublicId()))
                .thenReturn(classGroup);
        when(enrollmentRepository.existsByUserIdAndClassGroupIdAndStatusIn(
                        anyLong(), anyLong(), anyCollection()))
                .thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EnrollmentDTO result = enrollmentService.createEnrollment(request);

        assertEquals(EnrollmentStatus.PENDING, result.status());
        verify(enrollmentRepository)
                .existsByUserIdAndClassGroupIdAndStatusIn(
                        user.getId(),
                        classGroup.getId(),
                        Set.of(EnrollmentStatus.PENDING, EnrollmentStatus.CONFIRMED));
    }

    @Test
    void createEnrollment_whenUserMissing_throwsNotFound() {
        User admin = mockedAdmin();
        authenticateAs(admin);
        UUID userPublicId = UUID.randomUUID();
        CreateEnrollmentRequest request =
                new CreateEnrollmentRequest(userPublicId, UUID.randomUUID());
        when(userRepository.getByPublicIdOrThrow(userPublicId))
                .thenThrow(new ResourceNotFoundException("USER_NOT_FOUND", "User not found"));

        ResourceNotFoundException ex =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> enrollmentService.createEnrollment(request));

        assertEquals("USER_NOT_FOUND", ex.getCode());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void confirmEnrollment_whenPendingAndVacancyAvailable_confirmsAndConsumesVacancy() {
        authenticateAs(mockedAdmin());
        User user = mockedUser();
        ClassGroup classGroup = mockedClassGroup(10, 40);
        Enrollment enrollment = mockedEnrollment(user, classGroup, EnrollmentStatus.PENDING);
        when(enrollmentRepository.getByPublicIdOrThrow(enrollment.getPublicId()))
                .thenReturn(enrollment);
        when(classGroupRepository.save(any(ClassGroup.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EnrollmentDTO result = enrollmentService.confirmEnrollment(enrollment.getPublicId());

        assertEquals(EnrollmentStatus.CONFIRMED, result.status());
        ArgumentCaptor<ClassGroup> captor = ArgumentCaptor.forClass(ClassGroup.class);
        verify(classGroupRepository).save(captor.capture());
        assertEquals(11, captor.getValue().getEnrolledStudents());
    }

    @Test
    void confirmEnrollment_whenStudent_throwsForbidden() {
        authenticateAs(mockedUser());

        ForbiddenException ex =
                assertThrows(
                        ForbiddenException.class,
                        () -> enrollmentService.confirmEnrollment(UUID.randomUUID()));

        assertEquals("ACCESS_DENIED", ex.getCode());
    }

    @Test
    void confirmEnrollment_whenClassGroupFull_throwsConflict() {
        authenticateAs(mockedAdmin());
        User user = mockedUser();
        ClassGroup classGroup = mockedClassGroup(40, 40);
        Enrollment enrollment = mockedEnrollment(user, classGroup, EnrollmentStatus.PENDING);
        when(enrollmentRepository.getByPublicIdOrThrow(enrollment.getPublicId()))
                .thenReturn(enrollment);

        ConflictException ex =
                assertThrows(
                        ConflictException.class,
                        () -> enrollmentService.confirmEnrollment(enrollment.getPublicId()));

        assertEquals("CLASS_GROUP_FULL", ex.getCode());
        verify(classGroupRepository, never()).save(any());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void confirmEnrollment_whenAlreadyConfirmed_throwsConflict() {
        authenticateAs(mockedAdmin());
        User user = mockedUser();
        ClassGroup classGroup = mockedClassGroup(10, 40);
        Enrollment enrollment = mockedEnrollment(user, classGroup, EnrollmentStatus.CONFIRMED);
        when(enrollmentRepository.getByPublicIdOrThrow(enrollment.getPublicId()))
                .thenReturn(enrollment);

        ConflictException ex =
                assertThrows(
                        ConflictException.class,
                        () -> enrollmentService.confirmEnrollment(enrollment.getPublicId()));

        assertEquals("INVALID_ENROLLMENT_STATUS", ex.getCode());
    }

    @Test
    void cancelEnrollment_whenOwnPending_cancelsWithoutReleasingVacancy() {
        User user = mockedUser();
        authenticateAs(user);
        ClassGroup classGroup = mockedClassGroup(10, 40);
        Enrollment enrollment = mockedEnrollment(user, classGroup, EnrollmentStatus.PENDING);
        when(enrollmentRepository.getByPublicIdOrThrow(enrollment.getPublicId()))
                .thenReturn(enrollment);
        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EnrollmentDTO result = enrollmentService.cancelEnrollment(enrollment.getPublicId());

        assertEquals(EnrollmentStatus.CANCELLED, result.status());
        assertEquals(10, classGroup.getEnrolledStudents());
        verify(classGroupRepository, never()).save(any());
    }

    @Test
    void cancelEnrollment_whenOwnConfirmed_cancelsAndReleasesVacancy() {
        User user = mockedUser();
        authenticateAs(user);
        ClassGroup classGroup = mockedClassGroup(10, 40);
        Enrollment enrollment = mockedEnrollment(user, classGroup, EnrollmentStatus.CONFIRMED);
        when(enrollmentRepository.getByPublicIdOrThrow(enrollment.getPublicId()))
                .thenReturn(enrollment);
        when(classGroupRepository.save(any(ClassGroup.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EnrollmentDTO result = enrollmentService.cancelEnrollment(enrollment.getPublicId());

        assertEquals(EnrollmentStatus.CANCELLED, result.status());
        ArgumentCaptor<ClassGroup> captor = ArgumentCaptor.forClass(ClassGroup.class);
        verify(classGroupRepository).save(captor.capture());
        assertEquals(9, captor.getValue().getEnrolledStudents());
    }

    @Test
    void cancelEnrollment_whenOtherStudent_throwsForbidden() {
        authenticateAs(mockedUser());
        User other = mockedUser();
        other.setId(2L);
        other.setPublicId(UUID.randomUUID());
        ClassGroup classGroup = mockedClassGroup(10, 40);
        Enrollment enrollment = mockedEnrollment(other, classGroup, EnrollmentStatus.PENDING);
        when(enrollmentRepository.getByPublicIdOrThrow(enrollment.getPublicId()))
                .thenReturn(enrollment);

        ForbiddenException ex =
                assertThrows(
                        ForbiddenException.class,
                        () -> enrollmentService.cancelEnrollment(enrollment.getPublicId()));

        assertEquals("ACCESS_DENIED", ex.getCode());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void cancelEnrollment_whenAlreadyCancelled_throwsConflict() {
        User user = mockedUser();
        authenticateAs(user);
        ClassGroup classGroup = mockedClassGroup(10, 40);
        Enrollment enrollment = mockedEnrollment(user, classGroup, EnrollmentStatus.CANCELLED);
        when(enrollmentRepository.getByPublicIdOrThrow(enrollment.getPublicId()))
                .thenReturn(enrollment);

        ConflictException ex =
                assertThrows(
                        ConflictException.class,
                        () -> enrollmentService.cancelEnrollment(enrollment.getPublicId()));

        assertEquals("INVALID_ENROLLMENT_STATUS", ex.getCode());
    }

    @Test
    void getEnrollmentsByUser_whenSelf_returnsMappedDtos() {
        User user = mockedUser();
        authenticateAs(user);
        ClassGroup classGroup = mockedClassGroup(10, 40);
        Enrollment enrollment = mockedEnrollment(user, classGroup, EnrollmentStatus.PENDING);
        when(userRepository.getByPublicIdOrThrow(user.getPublicId())).thenReturn(user);
        when(enrollmentRepository.findAllByUserId(user.getId())).thenReturn(List.of(enrollment));

        List<EnrollmentDTO> result = enrollmentService.getEnrollmentsByUser(user.getPublicId());

        assertEquals(1, result.size());
        assertEquals(user.getPublicId(), result.get(0).user().publicId());
    }

    @Test
    void getEnrollmentsByUser_whenOtherStudent_throwsForbidden() {
        authenticateAs(mockedUser());

        ForbiddenException ex =
                assertThrows(
                        ForbiddenException.class,
                        () -> enrollmentService.getEnrollmentsByUser(UUID.randomUUID()));

        assertEquals("ACCESS_DENIED", ex.getCode());
    }

    @Test
    void getEnrollmentsByUser_whenNoEnrollments_returnsEmptyList() {
        User user = mockedUser();
        authenticateAs(user);
        when(userRepository.getByPublicIdOrThrow(user.getPublicId())).thenReturn(user);
        when(enrollmentRepository.findAllByUserId(user.getId())).thenReturn(List.of());

        List<EnrollmentDTO> result = enrollmentService.getEnrollmentsByUser(user.getPublicId());

        assertTrue(result.isEmpty());
    }

    @Test
    void getEnrollmentsByClassGroup_whenAdmin_returnsMappedDtos() {
        authenticateAs(mockedAdmin());
        User user = mockedUser();
        ClassGroup classGroup = mockedClassGroup(10, 40);
        Enrollment enrollment = mockedEnrollment(user, classGroup, EnrollmentStatus.CONFIRMED);
        when(classGroupRepository.getByPublicIdOrThrow(classGroup.getPublicId()))
                .thenReturn(classGroup);
        when(enrollmentRepository.findAllByClassGroupId(classGroup.getId()))
                .thenReturn(List.of(enrollment));

        List<EnrollmentDTO> result =
                enrollmentService.getEnrollmentsByClassGroup(classGroup.getPublicId());

        assertEquals(1, result.size());
        assertEquals(EnrollmentStatus.CONFIRMED, result.get(0).status());
    }

    @Test
    void getEnrollmentsByClassGroup_whenStudent_throwsForbidden() {
        authenticateAs(mockedUser());

        ForbiddenException ex =
                assertThrows(
                        ForbiddenException.class,
                        () -> enrollmentService.getEnrollmentsByClassGroup(UUID.randomUUID()));

        assertEquals("ACCESS_DENIED", ex.getCode());
    }
}
