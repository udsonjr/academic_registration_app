package br.com.techne.lyceum.academic.controller;

import br.com.techne.lyceum.academic.dto.CourseDTO;
import br.com.techne.lyceum.academic.dto.CreateCourseRequest;
import br.com.techne.lyceum.academic.dto.UpdateCourseRequest;
import br.com.techne.lyceum.academic.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Course management")
public class CourseController {

    private final CourseService courseService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List courses", description = "Returns all registered courses")
    public List<CourseDTO> getCourses() {
        return courseService.getCourses();
    }

    @GetMapping(value = "/{publicId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get course", description = "Returns a course by publicId")
    public CourseDTO getCourse(@PathVariable UUID publicId) {
        return courseService.getCourseByPublicId(publicId);
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create course", description = "Creates a new course")
    public CourseDTO createCourse(@Valid @RequestBody CreateCourseRequest request) {
        return courseService.createCourse(request);
    }

    @PatchMapping(
            value = "/{publicId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Update course",
            description = "Partially updates an existing course (only provided fields)")
    public CourseDTO updateCourse(
            @PathVariable UUID publicId, @Valid @RequestBody UpdateCourseRequest request) {
        return courseService.updateCourse(publicId, request);
    }

    @DeleteMapping("/{publicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete course", description = "Deletes a course by publicId")
    public void deleteCourse(@PathVariable UUID publicId) {
        courseService.deleteCourse(publicId);
    }
}
