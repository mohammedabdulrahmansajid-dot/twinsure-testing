package org.example.identityservice.controller;

import org.example.identityservice.dto.request.LoginRequestDTO;
import org.example.identityservice.dto.request.RegisterRequestDTO;
import org.example.identityservice.dto.response.AuthResponseDTO;
import org.example.identityservice.dto.response.UserRegistrationResponseDTO;
import org.example.identityservice.enums.Role;
import org.example.identityservice.enums.UserStatus;
import org.example.identityservice.security.JwtUtil;
import org.example.identityservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private RegisterRequestDTO registerRequest;
    private LoginRequestDTO loginRequest;
    private UserRegistrationResponseDTO registrationResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDTO("testuser", "Password123!");
        loginRequest = new LoginRequestDTO("testuser", "Password123!");
        registrationResponse = new UserRegistrationResponseDTO(
                1L, "testuser", Role.CUSTOMER, UserStatus.ACTIVE, LocalDateTime.now()
        );
    }

    @Test
    void register_shouldReturnCreatedStatus() {
        when(userService.registerCustomer(any(RegisterRequestDTO.class))).thenReturn(Mono.just(registrationResponse));

        Mono<ResponseEntity<UserRegistrationResponseDTO>> responseMono = authController.register(registerRequest);
        ResponseEntity<UserRegistrationResponseDTO> response = responseMono.block();

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody().userId());
    }

    @Test
    void login_shouldReturnAuthResponseWhenCredentialsValid() {
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        "testuser", "encodedPass", java.util.Collections.emptyList()
                );

        when(userService.findByUsername("testuser")).thenReturn(Mono.just(userDetails));
        when(passwordEncoder.matches("Password123!", "encodedPass")).thenReturn(true);
        when(jwtUtil.generateToken("testuser")).thenReturn("mock-jwt-token");

        Mono<ResponseEntity<AuthResponseDTO>> responseMono = authController.login(loginRequest);
        ResponseEntity<AuthResponseDTO> response = responseMono.block();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("mock-jwt-token", response.getBody().token());
    }
}
