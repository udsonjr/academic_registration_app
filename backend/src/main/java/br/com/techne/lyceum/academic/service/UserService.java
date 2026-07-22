package br.com.techne.lyceum.academic.service;

import br.com.techne.lyceum.academic.dto.CreateUserRequest;
import br.com.techne.lyceum.academic.dto.UpdateUserRequest;
import br.com.techne.lyceum.academic.dto.UserDTO;
import java.util.List;
import java.util.UUID;

public interface UserService {

    List<UserDTO> getUsers();

    UserDTO getUserByPublicId(UUID publicId);

    UserDTO createUser(CreateUserRequest request);

    UserDTO updateUser(UUID publicId, UpdateUserRequest request);

    void deleteUser(UUID publicId);
}
