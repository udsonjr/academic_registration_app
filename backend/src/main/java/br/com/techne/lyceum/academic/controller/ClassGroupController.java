package br.com.techne.lyceum.academic.controller;

import br.com.techne.lyceum.academic.dto.ClassGroupDTO;
import br.com.techne.lyceum.academic.dto.CreateClassGroupRequest;
import br.com.techne.lyceum.academic.dto.PageResponse;
import br.com.techne.lyceum.academic.dto.UpdateClassGroupRequest;
import br.com.techne.lyceum.academic.service.ClassGroupService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/class-groups")
@RequiredArgsConstructor
@Tag(name = "Class Groups", description = "Class group management")
@SecurityRequirement(name = "bearerAuth")
public class ClassGroupController {

    private final ClassGroupService classGroupService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "List class groups",
            description = "Returns paginated class groups, optionally filtered by subject")
    public PageResponse<ClassGroupDTO> getClassGroups(
            @RequestParam(required = false) UUID subjectPublicId,
            @PageableDefault(size = 10) Pageable pageable) {
        return classGroupService.getClassGroups(subjectPublicId, pageable);
    }

    @GetMapping(value = "/{publicId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get class group", description = "Returns a class group by publicId")
    public ClassGroupDTO getClassGroup(@PathVariable UUID publicId) {
        return classGroupService.getClassGroupByPublicId(publicId);
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create class group", description = "Creates a new class group")
    public ClassGroupDTO createClassGroup(@Valid @RequestBody CreateClassGroupRequest request) {
        return classGroupService.createClassGroup(request);
    }

    @PatchMapping(
            value = "/{publicId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update class group",
            description = "Partially updates an existing class group (only provided fields)")
    public ClassGroupDTO updateClassGroup(
            @PathVariable UUID publicId, @Valid @RequestBody UpdateClassGroupRequest request) {
        return classGroupService.updateClassGroup(publicId, request);
    }

    @DeleteMapping("/{publicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete class group", description = "Deletes a class group by publicId")
    public void deleteClassGroup(@PathVariable UUID publicId) {
        classGroupService.deleteClassGroup(publicId);
    }
}
