package org.example.identityservice.controller;

// Exposes focused user validation to authenticated internal services.
// Claims Service uses this endpoint before assigning a claim.

import org.example.identityservice.dto.response.UserRoleValidationResponseDTO;
import org.example.identityservice.enums.Role;
import org.example.identityservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserService userService;

    public InternalUserController(
            UserService userService) {

        this.userService = userService;
    }

    @GetMapping("/{userId}/role-validation")
    public Mono<ResponseEntity<UserRoleValidationResponseDTO>>
    validateUserRole(
            @PathVariable
            Long userId,

            @RequestParam
            Role requiredRole) {

        return userService
                .validateUserRole(
                        userId,
                        requiredRole
                )
                .map(
                        ResponseEntity::ok
                );
    }
}