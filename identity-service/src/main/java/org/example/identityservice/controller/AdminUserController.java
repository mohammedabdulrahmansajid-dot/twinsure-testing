package org.example.identityservice.controller;

import jakarta.validation.Valid;
import org.example.identityservice.dto.request.CreateStaffUserRequestDTO;
import org.example.identityservice.dto.request.UserStatusRequestDTO;
import org.example.identityservice.dto.response.UserDetailsResponseDTO;
import org.example.identityservice.dto.response.UserResponseDTO;
import org.example.identityservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(
            UserService userService) {

        this.userService = userService;
    }

    @PostMapping
    public Mono<ResponseEntity<UserResponseDTO>> createStaffUser(
            @Valid
            @RequestBody CreateStaffUserRequestDTO request) {

        return userService.createStaffUser(request)
                .map(response ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response)
                );
    }

    @GetMapping
    public Flux<UserResponseDTO> getAllUsers() {

        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public Mono<ResponseEntity<UserDetailsResponseDTO>> getUserById(
            @PathVariable Long userId) {

        return userService.getUserById(userId)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{userId}/status")
    public Mono<ResponseEntity<UserResponseDTO>> updateUserStatus(
            @PathVariable Long userId,

            @Valid
            @RequestBody UserStatusRequestDTO request) {

        return userService
                .updateUserStatus(userId, request)
                .map(ResponseEntity::ok);
    }
}
