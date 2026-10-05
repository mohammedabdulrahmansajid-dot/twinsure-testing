package org.example.identityservice.controller;

import jakarta.validation.Valid;
import org.example.identityservice.dto.request.UpdateProfileRequestDTO;
import org.example.identityservice.dto.response.UserProfileResponseDTO;
import org.example.identityservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(
            UserService userService) {

        this.userService = userService;
    }

    @GetMapping("/profile")
    public Mono<ResponseEntity<UserProfileResponseDTO>>
    getProfile(
            Authentication authentication) {

        String username =
                authentication.getName();

        return userService
                .getProfile(username)

                .map(ResponseEntity::ok);
    }

    @PutMapping("/profile")
    public Mono<ResponseEntity<UserProfileResponseDTO>>
    updateProfile(
            Authentication authentication,

            @Valid
            @RequestBody
            UpdateProfileRequestDTO request) {

        String currentUsername =
                authentication.getName();

        return userService
                .updateProfile(
                        currentUsername,
                        request
                )

                .map(ResponseEntity::ok);
    }
}
