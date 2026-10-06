package org.example.identityservice.service;

import org.example.identityservice.dto.request.*;
import org.example.identityservice.dto.response.*;
import org.example.identityservice.enums.Role;
import org.example.identityservice.enums.UserStatus;
import org.example.identityservice.exception.InvalidStaffRoleException;
import org.example.identityservice.exception.UserNotFoundException;
import org.example.identityservice.exception.UsernameAlreadyExistsException;
import org.example.identityservice.model.User;
import org.example.identityservice.repo.UserRepo;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class UserService implements ReactiveUserDetailsService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepo userRepo,
            PasswordEncoder passwordEncoder) {

        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<UserRegistrationResponseDTO> registerCustomer(
            RegisterRequestDTO request) {

        String username = request.username().trim();

        return userRepo.existsByUsername(username)
                .flatMap(usernameExists -> {

                    if (usernameExists) {
                        return Mono.error(
                                new UsernameAlreadyExistsException(
                                        "Username already exists: "
                                                + username
                                )
                        );
                    }

                    LocalDateTime currentTime =
                            LocalDateTime.now();

                    User user = new User();

                    user.setUsername(username);
                    user.setPassword(
                            passwordEncoder.encode(
                                    request.password()
                            )
                    );
                    user.setRole(Role.CUSTOMER);
                    user.setCustomerId(null);
                    user.setStatus(UserStatus.ACTIVE);
                    user.setCreatedAt(currentTime);
                    user.setUpdatedAt(currentTime);

                    return userRepo.save(user);
                })
                .map(savedUser ->
                        new UserRegistrationResponseDTO(
                                savedUser.getUserId(),
                                savedUser.getUsername(),
                                savedUser.getRole(),
                                savedUser.getCustomerId(),
                                savedUser.getStatus()
                        )
                );
    }

    @Override
    public Mono<UserDetails> findByUsername(
            String username) {

        return userRepo.findByUsername(username)
                .switchIfEmpty(
                        Mono.error(
                                new UsernameNotFoundException(
                                        "User not found: "
                                                + username
                                )
                        )
                )
                .map(user ->
                        org.springframework.security
                                .core.userdetails.User
                                .builder()
                                .username(user.getUsername())
                                .password(user.getPassword())
                                .roles(user.getRole().name())
                                .disabled(
                                        user.getStatus()
                                                != UserStatus.ACTIVE
                                )
                                .build()
                );
    }

    public Mono<User> findUserByUsername(
            String username) {

        return userRepo.findByUsername(username)
                .switchIfEmpty(
                        Mono.error(
                                new UsernameNotFoundException(
                                        "User not found: "
                                                + username
                                )
                        )
                );
    }

    public Mono<UserProfileResponseDTO> getProfile(
            String username) {

        return findUserByUsername(username)
                .map(this::convertToProfileResponse);
    }

    public Mono<UserProfileResponseDTO> updateProfile(
            String currentUsername,
            UpdateProfileRequestDTO request) {

        return findUserByUsername(currentUsername)
                .flatMap(existingUser -> {

                    String newUsername =
                            request.username().trim();

                    if (existingUser.getUsername()
                            .equals(newUsername)) {

                        return Mono.just(existingUser);
                    }

                    return userRepo
                            .existsByUsername(newUsername)
                            .flatMap(usernameExists -> {

                                if (usernameExists) {
                                    return Mono.error(
                                            new UsernameAlreadyExistsException(
                                                    "Username already exists: "
                                                            + newUsername
                                            )
                                    );
                                }

                                existingUser.setUsername(
                                        newUsername
                                );

                                existingUser.setUpdatedAt(
                                        LocalDateTime.now()
                                );

                                return userRepo.save(
                                        existingUser
                                );
                            });
                })
                .map(this::convertToProfileResponse);
    }

    public Mono<UserResponseDTO> createStaffUser(
            CreateStaffUserRequestDTO request) {

        if (request.role() != Role.UNDERWRITER
                && request.role()
                != Role.CLAIMS_ADJUSTER) {

            return Mono.error(
                    new InvalidStaffRoleException(
                            "Staff role must be UNDERWRITER "
                                    + "or CLAIMS_ADJUSTER"
                    )
            );
        }

        String username = request.username().trim();

        return userRepo.existsByUsername(username)
                .flatMap(usernameExists -> {

                    if (usernameExists) {
                        return Mono.error(
                                new UsernameAlreadyExistsException(
                                        "Username already exists: "
                                                + username
                                )
                        );
                    }

                    LocalDateTime currentTime =
                            LocalDateTime.now();

                    User staffUser = new User();

                    staffUser.setUsername(username);
                    staffUser.setPassword(
                            passwordEncoder.encode(
                                    request.password()
                            )
                    );
                    staffUser.setRole(request.role());
                    staffUser.setCustomerId(null);
                    staffUser.setStatus(UserStatus.ACTIVE);
                    staffUser.setCreatedAt(currentTime);
                    staffUser.setUpdatedAt(currentTime);

                    return userRepo.save(staffUser);
                })
                .map(this::convertToUserResponse);
    }

    public Flux<UserResponseDTO> getAllUsers() {

        return userRepo.findAll()
                .map(this::convertToUserResponse);
    }

    public Mono<UserDetailsResponseDTO> getUserById(
            Long userId) {

        return userRepo.findById(userId)
                .switchIfEmpty(
                        Mono.error(
                                new UserNotFoundException(
                                        "User not found with ID: "
                                                + userId
                                )
                        )
                )
                .map(this::convertToUserDetailsResponse);
    }

    public Mono<UserResponseDTO> updateUserStatus(
            Long userId,
            UserStatusRequestDTO request) {

        return userRepo.findById(userId)
                .switchIfEmpty(
                        Mono.error(
                                new UserNotFoundException(
                                        "User not found with ID: "
                                                + userId
                                )
                        )
                )
                .flatMap(user -> {

                    user.setStatus(request.status());
                    user.setUpdatedAt(LocalDateTime.now());

                    return userRepo.save(user);
                })
                .map(this::convertToUserResponse);
    }


    public Mono<UserResponseDTO> linkCustomerProfile(
            Long userId,
            CustomerLinkRequestDTO request) {

        return userRepo.findById(userId)
                .switchIfEmpty(
                        Mono.error(
                                new UserNotFoundException(
                                        "User not found with ID: "
                                                + userId
                                )
                        )
                )
                .flatMap(user -> {

                    if (user.getRole() != Role.CUSTOMER) {
                        return Mono.error(
                                new InvalidStaffRoleException(
                                        "Customer profile can only be linked "
                                                + "to a CUSTOMER user"
                                )
                        );
                    }

                    user.setCustomerId(request.customerId());
                    user.setUpdatedAt(LocalDateTime.now());

                    return userRepo.save(user);
                })
                .map(this::convertToUserResponse);
    }

    public Mono<UserRoleValidationResponseDTO> validateUserRole(
            Long userId,
            Role requiredRole) {

        return userRepo.findById(userId)
                .switchIfEmpty(
                        Mono.error(
                                new UserNotFoundException(
                                        "User not found with ID: "
                                                + userId
                                )
                        )
                )
                .map(user -> {

                    boolean valid =
                            user.getRole() == requiredRole
                                    && user.getStatus()
                                    == UserStatus.ACTIVE;

                    return new UserRoleValidationResponseDTO(
                            user.getUserId(),
                            user.getUsername(),
                            user.getRole(),
                            user.getStatus(),
                            valid
                    );
                });
    }



    private UserProfileResponseDTO convertToProfileResponse(
            User user) {

        return new UserProfileResponseDTO(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                user.getCustomerId(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private UserResponseDTO convertToUserResponse(
            User user) {

        return new UserResponseDTO(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                user.getCustomerId(),
                user.getStatus()
        );
    }

    private UserDetailsResponseDTO
    convertToUserDetailsResponse(User user) {

        return new UserDetailsResponseDTO(
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                user.getCustomerId(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public Mono<Void> linkCustomer(
            Long userId,
            Long customerId) {

        return userRepo
                .findById(userId)
                .flatMap(user -> {

                    user.setCustomerId(customerId);

                    return userRepo.save(user);
                })
                .then();
    }
}