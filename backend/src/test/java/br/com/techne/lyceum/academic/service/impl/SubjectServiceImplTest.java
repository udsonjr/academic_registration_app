package br.com.techne.lyceum.academic.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.techne.lyceum.academic.domain.Course;
import br.com.techne.lyceum.academic.domain.Subject;
import br.com.techne.lyceum.academic.dto.CreateSubjectRequest;
import br.com.techne.lyceum.academic.dto.SubjectDTO;
import br.com.techne.lyceum.academic.dto.UpdateSubjectRequest;
import br.com.techne.lyceum.academic.repository.ClassGroupRepository;
import br.com.techne.lyceum.academic.repository.CourseRepository;
import br.com.techne.lyceum.academic.repository.SubjectRepository;
import br.com.techne.lyceum.academic.shared.exception.ConflictException;
import br.com.techne.lyceum.academic.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubjectServiceImplTest {

    @Mock private SubjectRepository subjectRepository;

    @Mock private CourseRepository courseRepository;

    @Mock private ClassGroupRepository classGroupRepository;

    @InjectMocks private SubjectServiceImpl subjectService;

    private Course mockedCourse() {
        Course course = new Course();
        course.setId(1L);
        course.setPublicId(UUID.randomUUID());
        course.setName("Computer Science");
        course.setDescription("CS degree");
        course.setActive(true);
        return course;
    }

    private Subject mockedSubject(Course course) {
        return mockedSubject(1L, "Algorithms", "Intro to algorithms", course);
    }

    private Subject mockedSubject(Long id, String name, String description, Course course) {
        Subject subject = new Subject();
        subject.setId(id);
        subject.setPublicId(UUID.randomUUID());
        subject.setName(name);
        subject.setDescription(description);
        subject.setCourse(course);
        return subject;
    }

    @Test
    void getSubjects_whenSubjectsExist_returnsMappedDtos() {
        Course course = mockedCourse();
        Subject subject1 = mockedSubject(1L, "Algorithms", "Intro", course);
        Subject subject2 = mockedSubject(2L, "Databases", "SQL basics", course);
        when(subjectRepository.findAll()).thenReturn(List.of(subject1, subject2));

        List<SubjectDTO> result = subjectService.getSubjects();

        assertEquals(2, result.size());
        assertEquals(subject1.getPublicId(), result.get(0).publicId());
        assertEquals("Algorithms", result.get(0).name());
        assertEquals(course.getPublicId(), result.get(0).coursePublicId());
        assertEquals("Databases", result.get(1).name());
        verify(subjectRepository).findAll();
    }

    @Test
    void getSubjects_whenEmpty_returnsEmptyList() {
        when(subjectRepository.findAll()).thenReturn(List.of());

        List<SubjectDTO> result = subjectService.getSubjects();

        assertTrue(result.isEmpty());
        verify(subjectRepository).findAll();
    }

    @Test
    void getSubjectByPublicId_whenExists_returnsMappedDto() {
        Course course = mockedCourse();
        Subject subject = mockedSubject(course);
        when(subjectRepository.getByPublicIdOrThrow(subject.getPublicId())).thenReturn(subject);

        SubjectDTO result = subjectService.getSubjectByPublicId(subject.getPublicId());

        assertEquals(subject.getPublicId(), result.publicId());
        assertEquals("Algorithms", result.name());
        assertEquals(course.getPublicId(), result.coursePublicId());
    }

    @Test
    void getSubjectByPublicId_whenMissing_throwsNotFound() {
        UUID publicId = UUID.randomUUID();
        when(subjectRepository.getByPublicIdOrThrow(publicId))
                .thenThrow(new ResourceNotFoundException("SUBJECT_NOT_FOUND", "Subject not found"));

        ResourceNotFoundException ex =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> subjectService.getSubjectByPublicId(publicId));

        assertEquals("SUBJECT_NOT_FOUND", ex.getCode());
    }

    @Test
    void createSubject_whenValid_persistsAndReturnsDto() {
        Course course = mockedCourse();
        Subject subject = mockedSubject(course);
        CreateSubjectRequest request =
                new CreateSubjectRequest("Algorithms", "Intro to algorithms", course.getPublicId());
        when(courseRepository.getByPublicIdOrThrow(course.getPublicId())).thenReturn(course);
        when(subjectRepository.save(any(Subject.class))).thenReturn(subject);

        SubjectDTO result = subjectService.createSubject(request);

        assertEquals("Algorithms", result.name());
        assertEquals(course.getPublicId(), result.coursePublicId());
        verify(subjectRepository).save(any(Subject.class));
    }

    @Test
    void createSubject_whenCourseMissing_throwsNotFound() {
        UUID coursePublicId = UUID.randomUUID();
        CreateSubjectRequest request =
                new CreateSubjectRequest("Algorithms", "Intro", coursePublicId);
        when(courseRepository.getByPublicIdOrThrow(coursePublicId))
                .thenThrow(new ResourceNotFoundException("COURSE_NOT_FOUND", "Course not found"));

        ResourceNotFoundException ex =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> subjectService.createSubject(request));

        assertEquals("COURSE_NOT_FOUND", ex.getCode());
        verify(subjectRepository, never()).save(any());
    }

    @Test
    void updateSubject_whenAllFieldsProvided_updatesAndReturnsDto() {
        Course course = mockedCourse();
        Course newCourse = mockedCourse();
        newCourse.setId(2L);
        newCourse.setName("Mathematics");
        Subject subject = mockedSubject(course);
        UpdateSubjectRequest request =
                new UpdateSubjectRequest(
                        "Advanced Algorithms", "Advanced", newCourse.getPublicId());
        when(subjectRepository.getByPublicIdOrThrow(subject.getPublicId())).thenReturn(subject);
        when(courseRepository.getByPublicIdOrThrow(newCourse.getPublicId())).thenReturn(newCourse);
        when(subjectRepository.save(any(Subject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SubjectDTO result = subjectService.updateSubject(subject.getPublicId(), request);

        assertEquals("Advanced Algorithms", result.name());
        assertEquals("Advanced", result.description());
        assertEquals(newCourse.getPublicId(), result.coursePublicId());
    }

    @Test
    void updateSubject_whenPartialFields_updatesOnlyProvidedFields() {
        Course course = mockedCourse();
        Subject subject = mockedSubject(course);
        UpdateSubjectRequest request = new UpdateSubjectRequest("New Name", null, null);
        when(subjectRepository.getByPublicIdOrThrow(subject.getPublicId())).thenReturn(subject);
        when(subjectRepository.save(any(Subject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SubjectDTO result = subjectService.updateSubject(subject.getPublicId(), request);

        assertEquals("New Name", result.name());
        assertEquals("Intro to algorithms", result.description());
        assertEquals(course.getPublicId(), result.coursePublicId());
        verify(courseRepository, never()).getByPublicIdOrThrow(any());
    }

    @Test
    void updateSubject_whenSubjectMissing_throwsNotFound() {
        UUID publicId = UUID.randomUUID();
        UpdateSubjectRequest request = new UpdateSubjectRequest("Name", "Desc", UUID.randomUUID());
        when(subjectRepository.getByPublicIdOrThrow(publicId))
                .thenThrow(new ResourceNotFoundException("SUBJECT_NOT_FOUND", "Subject not found"));

        ResourceNotFoundException ex =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> subjectService.updateSubject(publicId, request));

        assertEquals("SUBJECT_NOT_FOUND", ex.getCode());
        verify(subjectRepository, never()).save(any());
    }

    @Test
    void deleteSubject_whenExistsWithoutClassGroups_softDeletes() {
        Course course = mockedCourse();
        Subject subject = mockedSubject(course);
        when(subjectRepository.getByPublicIdOrThrow(subject.getPublicId())).thenReturn(subject);
        when(classGroupRepository.existsBySubjectId(subject.getId())).thenReturn(false);
        when(subjectRepository.save(any(Subject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        subjectService.deleteSubject(subject.getPublicId());

        ArgumentCaptor<Subject> captor = ArgumentCaptor.forClass(Subject.class);
        verify(subjectRepository).save(captor.capture());
        verify(subjectRepository, never()).delete(any());
        assertNotNull(captor.getValue().getDeletedAt());
    }

    @Test
    void deleteSubject_whenHasClassGroups_throwsConflict() {
        Course course = mockedCourse();
        Subject subject = mockedSubject(course);
        when(subjectRepository.getByPublicIdOrThrow(subject.getPublicId())).thenReturn(subject);
        when(classGroupRepository.existsBySubjectId(subject.getId())).thenReturn(true);

        ConflictException ex =
                assertThrows(
                        ConflictException.class,
                        () -> subjectService.deleteSubject(subject.getPublicId()));

        assertEquals("SUBJECT_HAS_CLASS_GROUPS", ex.getCode());
        verify(subjectRepository, never()).save(any());
        verify(subjectRepository, never()).delete(any());
    }

    @Test
    void deleteSubject_whenMissing_throwsNotFound() {
        UUID publicId = UUID.randomUUID();
        when(subjectRepository.getByPublicIdOrThrow(publicId))
                .thenThrow(new ResourceNotFoundException("SUBJECT_NOT_FOUND", "Subject not found"));

        ResourceNotFoundException ex =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> subjectService.deleteSubject(publicId));

        assertEquals("SUBJECT_NOT_FOUND", ex.getCode());
        verify(subjectRepository, never()).save(any());
        verify(subjectRepository, never()).delete(any());
    }
}
