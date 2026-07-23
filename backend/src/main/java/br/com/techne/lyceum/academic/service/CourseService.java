package br.com.techne.lyceum.academic.service;

import br.com.techne.lyceum.academic.dto.CourseDTO;
import br.com.techne.lyceum.academic.dto.CreateCourseRequest;
import br.com.techne.lyceum.academic.dto.PageResponse;
import br.com.techne.lyceum.academic.dto.UpdateCourseRequest;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface CourseService {

    PageResponse<CourseDTO> getCourses(String name, Boolean active, Pageable pageable);

    CourseDTO getCourseByPublicId(UUID publicId);

    CourseDTO createCourse(CreateCourseRequest request);

    CourseDTO updateCourse(UUID publicId, UpdateCourseRequest request);

    void deleteCourse(UUID publicId);
}
