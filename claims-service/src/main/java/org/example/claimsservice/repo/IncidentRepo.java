package org.example.claimsservice.repo;

// Provides reactive database operations for Customer-reported incidents.
// It supports ownership checks, duplicate action detection, and incident history.

import org.example.claimsservice.enums.IncidentStatus;
import org.example.claimsservice.model.Incident;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IncidentRepo
        extends ReactiveCrudRepository<Incident, Long> {

    Flux<Incident> findAllByCustomerId(
            Long customerId
    );

    Flux<Incident> findAllByStatus(
            IncidentStatus status
    );

    Mono<Incident> findByIncidentIdAndCustomerId(
            Long incidentId,
            Long customerId
    );

    Mono<Incident> findByActionId(
            Long actionId
    );

    Mono<Boolean> existsByActionId(
            Long actionId
    );
}