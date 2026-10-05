package org.example.claimsservice.repo;

// Provides reactive database operations for formal insurance claims.
// Customer and Adjuster lists are returned newest first so recent
// claim activity remains visible at the top of each workspace.

import org.example.claimsservice.enums.ClaimStatus;
import org.example.claimsservice.model.Claim;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClaimRepo
        extends ReactiveCrudRepository<Claim, Long> {

    Flux<Claim> findAllByCustomerIdOrderBySubmittedAtDesc(
            Long customerId
    );

    Flux<Claim> findAllByStatusOrderByUpdatedAtDesc(
            ClaimStatus status
    );

    Flux<Claim> findAllByAssignedAdjusterIdOrderByUpdatedAtDesc(
            Long assignedAdjusterId
    );

    Mono<Claim> findByClaimIdAndCustomerId(
            Long claimId,
            Long customerId
    );

    Mono<Claim> findByClaimNumber(
            String claimNumber
    );

    Mono<Claim> findByIncidentId(
            Long incidentId
    );

    Mono<Boolean> existsByIncidentId(
            Long incidentId
    );

    Mono<Boolean> existsByClaimNumber(
            String claimNumber
    );
}
