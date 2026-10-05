package org.example.insurancepolicyservice.repo;

// Provides reactive database operations for issued insurance policies.
// It is needed for Customer views and policy validation during claims.

import org.example.insurancepolicyservice.enums.PolicyStatus;
import org.example.insurancepolicyservice.model.Policy;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PolicyRepo
        extends ReactiveCrudRepository<Policy, Long> {

    Flux<Policy> findAllByCustomerId(
            Long customerId
    );

    Flux<Policy> findAllByStatus(
            PolicyStatus status
    );

    Mono<Policy> findByPolicyIdAndCustomerId(
            Long policyId,
            Long customerId
    );

    Mono<Policy> findByApplicationId(
            Long applicationId
    );

    Mono<Policy> findFirstByTwinIdAndStatus(
            Long twinId,
            PolicyStatus status
    );

    Mono<Boolean> existsByApplicationId(
            Long applicationId
    );

    Mono<Boolean> existsByTwinIdAndStatus(
            Long twinId,
            PolicyStatus status
    );
}