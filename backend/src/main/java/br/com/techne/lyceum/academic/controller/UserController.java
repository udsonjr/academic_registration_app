package br.com.techne.lyceum.academic.controller;

import br.com.techne.lyceum.academic.dto.CreateUserRequest;
import br.com.techne.lyceum.academic.dto.UpdateUserRequest;
import br.com.techne.lyceum.academic.dto.UserDTO;
import br.com.techne.lyceum.academic.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List users", description = "Returns all registered users (ADMIN only)")
    public List<UserDTO> getUsers() {
        return userService.getUsers();
    }

    @GetMapping(value = "/{publicId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get user",
            description = "Returns a user by publicId (self or ADMIN)")
    public UserDTO getUser(@PathVariable UUID publicId) {
        return userService.getUserByPublicId(publicId);
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create user",
            description = "Creates a new user with optional role (ADMIN only)")
    public UserDTO createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @PatchMapping(
            value = "/{publicId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Update user",
            description = "Partially updates a user (self or ADMIN)")
    public UserDTO updateUser(
            @PathVariable UUID publicId, @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateUser(publicId, request);
    }

    @DeleteMapping("/{publicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete user",
            description = "Soft-deletes a user (self or ADMIN)")
    public void deleteUser(@PathVariable UUID publicId) {
        userService.deleteUser(publicId);
    }
}
