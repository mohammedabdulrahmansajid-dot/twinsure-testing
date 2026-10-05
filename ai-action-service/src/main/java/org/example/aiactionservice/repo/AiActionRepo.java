package org.example.aiactionservice.repo;

// Provides reactive database operations for simulated AI actions.
// It is required for Customer history, duplicate-reference checks,
// and retrieving actions associated with a Twin or policy.

import org.example.aiactionservice.enums.ActionStatus;
import org.example.aiactionservice.model.AiAction;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AiActionRepo
        extends ReactiveCrudRepository<AiAction, Long> {

    Flux<AiAction> findAllByCustomerId(
            Long customerId
    );

    Flux<AiAction> findAllByTwinId(
            Long twinId
    );

    Flux<AiAction> findAllByPolicyId(
            Long policyId
    );

    Flux<AiAction> findAllByActionStatus(
            ActionStatus actionStatus
    );

    Mono<AiAction> findByActionIdAndCustomerId(
            Long actionId,
            Long customerId
    );

    Mono<AiAction> findByTransactionReference(
            String transactionReference
    );

    Mono<Boolean> existsByTransactionReference(
            String transactionReference
    );
}