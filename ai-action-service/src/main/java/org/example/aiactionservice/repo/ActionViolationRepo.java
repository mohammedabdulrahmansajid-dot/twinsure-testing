package org.example.aiactionservice.repo;

// Provides reactive access to violations detected for AI actions.
// It is required because one action may contain multiple rule violations
// that must later be reviewed by Claims Service.

import org.example.aiactionservice.enums.ViolationType;
import org.example.aiactionservice.model.ActionViolation;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ActionViolationRepo
        extends ReactiveCrudRepository<ActionViolation, Long> {

    Flux<ActionViolation> findAllByActionId(
            Long actionId
    );

    Flux<ActionViolation> findAllByClaimEligibleTrue();

    Mono<ActionViolation> findByActionIdAndViolationType(
            Long actionId,
            ViolationType violationType
    );

    Mono<Boolean> existsByActionIdAndViolationType(
            Long actionId,
            ViolationType violationType
    );

    Mono<Void> deleteAllByActionId(
            Long actionId
    );
}
