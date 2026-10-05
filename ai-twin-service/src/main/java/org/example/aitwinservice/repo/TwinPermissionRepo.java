package org.example.aitwinservice.repo;

import org.example.aitwinservice.enums.ActionType;
import org.example.aitwinservice.model.TwinPermission;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TwinPermissionRepo
        extends ReactiveCrudRepository<TwinPermission, Long> {

    Flux<TwinPermission> findAllByTwinId(
            Long twinId
    );

    Flux<TwinPermission> findAllByTwinIdAndActiveTrue(
            Long twinId
    );

    Mono<TwinPermission> findByTwinIdAndActionType(
            Long twinId,
            ActionType actionType
    );

    Mono<Boolean> existsByTwinIdAndActionType(
            Long twinId,
            ActionType actionType
    );

    Mono<Void> deleteAllByTwinId(
            Long twinId
    );
}

