package org.example.claimsservice.repo;

// Provides reactive access to Claims Adjuster decision history.
// Decision records are returned newest first for review timelines
// while preserving every previous workflow transition.

import org.example.claimsservice.enums.ClaimDecisionType;
import org.example.claimsservice.model.ClaimDecision;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ClaimDecisionRepo
        extends ReactiveCrudRepository<ClaimDecision, Long> {

    Flux<ClaimDecision> findAllByClaimIdOrderByDecidedAtDesc(
            Long claimId
    );

    Flux<ClaimDecision> findAllByAdjusterIdOrderByDecidedAtDesc(
            Long adjusterId
    );

    Flux<ClaimDecision>
    findAllByClaimIdAndDecisionTypeOrderByDecidedAtDesc(
            Long claimId,
            ClaimDecisionType decisionType
    );
}