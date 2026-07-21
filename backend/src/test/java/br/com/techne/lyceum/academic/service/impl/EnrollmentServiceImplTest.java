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
import br.com.techne.lyceum.academic.domain.Student;
import br.com.techne.lyceum.academic.domain.Subject;
import br.com.techne.lyceum.academic.dto.CreateEnrollmentRequest;
import br.com.techne.lyceum.academic.dto.EnrollmentDTO;
import br.com.techne.lyceum.academic.repository.ClassGroupRepository;
import br.com.techne.lyceum.academic.repository.EnrollmentRepository;
import br.com.techne.lyceum.academic.repository.StudentRepository;
import br.com.techne.lyceum.academic.shared.exception.ConflictException;
import br.com.techne.lyceum.academic.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceImplTest {

    @Mock private EnrollmentRepository enrollmentRepository;

    @Mock private StudentRepository studentRepository;

    @Mock private ClassGroupRepository classGroupRepository;

    @InjectMocks private EnrollmentServiceImpl enrollmentService;

    private Student mockedStudent() {
        Student student = new Student();
        student.setId(1L);
        student.setPublicId(UUID.randomUUID());
        student.setName("student1");
        student.setEmail("student1@example.com");
        student.setPassword("secret1");
        return student;
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

    private Enrollment mockedEnrollment(
            Student student, ClassGroup classGroup, EnrollmentStatus status) {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(1L);
        enrollment.setPublicId(UUID.randomUUID());
        enrollment.setStudent(student);
        enrollment.setClassGroup(classGroup);
        enrollment.setStatus(status);
        return enrollment;
    }

    @Test
    void getEnrollments_whenEnrollmentsExist_returnsMappedDtos() {
        Student student = mockedStudent();
        ClassGroup classGroup = mockedClassGroup(10, 40);
        Enrollment enrollment1 = mockedEnrollment(student, classGroup, EnrollmentStatus.PENDING);
        Enrollment enrollment2 = mockedEnrollment(student, classGroup, EnrollmentStatus.CONFIRMED);
        enrollment2.setId(2L);
        enrollment2.setPublicId(UUID.randomUUID());
        when(enrollmentRepository.findAll()).thenReturn(List.of(enrollment1, enrollment2));

        List<EnrollmentDTO> result = enrollmentService.getEnrollments();

        assertEquals(2, result.size());
        assertEquals(enrollment1.getPublicId(), result.get(0).publicId());
        assertEquals(EnrollmentStatus.PENDING, result.get(0).status());
        assertEquals(enrollment2.getPublicId(), result.get(1).publicId());
        assertEquals(EnrollmentStatus.CONFIRMED, result.get(1).status());
        verify(enrollmentRepository).findAll();
    }

    @Test
    void getEnrollments_whenEmpty_returnsEmptyList() {
        when(enrollmentRepository.findAll()).thenReturn(List.of());

        List<EnrollmentDTO> result = enrollmentService.getEnrollments();

        assertTrue(result.isEmpty());
        verify(enrollmentRepository).findAll();
    }

    @Test
    void createEnrollment_whenValid_persistsWithPendingStatus() {
        Student student = mockedStudent();
        ClassGroup classGroup = mockedClassGroup(0, 40);
        CreateEnrollmentRequest request =
                new CreateEnrollmentRequest(student.getPublicId(), classGroup.getPublicId());
        when(studentRepository.getByPublicIdOrThrow(student.getPublicId())).thenReturn(student);
        when(classGroupRepository.getByPublicIdOrThrow(classGroup.getPublicId()))
                .thenReturn(classGroup);
        when(enrollmentRepository.existsByStudentIdAndClassGroupIdAndStatusIn(
                        anyLong(), anyLong(), anyCollection()))
                .thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EnrollmentDTO result = enrollmentService.createEnrollment(request);

        assertEquals(student.getPublicId(), result.student().publicId());
        assertEquals(classGroup.getPublicId(), result.classGroup().publicId());
        assertEquals(EnrollmentStatus.PENDING, result.status());
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void createEnrollment_whenClassGroupNotOpen_throwsConflict() {
        Student student = mockedStudent();
        ClassGroup classGroup = mockedClassGroup(0, 40);
        classGroup.setOpenForEnrollment(false);
        CreateEnrollmentRequest request =
                new CreateEnrollmentRequest(student.getPublicId(), classGroup.getPublicId());
        when(studentRepository.getByPublicIdOrThrow(student.getPublicId())).thenReturn(student);
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
        Student student = mockedStudent();
        ClassGroup classGroup = mockedClassGroup(0, 40);
        CreateEnrollmentRequest request =
                new CreateEnrollmentRequest(student.getPublicId(), classGroup.getPublicId());
        when(studentRepository.getByPublicIdOrThrow(student.getPublicId())).thenReturn(student);
        when(classGroupRepository.getByPublicIdOrThrow(classGroup.getPublicId()))
                .thenReturn(classGroup);
        when(enrollmentRepository.existsByStudentIdAndClassGroupIdAndStatusIn(
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
        Student student = mockedStudent();
        ClassGroup classGroup = mockedClassGroup(0, 40);
        CreateEnrollmentRequest request =
                new CreateEnrollmentRequest(student.getPublicId(), classGroup.getPublicId());
        when(studentRepository.getByPublicIdOrThrow(student.getPublicId())).thenReturn(student);
        when(classGroupRepository.getByPublicIdOrThrow(classGroup.getPublicId()))
                .thenReturn(classGroup);
        // Cancelled enrollments are not active, so the duplicate check returns false
        when(enrollmentRepository.existsByStudentIdAndClassGroupIdAndStatusIn(
                        anyLong(), anyLong(), anyCollection()))
                .thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EnrollmentDTO result = enrollmentService.createEnrollment(request);

        assertEquals(EnrollmentStatus.PENDING, result.status());
        verify(enrollmentRepository)
                .existsByStudentIdAndClassGroupIdAndStatusIn(
                        student.getId(),
                        classGroup.getId(),
                        Set.of(EnrollmentStatus.PENDING, EnrollmentStatus.CONFIRMED));
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void createEnrollment_whenStudentMissing_throwsNotFound() {
        UUID studentPublicId = UUID.randomUUID();
        CreateEnrollmentRequest request =
                new CreateEnrollmentRequest(studentPublicId, UUID.randomUUID());
        when(studentRepository.getByPublicIdOrThrow(studentPublicId))
                .thenThrow(new ResourceNotFoundException("STUDENT_NOT_FOUND", "Student not found"));

        ResourceNotFoundException ex =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> enrollmentService.createEnrollment(request));

        assertEquals("STUDENT_NOT_FOUND", ex.getCode());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void createEnrollment_whenClassGroupMissing_throwsNotFound() {
        Student student = mockedStudent();
        UUID classGroupPublicId = UUID.randomUUID();
        CreateEnrollmentRequest request =
                new CreateEnrollmentRequest(student.getPublicId(), classGroupPublicId);
        when(studentRepository.getByPublicIdOrThrow(student.getPublicId())).thenReturn(student);
        when(classGroupRepository.getByPublicIdOrThrow(classGroupPublicId))
                .thenThrow(
                        new ResourceNotFoundException(
                                "CLASS_GROUP_NOT_FOUND", "Class group not found"));

        ResourceNotFoundException ex =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> enrollmentService.createEnrollment(request));

        assertEquals("CLASS_GROUP_NOT_FOUND", ex.getCode());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void confirmEnrollment_whenPendingAndVacancyAvailable_confirmsAndConsumesVacancy() {
        Student student = mockedStudent();
        ClassGroup classGroup = mockedClassGroup(10, 40);
        Enrollment enrollment = mockedEnrollment(student, classGroup, EnrollmentStatus.PENDING);
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
    void confirmEnrollment_whenClassGroupFull_throwsConflict() {
        Student student = mockedStudent();
        ClassGroup classGroup = mockedClassGroup(40, 40);
        Enrollment enrollment = mockedEnrollment(student, classGroup, EnrollmentStatus.PENDING);
        when(enrollmentRepository.getByPublicIdOrThrow(enrollment.getPublicId()))
                .thenReturn(enrollment);

        ConflictException ex =
                assertThrows(
                        ConflictException.class,
                        () -> enrollmentService.confirmEnrollment(enrollment.getPublicId()));

        assertEquals("CLASS_GROUP_FULL", ex.getCode());
        assertEquals(40, classGroup.getEnrolledStudents());
        verify(classGroupRepository, never()).save(any());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void confirmEnrollment_whenAlreadyConfirmed_throwsConflict() {
        Student student = mockedStudent();
        ClassGroup classGroup = mockedClassGroup(10, 40);
        Enrollment enrollment = mockedEnrollment(student, classGroup, EnrollmentStatus.CONFIRMED);
        when(enrollmentRepository.getByPublicIdOrThrow(enrollment.getPublicId()))
                .thenReturn(enrollment);

        ConflictException ex =
                assertThrows(
                        ConflictException.class,
                        () -> enrollmentService.confirmEnrollment(enrollment.getPublicId()));

        assertEquals("INVALID_ENROLLMENT_STATUS", ex.getCode());
        verify(classGroupRepository, never()).save(any());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void confirmEnrollment_whenCancelled_throwsConflict() {
        Student student = mockedStudent();
        ClassGroup classGroup = mockedClassGroup(10, 40);
        Enrollment enrollment = mockedEnrollment(student, classGroup, EnrollmentStatus.CANCELLED);
        when(enrollmentRepository.getByPublicIdOrThrow(enrollment.getPublicId()))
                .thenReturn(enrollment);

        ConflictException ex =
                assertThrows(
                        ConflictException.class,
                        () -> enrollmentService.confirmEnrollment(enrollment.getPublicId()));

        assertEquals("INVALID_ENROLLMENT_STATUS", ex.getCode());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void confirmEnrollment_whenMissing_throwsNotFound() {
        UUID publicId = UUID.randomUUID();
        when(enrollmentRepository.getByPublicIdOrThrow(publicId))
                .thenThrow(
                        new ResourceNotFoundException(
                                "ENROLLMENT_NOT_FOUND", "Enrollment not found"));

        ResourceNotFoundException ex =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> enrollmentService.confirmEnrollment(publicId));

        assertEquals("ENROLLMENT_NOT_FOUND", ex.getCode());
    }

    @Test
    void cancelEnrollment_whenPending_cancelsWithoutReleasingVacancy() {
        Student student = mockedStudent();
        ClassGroup classGroup = mockedClassGroup(10, 40);
        Enrollment enrollment = mockedEnrollment(student, classGroup, EnrollmentStatus.PENDING);
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
    void cancelEnrollment_whenConfirmed_cancelsAndReleasesVacancy() {
        Student student = mockedStudent();
        ClassGroup classGroup = mockedClassGroup(10, 40);
        Enrollment enrollment = mockedEnrollment(student, classGroup, EnrollmentStatus.CONFIRMED);
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
    void cancelEnrollment_whenAlreadyCancelled_throwsConflict() {
        Student student = mockedStudent();
        ClassGroup classGroup = mockedClassGroup(10, 40);
        Enrollment enrollment = mockedEnrollment(student, classGroup, EnrollmentStatus.CANCELLED);
        when(enrollmentRepository.getByPublicIdOrThrow(enrollment.getPublicId()))
                .thenReturn(enrollment);

        ConflictException ex =
                assertThrows(
                        ConflictException.class,
                        () -> enrollmentService.cancelEnrollment(enrollment.getPublicId()));

        assertEquals("INVALID_ENROLLMENT_STATUS", ex.getCode());
        verify(classGroupRepository, never()).save(any());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void cancelEnrollment_whenMissing_throwsNotFound() {
        UUID publicId = UUID.randomUUID();
        when(enrollmentRepository.getByPublicIdOrThrow(publicId))
                .thenThrow(
                        new ResourceNotFoundException(
                                "ENROLLMENT_NOT_FOUND", "Enrollment not found"));

        ResourceNotFoundException ex =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> enrollmentService.cancelEnrollment(publicId));

        assertEquals("ENROLLMENT_NOT_FOUND", ex.getCode());
    }

    @Test
    void getEnrollmentsByStudent_whenStudentExists_returnsMappedDtos() {
        Student student = mockedStudent();
        ClassGroup classGroup = mockedClassGroup(10, 40);
        Enrollment enrollment = mockedEnrollment(student, classGroup, EnrollmentStatus.PENDING);
        when(studentRepository.getByPublicIdOrThrow(student.getPublicId())).thenReturn(student);
        when(enrollmentRepository.findAllByStudentId(student.getId()))
                .thenReturn(List.of(enrollment));

        List<EnrollmentDTO> result =
                enrollmentService.getEnrollmentsByStudent(student.getPublicId());

        assertEquals(1, result.size());
        assertEquals(enrollment.getPublicId(), result.get(0).publicId());
        assertEquals(student.getPublicId(), result.get(0).student().publicId());
        assertEquals(classGroup.getPublicId(), result.get(0).classGroup().publicId());
        assertEquals(EnrollmentStatus.PENDING, result.get(0).status());
    }

    @Test
    void getEnrollmentsByStudent_whenNoEnrollments_returnsEmptyList() {
        Student student = mockedStudent();
        when(studentRepository.getByPublicIdOrThrow(student.getPublicId())).thenReturn(student);
        when(enrollmentRepository.findAllByStudentId(student.getId())).thenReturn(List.of());

        List<EnrollmentDTO> result =
                enrollmentService.getEnrollmentsByStudent(student.getPublicId());

        assertTrue(result.isEmpty());
    }

    @Test
    void getEnrollmentsByStudent_whenStudentMissing_throwsNotFound() {
        UUID studentPublicId = UUID.randomUUID();
        when(studentRepository.getByPublicIdOrThrow(studentPublicId))
                .thenThrow(new ResourceNotFoundException("STUDENT_NOT_FOUND", "Student not found"));

        ResourceNotFoundException ex =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> enrollmentService.getEnrollmentsByStudent(studentPublicId));

        assertEquals("STUDENT_NOT_FOUND", ex.getCode());
    }

    @Test
    void getEnrollmentsByClassGroup_whenClassGroupExists_returnsMappedDtos() {
        Student student = mockedStudent();
        ClassGroup classGroup = mockedClassGroup(10, 40);
        Enrollment enrollment = mockedEnrollment(student, classGroup, EnrollmentStatus.CONFIRMED);
        when(classGroupRepository.getByPublicIdOrThrow(classGroup.getPublicId()))
                .thenReturn(classGroup);
        when(enrollmentRepository.findAllByClassGroupId(classGroup.getId()))
                .thenReturn(List.of(enrollment));

        List<EnrollmentDTO> result =
                enrollmentService.getEnrollmentsByClassGroup(classGroup.getPublicId());

        assertEquals(1, result.size());
        assertEquals(enrollment.getPublicId(), result.get(0).publicId());
        assertEquals(EnrollmentStatus.CONFIRMED, result.get(0).status());
    }

    @Test
    void getEnrollmentsByClassGroup_whenClassGroupMissing_throwsNotFound() {
        UUID classGroupPublicId = UUID.randomUUID();
        when(classGroupRepository.getByPublicIdOrThrow(classGroupPublicId))
                .thenThrow(
                        new ResourceNotFoundException(
                                "CLASS_GROUP_NOT_FOUND", "Class group not found"));

        ResourceNotFoundException ex =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> enrollmentService.getEnrollmentsByClassGroup(classGroupPublicId));

        assertEquals("CLASS_GROUP_NOT_FOUND", ex.getCode());
    }
}
