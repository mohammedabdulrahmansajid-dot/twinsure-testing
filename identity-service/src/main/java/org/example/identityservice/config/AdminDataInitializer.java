package org.example.identityservice.config;

import org.example.identityservice.enums.Role;
import org.example.identityservice.enums.UserStatus;
import org.example.identityservice.model.User;
import org.example.identityservice.repo.UserRepo;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
public class AdminDataInitializer implements ApplicationRunner {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public AdminDataInitializer(
            UserRepo userRepo,
            PasswordEncoder passwordEncoder) {

        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {

        String adminUsername = "admin";

        userRepo.existsByUsername(adminUsername)

                .flatMap(adminExists -> {

                    if (adminExists) {
                        return Mono.empty();
                    }

                    LocalDateTime currentTime =
                            LocalDateTime.now();

                    User admin = new User();

                    admin.setUsername(adminUsername);

                    admin.setPassword(
                            passwordEncoder.encode(
                                    "admin123"
                            )
                    );

                    admin.setRole(Role.ADMIN);
                    admin.setCustomerId(null);
                    admin.setStatus(UserStatus.ACTIVE);
                    admin.setCreatedAt(currentTime);
                    admin.setUpdatedAt(currentTime);

                    return userRepo.save(admin);
                })

                .doOnNext(admin ->
                        System.out.println(
                                "Default Admin created: "
                                        + admin.getUsername()
                        )
                )

                .doOnError(error ->
                        System.err.println(
                                "Failed to create default Admin: "
                                        + error.getMessage()
                        )
                )

                .subscribe();
    }
}