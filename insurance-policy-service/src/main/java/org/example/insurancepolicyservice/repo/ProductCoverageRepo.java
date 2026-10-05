package org.example.insurancepolicyservice.repo;

// Provides reactive access to the coverage rules of an insurance product.
// It is needed to determine which AI actions and violations are insured.

import org.example.insurancepolicyservice.enums.ActionType;
import org.example.insurancepolicyservice.enums.ViolationType;
import org.example.insurancepolicyservice.model.ProductCoverage;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductCoverageRepo
        extends ReactiveCrudRepository<ProductCoverage, Long> {

    Flux<ProductCoverage> findAllByProductId(
            Long productId
    );

    Flux<ProductCoverage> findAllByProductIdAndActiveTrue(
            Long productId
    );

    Mono<ProductCoverage>
    findByProductIdAndActionTypeAndViolationType(
            Long productId,
            ActionType actionType,
            ViolationType violationType
    );

    Mono<Boolean>
    existsByProductIdAndActionTypeAndViolationType(
            Long productId,
            ActionType actionType,
            ViolationType violationType
    );

    Mono<Void> deleteAllByProductId(
            Long productId
    );
}