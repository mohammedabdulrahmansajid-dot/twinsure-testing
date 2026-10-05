package org.example.insurancepolicyservice.repo;

// Stores and retrieves Customer policy applications during underwriting.
// It is needed for Customer tracking and Underwriter work queues.

import org.example.insurancepolicyservice.enums.PolicyApplicationStatus;
import org.example.insurancepolicyservice.model.PolicyApplication;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PolicyApplicationRepo
        extends ReactiveCrudRepository<PolicyApplication, Long> {

    Flux<PolicyApplication> findAllByCustomerId(
            Long customerId
    );

    Flux<PolicyApplication> findAllByStatus(
            PolicyApplicationStatus status
    );

    Flux<PolicyApplication> findAllByReviewedBy(
            Long reviewedBy
    );

    Mono<PolicyApplication> findByApplicationIdAndCustomerId(
            Long applicationId,
            Long customerId
    );

    Mono<Boolean> existsByTwinIdAndProductIdAndStatus(
            Long twinId,
            Long productId,
            PolicyApplicationStatus status
    );
}