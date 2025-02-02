package antifraud.service;

import antifraud.dto.*;
import antifraud.exception.UserAlreadyExistsException;
import antifraud.exception.UserNotFoundException;
import antifraud.model.Authority;
import antifraud.model.User;
import antifraud.repository.AuthorityRepository;
import antifraud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, AuthorityRepository authorityRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserRegistrationResponse registerUser(UserRegistrationRequest request) {
        String usernameLower = request.getUsername().toLowerCase();
        if (userRepository.existsByUsernameIgnoreCase(usernameLower)) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setUsername(usernameLower);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Check if any users exist; if not, this is the first user.
        if (userRepository.count() == 0) {
            // First user gets ADMINISTRATOR role and is unlocked.
            user.setEnabled(true);
            userRepository.save(user);
            Authority authority = new Authority(user, "ADMINISTRATOR");
            authorityRepository.save(authority);
        } else {
            // All other users get MERCHANT role and are locked by default.
            user.setEnabled(false);
            userRepository.save(user);
            Authority authority = new Authority(user, "MERCHANT");
            authorityRepository.save(authority);
        }

        // Build the response including the role.
        String role = user.getAuthorities().iterator().next().getAuthority();
        return new UserRegistrationResponse(user.getId(), user.getName(), user.getUsername(), role);
    }

    @Override
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .sorted((u1, u2) -> u1.getId().compareTo(u2.getId()))
                .map(user -> {
                    String role = user.getAuthorities().iterator().next().getAuthority();
                    return new UserResponse(user.getId(), user.getName(), user.getUsername(), role);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDeletionResponse deleteUser(String username) {
        String usernameLower = username.toLowerCase();
        Optional<User> optionalUser = userRepository.findByUsernameIgnoreCase(usernameLower);
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }

        User user = optionalUser.get();

        // Delete authorities first due to foreign key constraint
        authorityRepository.deleteAll(user.getAuthorities());
        userRepository.delete(user);
        return new UserDeletionResponse(usernameLower, "Deleted successfully!");
    }

    @Override
    @Transactional
    public UserResponse changeRole(RoleRequest request) {
        String usernameLower = request.getUsername().toLowerCase();
        Optional<User> optionalUser = userRepository.findByUsernameIgnoreCase(usernameLower);
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }
        User user = optionalUser.get();

        // ADMINISTRATOR cannot be changed to any other role.
        String currentRole = user.getAuthorities().iterator().next().getAuthority();
        if ("ADMINISTRATOR".equals(currentRole)) {
            throw new IllegalArgumentException("Cannot change role of ADMINISTRATOR");
        }

        // The only allowed roles to assign are MERCHANT and SUPPORT.
        String newRole = request.getRole().toUpperCase();
        if (!newRole.equals("MERCHANT") && !newRole.equals("SUPPORT")) {
            throw new IllegalArgumentException("Role must be either SUPPORT or MERCHANT");
        }
        if (currentRole.equals(newRole)) {
            throw new UserAlreadyExistsException("User already has this role");
        }

        // Remove the existing authority and assign the new one.
        authorityRepository.deleteAll(user.getAuthorities());
        Authority newAuthority = new Authority(user, newRole);
        authorityRepository.save(newAuthority);

        return new UserResponse(user.getId(), user.getName(), user.getUsername(), newRole);
    }

    @Override
    @Transactional
    public String changeAccess(AccessRequest request) {
        String usernameLower = request.getUsername().toLowerCase();
        Optional<User> optionalUser = userRepository.findByUsernameIgnoreCase(usernameLower);
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }
        User user = optionalUser.get();

        // For safety, ADMINISTRATOR cannot be blocked.
        String role = user.getAuthorities().iterator().next().getAuthority();
        if ("ADMINISTRATOR".equals(role)) {
            throw new IllegalArgumentException("Cannot lock/unlock ADMINISTRATOR");
        }

        String operation = request.getOperation().toUpperCase();
        if (operation.equals("LOCK")) {
            user.setEnabled(false);
            userRepository.save(user);
            return "User " + user.getUsername() + " locked!";
        } else if (operation.equals("UNLOCK")) {
            user.setEnabled(true);
            userRepository.save(user);
            return "User " + user.getUsername() + " unlocked!";
        } else {
            throw new IllegalArgumentException("Invalid operation: " + operation);
        }
    }
}
