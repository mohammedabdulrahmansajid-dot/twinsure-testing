package org.example.identityservice.repo;

import org.example.identityservice.enums.Role;
import org.example.identityservice.enums.UserStatus;
import org.example.identityservice.model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepo
        extends ReactiveCrudRepository<User, Long> {

    Mono<User> findByUsername(String username);

    Mono<Boolean> existsByUsername(String username);

    Mono<User> findByUserIdAndStatus(
            Long userId,
            UserStatus status
    );

    Flux<User> findAllByRole(Role role);

    Flux<User> findAllByStatus(UserStatus status);

    Mono<Boolean> existsByCustomerId(Long customerId);
}