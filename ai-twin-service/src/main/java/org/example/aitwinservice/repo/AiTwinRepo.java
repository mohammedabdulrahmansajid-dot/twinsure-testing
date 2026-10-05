package org.example.aitwinservice.repo;

import org.example.aitwinservice.enums.AiTwinStatus;
import org.example.aitwinservice.model.AiTwin;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AiTwinRepo
        extends ReactiveCrudRepository<AiTwin, Long> {

    Flux<AiTwin> findAllByCustomerId(Long customerId);

    Mono<AiTwin> findByTwinIdAndCustomerId(
            Long twinId,
            Long customerId
    );

    Mono<Boolean> existsByCustomerIdAndTwinName(
            Long customerId,
            String twinName
    );

    Flux<AiTwin> findAllByStatus(
            AiTwinStatus status
    );
}