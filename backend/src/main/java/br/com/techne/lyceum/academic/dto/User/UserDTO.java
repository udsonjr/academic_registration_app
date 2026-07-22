package br.com.techne.lyceum.academic.dto;

import br.com.techne.lyceum.academic.domain.User;
import br.com.techne.lyceum.academic.domain.UserRole;
import java.util.UUID;

public record UserDTO(UUID publicId, String name, String email, UserRole role) {

    public static UserDTO from(User user) {
        return new UserDTO(user.getPublicId(), user.getName(), user.getEmail(), user.getRole());
    }
}
