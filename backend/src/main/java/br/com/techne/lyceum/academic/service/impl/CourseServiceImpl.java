package br.com.techne.lyceum.academic.service.impl;

import br.com.techne.lyceum.academic.domain.Course;
import br.com.techne.lyceum.academic.dto.CourseDTO;
import br.com.techne.lyceum.academic.dto.CreateCourseRequest;
import br.com.techne.lyceum.academic.dto.PageResponse;
import br.com.techne.lyceum.academic.dto.UpdateCourseRequest;
import br.com.techne.lyceum.academic.repository.CourseRepository;
import br.com.techne.lyceum.academic.repository.SubjectRepository;
import br.com.techne.lyceum.academic.repository.spec.CourseSpecs;
import br.com.techne.lyceum.academic.service.CourseService;
import br.com.techne.lyceum.academic.shared.exception.ConflictException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CourseDTO> getCourses(String name, Boolean active, Pageable pageable) {
        return PageResponse.from(
                courseRepository.findAll(CourseSpecs.withFilters(name, active), pageable),
                CourseDTO::from);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDTO getCourseByPublicId(UUID publicId) {
        return CourseDTO.from(courseRepository.getByPublicIdOrThrow(publicId));
    }

    @Override
    @Transactional
    public CourseDTO createCourse(CreateCourseRequest request) {
        Course course = new Course();
        course.setName(request.name());
        course.setDescription(request.description());
        course.setActive(request.active());

        return CourseDTO.from(courseRepository.save(course));
    }

    @Override
    @Transactional
    public CourseDTO updateCourse(UUID publicId, UpdateCourseRequest request) {
        Course course = courseRepository.getByPublicIdOrThrow(publicId);

        if (request.name() != null) {
            course.setName(request.name());
        }
        if (request.description() != null) {
            course.setDescription(request.description());
        }
        if (request.active() != null) {
            course.setActive(request.active());
        }

        return CourseDTO.from(courseRepository.save(course));
    }

    @Override
    @Transactional
    public void deleteCourse(UUID publicId) {
        Course course = courseRepository.getByPublicIdOrThrow(publicId);

        if (subjectRepository.existsByCourseId(course.getId())) {
            throw new ConflictException(
                    "COURSE_HAS_SUBJECTS",
                    "Cannot delete course with associated subjects. publicId: " + publicId);
        }

        course.markAsDeleted();
        courseRepository.save(course);
    }
}
