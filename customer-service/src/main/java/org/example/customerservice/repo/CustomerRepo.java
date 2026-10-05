package org.example.customerservice.repo;

import org.example.customerservice.model.Customer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface CustomerRepo
        extends ReactiveCrudRepository<Customer, Long> {

    Mono<Customer> findByUserId(Long userId);

    Mono<Boolean> existsByUserId(Long userId);

    Mono<Boolean> existsByEmail(String email);
}