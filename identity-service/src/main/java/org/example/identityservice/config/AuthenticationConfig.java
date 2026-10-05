package org.example.identityservice.config;

import org.example.identityservice.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthenticationConfig {

    @Bean
    public ReactiveAuthenticationManager
    reactiveAuthenticationManager(
            UserService userService,
            PasswordEncoder passwordEncoder) {

        UserDetailsRepositoryReactiveAuthenticationManager
                authenticationManager =
                new UserDetailsRepositoryReactiveAuthenticationManager(
                        userService
                );

        authenticationManager.setPasswordEncoder(
                passwordEncoder
        );

        return authenticationManager;
    }
}