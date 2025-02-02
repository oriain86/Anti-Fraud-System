package antifraud.service;

import antifraud.dto.*;

import java.util.List;

public interface UserService {
    UserRegistrationResponse registerUser(UserRegistrationRequest request);
    List<UserResponse> listUsers();
    UserDeletionResponse deleteUser(String username);

    UserResponse changeRole(RoleRequest request);
    String changeAccess(AccessRequest request); // returns a status message
}
