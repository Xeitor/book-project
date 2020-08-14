package com.karankumar.bookproject.backend.service;

import com.karankumar.bookproject.backend.entity.account.Role;
import com.karankumar.bookproject.backend.entity.account.User;
import com.karankumar.bookproject.backend.repository.RoleRepository;
import com.karankumar.bookproject.backend.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(User user) throws UserAlreadyRegisteredException {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<User>> constraintViolations = validator.validate(user);

        if (!constraintViolations.isEmpty()) {
            String errors = constraintViolations
                    .stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(Collectors.joining());
            throw new BadCredentialsException(errors);
        }

        if (usernameIsInUse(user.getUsername())) {
            throw new UserAlreadyRegisteredException(
                    "The username " + user.getUsername() + " is already taken");
        }

        if (emailIsInUse(user.getEmail())) {
            throw new UserAlreadyRegisteredException(
                    "A user with the email address " + user.getUsername() + " already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);
        Role userRole = roleRepository.findByRole("USER")
                                      .orElseThrow(() -> new AuthenticationServiceException(
                                              "The default user role could not be found"));
        user.setRoles(Set.of(userRole));
        userRepository.save(user);
    }

    public void delete(User user) {
        userRepository.delete(user);
    }

    public boolean usernameIsInUse(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean usernameIsNotInUse(String username) {
        return !usernameIsInUse(username);
    }

    public boolean emailIsInUse(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean emailIsNotInUse(String email) {
        return !emailIsInUse(email);
    }
}
