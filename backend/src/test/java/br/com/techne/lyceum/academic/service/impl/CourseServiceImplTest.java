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
import br.com.techne.lyceum.academic.dto.CourseDTO;
import br.com.techne.lyceum.academic.dto.CreateCourseRequest;
import br.com.techne.lyceum.academic.dto.UpdateCourseRequest;
import br.com.techne.lyceum.academic.repository.CourseRepository;
import br.com.techne.lyceum.academic.repository.SubjectRepository;
import br.com.techne.lyceum.academic.shared.exception.ConflictException;
import br.com.techne.lyceum.academic.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock private CourseRepository courseRepository;

    @Mock private SubjectRepository subjectRepository;

    @InjectMocks private CourseServiceImpl courseService;

    private Course mockedCourse() {
        return mockedCourse(1L, "Computer Science", "CS degree", true);
    }

    private Course mockedCourse(Long id, String name, String description, Boolean active) {
        Course course = new Course();
        course.setId(id);
        course.setPublicId(UUID.randomUUID());
        course.setName(name);
        course.setDescription(description);
        course.setActive(active);
        return course;
    }

    @Test
    void getCourses_whenCoursesExist_returnsMappedDtos() {
        Course course1 = mockedCourse(1L, "Computer Science", "CS degree", true);
        Course course2 = mockedCourse(2L, "Mathematics", "Math degree", false);
        when(courseRepository.findAll()).thenReturn(List.of(course1, course2));

        List<CourseDTO> result = courseService.getCourses();

        assertEquals(2, result.size());
        assertEquals(course1.getPublicId(), result.get(0).publicId());
        assertEquals("Computer Science", result.get(0).name());
        assertEquals("CS degree", result.get(0).description());
        assertEquals(true, result.get(0).active());
        assertEquals(course2.getPublicId(), result.get(1).publicId());
        assertEquals("Mathematics", result.get(1).name());
        assertEquals(false, result.get(1).active());
        verify(courseRepository).findAll();
    }

    @Test
    void getCourses_whenEmpty_returnsEmptyList() {
        when(courseRepository.findAll()).thenReturn(List.of());

        List<CourseDTO> result = courseService.getCourses();

        assertTrue(result.isEmpty());
        verify(courseRepository).findAll();
    }

    @Test
    void getCourseByPublicId_whenExists_returnsMappedDto() {
        Course course = mockedCourse();
        when(courseRepository.findByPublicId(course.getPublicId())).thenReturn(Optional.of(course));

        CourseDTO result = courseService.getCourseByPublicId(course.getPublicId());

        assertEquals(course.getPublicId(), result.publicId());
        assertEquals("Computer Science", result.name());
        assertEquals("CS degree", result.description());
        assertEquals(true, result.active());
        verify(courseRepository).findByPublicId(course.getPublicId());
    }

    @Test
    void getCourseByPublicId_whenMissing_throwsNotFound() {
        UUID publicId = UUID.randomUUID();
        when(courseRepository.findByPublicId(publicId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> courseService.getCourseByPublicId(publicId));

        assertEquals("COURSE_NOT_FOUND", ex.getCode());
    }

    @Test
    void createCourse_whenValid_persistsAndReturnsDto() {
        CreateCourseRequest request =
                new CreateCourseRequest("Computer Science", "CS degree", true);
        when(courseRepository.save(any(Course.class))).thenReturn(mockedCourse());

        CourseDTO result = courseService.createCourse(request);

        assertEquals("Computer Science", result.name());
        assertEquals("CS degree", result.description());
        assertEquals(true, result.active());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void updateCourse_whenAllFieldsProvided_updatesAndReturnsDto() {
        Course course = mockedCourse();
        UpdateCourseRequest request =
                new UpdateCourseRequest("Software Engineering", "SE degree", false);
        when(courseRepository.findByPublicId(course.getPublicId())).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CourseDTO result = courseService.updateCourse(course.getPublicId(), request);

        assertEquals("Software Engineering", result.name());
        assertEquals("SE degree", result.description());
        assertEquals(false, result.active());
        verify(courseRepository).save(course);
    }

    @Test
    void updateCourse_whenPartialFields_updatesOnlyProvidedFields() {
        Course course = mockedCourse();
        UpdateCourseRequest request = new UpdateCourseRequest(null, null, false);
        when(courseRepository.findByPublicId(course.getPublicId())).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CourseDTO result = courseService.updateCourse(course.getPublicId(), request);

        assertEquals("Computer Science", result.name());
        assertEquals("CS degree", result.description());
        assertEquals(false, result.active());
    }

    @Test
    void updateCourse_whenMissing_throwsNotFound() {
        UUID publicId = UUID.randomUUID();
        UpdateCourseRequest request = new UpdateCourseRequest("Name", "Desc", true);
        when(courseRepository.findByPublicId(publicId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> courseService.updateCourse(publicId, request));

        assertEquals("COURSE_NOT_FOUND", ex.getCode());
        verify(courseRepository, never()).save(any());
    }

    @Test
    void deleteCourse_whenExistsWithoutSubjects_softDeletes() {
        Course course = mockedCourse();
        when(courseRepository.findByPublicId(course.getPublicId())).thenReturn(Optional.of(course));
        when(subjectRepository.existsByCourseId(course.getId())).thenReturn(false);
        when(courseRepository.save(any(Course.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        courseService.deleteCourse(course.getPublicId());

        ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(captor.capture());
        verify(courseRepository, never()).delete(any());
        assertNotNull(captor.getValue().getDeletedAt());
    }

    @Test
    void deleteCourse_whenHasSubjects_throwsConflict() {
        Course course = mockedCourse();
        when(courseRepository.findByPublicId(course.getPublicId())).thenReturn(Optional.of(course));
        when(subjectRepository.existsByCourseId(course.getId())).thenReturn(true);

        ConflictException ex =
                assertThrows(
                        ConflictException.class,
                        () -> courseService.deleteCourse(course.getPublicId()));

        assertEquals("COURSE_HAS_SUBJECTS", ex.getCode());
        verify(courseRepository, never()).save(any());
        verify(courseRepository, never()).delete(any());
    }

    @Test
    void deleteCourse_whenMissing_throwsNotFound() {
        UUID publicId = UUID.randomUUID();
        when(courseRepository.findByPublicId(publicId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> courseService.deleteCourse(publicId));

        assertEquals("COURSE_NOT_FOUND", ex.getCode());
        verify(courseRepository, never()).save(any());
        verify(courseRepository, never()).delete(any());
    }
}
