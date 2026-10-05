package org.example.identityservice.controller;

import jakarta.validation.Valid;
import org.example.identityservice.dto.request.LoginRequestDTO;
import org.example.identityservice.dto.request.RegisterRequestDTO;
import org.example.identityservice.dto.response.LoginResponseDTO;
import org.example.identityservice.dto.response.UserRegistrationResponseDTO;
import org.example.identityservice.service.UserService;
import org.example.identityservice.utility.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final ReactiveAuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    private final String cookieName;
    private final boolean secureCookie;
    private final String sameSite;

    public AuthController(
            UserService userService,
            ReactiveAuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            @Value("${jwt.cookie.name}") String cookieName,
            @Value("${jwt.cookie.secure}") boolean secureCookie,
            @Value("${jwt.cookie.same-site}") String sameSite) {

        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.cookieName = cookieName;
        this.secureCookie = secureCookie;
        this.sameSite = sameSite;
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<UserRegistrationResponseDTO>>
    registerCustomer(
            @Valid
            @RequestBody RegisterRequestDTO request) {

        return userService.registerCustomer(request)
                .map(response ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response)
                );
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponseDTO>> login(
            @Valid
            @RequestBody LoginRequestDTO request) {

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                );

        return authenticationManager
                .authenticate(authenticationToken)

                .flatMap(authentication ->
                        userService.findUserByUsername(
                                request.username()
                        )
                )

                .map(user -> {

                    String jwtToken =
                            jwtUtil.generateToken(user);

                    ResponseCookie authenticationCookie =
                            ResponseCookie
                                    .from(cookieName, jwtToken)
                                    .httpOnly(true)
                                    .secure(secureCookie)
                                    .sameSite(sameSite)
                                    .path("/")
                                    .maxAge(Duration.ofHours(1))
                                    .build();

                    LoginResponseDTO response =
                            new LoginResponseDTO(
                                    user.getUserId(),
                                    user.getUsername(),
                                    user.getRole(),
                                    user.getCustomerId(),
                                    "Login successful"
                            );

                    return ResponseEntity
                            .ok()
                            .header(
                                    HttpHeaders.SET_COOKIE,
                                    authenticationCookie.toString()
                            )
                            .body(response);
                });
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<String>> logout() {

        ResponseCookie deletionCookie =
                ResponseCookie
                        .from(cookieName, "")
                        .httpOnly(true)
                        .secure(secureCookie)
                        .sameSite(sameSite)
                        .path("/")
                        .maxAge(Duration.ZERO)
                        .build();

        return Mono.just(
                ResponseEntity
                        .ok()
                        .header(
                                HttpHeaders.SET_COOKIE,
                                deletionCookie.toString()
                        )
                        .body("Logout successful")
        );
    }
}