package br.com.techne.lyceum.academic.service;

import br.com.techne.lyceum.academic.dto.CourseDTO;
import br.com.techne.lyceum.academic.dto.CreateCourseRequest;
import br.com.techne.lyceum.academic.dto.UpdateCourseRequest;
import java.util.List;
import java.util.UUID;

public interface CourseService {

    List<CourseDTO> getCourses();

    CourseDTO getCourseByPublicId(UUID publicId);

    CourseDTO createCourse(CreateCourseRequest request);

    CourseDTO updateCourse(UUID publicId, UpdateCourseRequest request);

    void deleteCourse(UUID publicId);
}
