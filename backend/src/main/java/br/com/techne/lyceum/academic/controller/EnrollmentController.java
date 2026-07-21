package br.com.techne.lyceum.academic.controller;

import br.com.techne.lyceum.academic.dto.CreateEnrollmentRequest;
import br.com.techne.lyceum.academic.dto.EnrollmentDTO;
import br.com.techne.lyceum.academic.service.EnrollmentService;
import br.com.techne.lyceum.academic.shared.exception.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollments", description = "Enrollment management")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "List enrollments",
            description = "Returns enrollments filtered by student or by class group")
    public List<EnrollmentDTO> getEnrollments(
            @RequestParam(required = false) UUID studentPublicId,
            @RequestParam(required = false) UUID classGroupPublicId) {
        if (studentPublicId != null) {
            return enrollmentService.getEnrollmentsByStudent(studentPublicId);
        }
        if (classGroupPublicId != null) {
            return enrollmentService.getEnrollmentsByClassGroup(classGroupPublicId);
        }
        throw new BadRequestException(
                "MISSING_ENROLLMENT_FILTER",
                "Either studentPublicId or classGroupPublicId must be provided");
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create enrollment",
            description = "Creates a new enrollment with PENDING status")
    public EnrollmentDTO createEnrollment(@Valid @RequestBody CreateEnrollmentRequest request) {
        return enrollmentService.createEnrollment(request);
    }

    @PatchMapping(value = "/{publicId}/confirm", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Confirm enrollment",
            description = "Confirms a pending enrollment and consumes a class group vacancy")
    public EnrollmentDTO confirmEnrollment(@PathVariable UUID publicId) {
        return enrollmentService.confirmEnrollment(publicId);
    }

    @PatchMapping(value = "/{publicId}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Cancel enrollment",
            description =
                    "Cancels an enrollment; releases the vacancy if the enrollment was confirmed")
    public EnrollmentDTO cancelEnrollment(@PathVariable UUID publicId) {
        return enrollmentService.cancelEnrollment(publicId);
    }
}
