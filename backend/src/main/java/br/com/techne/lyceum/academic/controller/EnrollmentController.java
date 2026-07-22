package br.com.techne.lyceum.academic.controller;

import br.com.techne.lyceum.academic.dto.CreateEnrollmentRequest;
import br.com.techne.lyceum.academic.dto.EnrollmentDTO;
import br.com.techne.lyceum.academic.dto.PageResponse;
import br.com.techne.lyceum.academic.security.SecurityUtils;
import br.com.techne.lyceum.academic.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
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
@SecurityRequirement(name = "bearerAuth")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "List enrollments",
            description =
                    "ADMIN: all enrollments, optionally filtered. STUDENT: only own enrollments.")
    public PageResponse<EnrollmentDTO> getEnrollments(
            @RequestParam(required = false) UUID userPublicId,
            @RequestParam(required = false) UUID classGroupPublicId,
            @PageableDefault(size = 10) Pageable pageable) {
        if (!SecurityUtils.isAdmin()) {
            return enrollmentService.getEnrollmentsByUser(
                    SecurityUtils.currentUserPublicId(), pageable);
        }
        if (userPublicId != null) {
            return enrollmentService.getEnrollmentsByUser(userPublicId, pageable);
        }
        if (classGroupPublicId != null) {
            return enrollmentService.getEnrollmentsByClassGroup(classGroupPublicId, pageable);
        }
        return enrollmentService.getEnrollments(pageable);
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
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Confirm enrollment",
            description =
                    "Confirms a pending enrollment and consumes a class group vacancy (ADMIN)")
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
