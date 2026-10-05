package org.example.identityservice.service;

import org.example.identityservice.dto.request.CreateStaffUserRequestDTO;
import org.example.identityservice.dto.request.RegisterRequestDTO;
import org.example.identityservice.dto.request.UserStatusRequestDTO;
import org.example.identityservice.dto.response.UserDetailsResponseDTO;
import org.example.identityservice.dto.response.UserRegistrationResponseDTO;
import org.example.identityservice.dto.response.UserResponseDTO;
import org.example.identityservice.enums.Role;
import org.example.identityservice.enums.UserStatus;
import org.example.identityservice.exception.InvalidStaffRoleException;
import org.example.identityservice.exception.UserNotFoundException;
import org.example.identityservice.exception.UsernameAlreadyExistsException;
import org.example.identityservice.model.User;
import org.example.identityservice.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepo, passwordEncoder);

        sampleUser = new User();
        sampleUser.setUserId(1L);
        sampleUser.setUsername("john_doe");
        sampleUser.setPassword("encoded_pass");
        sampleUser.setRole(Role.CUSTOMER);
        sampleUser.setStatus(UserStatus.ACTIVE);
        sampleUser.setCreatedAt(LocalDateTime.now());
        sampleUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void registerCustomer_shouldSuccessfullyRegisterNewUser() {
        RegisterRequestDTO request = new RegisterRequestDTO("john_doe", "SecurePassword123!");

        when(userRepo.existsByUsername("john_doe")).thenReturn(Mono.just(false));
        when(passwordEncoder.encode("SecurePassword123!")).thenReturn("encoded_pass");
        when(userRepo.save(any(User.class))).thenReturn(Mono.just(sampleUser));

        UserRegistrationResponseDTO response = userService.registerCustomer(request).block();

        assertNotNull(response);
        assertEquals(1L, response.userId());
        assertEquals("john_doe", response.username());
        assertEquals(Role.CUSTOMER, response.role());
        assertEquals(UserStatus.ACTIVE, response.status());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(userCaptor.capture());
        assertEquals("john_doe", userCaptor.getValue().getUsername());
        assertEquals("encoded_pass", userCaptor.getValue().getPassword());
    }

    @Test
    void registerCustomer_shouldThrowExceptionWhenUsernameExists() {
        RegisterRequestDTO request = new RegisterRequestDTO("existing_user", "Password123!");

        when(userRepo.existsByUsername("existing_user")).thenReturn(Mono.just(true));

        assertThrows(UsernameAlreadyExistsException.class, () -> {
            userService.registerCustomer(request).block();
        });

        verify(userRepo, never()).save(any());
    }

    @Test
    void findByUsername_shouldReturnUserDetailsWhenFound() {
        when(userRepo.findByUsername("john_doe")).thenReturn(Mono.just(sampleUser));

        UserDetails userDetails = userService.findByUsername("john_doe").block();

        assertNotNull(userDetails);
        assertEquals("john_doe", userDetails.getUsername());
        assertEquals("encoded_pass", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void findByUsername_shouldThrowExceptionWhenNotFound() {
        when(userRepo.findByUsername("unknown")).thenReturn(Mono.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.findByUsername("unknown").block();
        });
    }

    @Test
    void createStaffUser_shouldCreateUnderwriterSuccessfully() {
        CreateStaffUserRequestDTO request = new CreateStaffUserRequestDTO("uw_user", "Password123!", Role.UNDERWRITER);

        User uwUser = new User();
        uwUser.setUserId(2L);
        uwUser.setUsername("uw_user");
        uwUser.setPassword("encoded_uw");
        uwUser.setRole(Role.UNDERWRITER);
        uwUser.setStatus(UserStatus.ACTIVE);

        when(userRepo.existsByUsername("uw_user")).thenReturn(Mono.just(false));
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded_uw");
        when(userRepo.save(any(User.class))).thenReturn(Mono.just(uwUser));

        UserResponseDTO response = userService.createStaffUser(request).block();

        assertNotNull(response);
        assertEquals(2L, response.userId());
        assertEquals("uw_user", response.username());
        assertEquals(Role.UNDERWRITER, response.role());
    }

    @Test
    void createStaffUser_shouldRejectCustomerRole() {
        CreateStaffUserRequestDTO request = new CreateStaffUserRequestDTO("invalid_staff", "Password123!", Role.CUSTOMER);

        assertThrows(InvalidStaffRoleException.class, () -> {
            userService.createStaffUser(request).block();
        });
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        when(userRepo.findAll()).thenReturn(Flux.just(sampleUser));

        List<UserResponseDTO> users = userService.getAllUsers().collectList().block();

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("john_doe", users.get(0).username());
    }

    @Test
    void getUserById_shouldReturnUserDetails() {
        when(userRepo.findById(1L)).thenReturn(Mono.just(sampleUser));

        UserDetailsResponseDTO response = userService.getUserById(1L).block();

        assertNotNull(response);
        assertEquals(1L, response.userId());
    }

    @Test
    void getUserById_shouldThrowUserNotFoundException() {
        when(userRepo.findById(99L)).thenReturn(Mono.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(99L).block();
        });
    }

    @Test
    void updateUserStatus_shouldUpdateUserStatus() {
        UserStatusRequestDTO request = new UserStatusRequestDTO(UserStatus.SUSPENDED);
        User suspendedUser = new User();
        suspendedUser.setUserId(1L);
        suspendedUser.setUsername("john_doe");
        suspendedUser.setRole(Role.CUSTOMER);
        suspendedUser.setStatus(UserStatus.SUSPENDED);

        when(userRepo.findById(1L)).thenReturn(Mono.just(sampleUser));
        when(userRepo.save(any(User.class))).thenReturn(Mono.just(suspendedUser));

        UserResponseDTO response = userService.updateUserStatus(1L, request).block();

        assertNotNull(response);
        assertEquals(UserStatus.SUSPENDED, response.status());
    }
}
