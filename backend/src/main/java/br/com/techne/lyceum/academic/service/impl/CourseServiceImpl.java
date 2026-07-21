package br.com.techne.lyceum.academic.service.impl;

import br.com.techne.lyceum.academic.domain.Course;
import br.com.techne.lyceum.academic.dto.CourseDTO;
import br.com.techne.lyceum.academic.dto.CreateCourseRequest;
import br.com.techne.lyceum.academic.dto.UpdateCourseRequest;
import br.com.techne.lyceum.academic.repository.CourseRepository;
import br.com.techne.lyceum.academic.service.CourseService;
import br.com.techne.lyceum.academic.shared.exception.ConflictException;
import br.com.techne.lyceum.academic.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> getCourses() {
        return courseRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDTO getCourseByPublicId(UUID publicId) {
        return toDto(findCourseByPublicId(publicId));
    }

    @Override
    @Transactional
    public CourseDTO createCourse(CreateCourseRequest request) {
        Course course = new Course();
        course.setName(request.name());
        course.setDescription(request.description());
        course.setActive(request.active());

        return toDto(courseRepository.save(course));
    }

    @Override
    @Transactional
    public CourseDTO updateCourse(UUID publicId, UpdateCourseRequest request) {
        Course course = findCourseByPublicId(publicId);

        if (request.name() != null) {
            course.setName(request.name());
        }
        if (request.description() != null) {
            course.setDescription(request.description());
        }
        if (request.active() != null) {
            course.setActive(request.active());
        }

        return toDto(courseRepository.save(course));
    }

    @Override
    @Transactional
    public void deleteCourse(UUID publicId) {
        Course course = findCourseByPublicId(publicId);

        course.markAsDeleted();
        courseRepository.save(course);
    }

    private Course findCourseByPublicId(UUID publicId) {
        return courseRepository
                .findByPublicId(publicId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "COURSE_NOT_FOUND",
                                        "Course not found for publicId: " + publicId));
    }

    private CourseDTO toDto(Course course) {
        return new CourseDTO(
                course.getPublicId(),
                course.getName(),
                course.getDescription(),
                course.getActive());
    }
}
