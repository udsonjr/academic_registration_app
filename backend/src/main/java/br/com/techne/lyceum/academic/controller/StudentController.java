package br.com.techne.lyceum.academic.controller;

import br.com.techne.lyceum.academic.dto.CreateStudentRequest;
import br.com.techne.lyceum.academic.dto.StudentDTO;
import br.com.techne.lyceum.academic.dto.UpdateStudentRequest;
import br.com.techne.lyceum.academic.service.StudentService;
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
@RequestMapping("/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "Student management")
public class StudentController {

    private final StudentService studentService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List students", description = "Returns all registered students")
    public List<StudentDTO> getStudents() {
        return studentService.getStudents();
    }

    @GetMapping(value = "/{publicId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get student", description = "Returns a student by publicId")
    public StudentDTO getStudent(@PathVariable UUID publicId) {
        return studentService.getStudentByPublicId(publicId);
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create student", description = "Creates a new student")
    public StudentDTO createStudent(@Valid @RequestBody CreateStudentRequest request) {
        return studentService.createStudent(request);
    }

    @PatchMapping(
            value = "/{publicId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Update student",
            description = "Partially updates an existing student (only provided fields)")
    public StudentDTO updateStudent(
            @PathVariable UUID publicId, @Valid @RequestBody UpdateStudentRequest request) {
        return studentService.updateStudent(publicId, request);
    }

    @DeleteMapping("/{publicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete student", description = "Deletes a student by publicId")
    public void deleteStudent(@PathVariable UUID publicId) {
        studentService.deleteStudent(publicId);
    }
}
