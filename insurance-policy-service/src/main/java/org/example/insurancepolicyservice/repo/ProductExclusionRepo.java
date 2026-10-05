package org.example.insurancepolicyservice.repo;

// Provides reactive access to insurance-product exclusions.
// It is needed to identify conditions under which a claim is not covered.

import org.example.insurancepolicyservice.model.ProductExclusion;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductExclusionRepo
        extends ReactiveCrudRepository<ProductExclusion, Long> {

    Flux<ProductExclusion> findAllByProductId(
            Long productId
    );

    Flux<ProductExclusion> findAllByProductIdAndActiveTrue(
            Long productId
    );

    Mono<ProductExclusion> findByProductIdAndExclusionCode(
            Long productId,
            String exclusionCode
    );

    Mono<Boolean> existsByProductIdAndExclusionCode(
            Long productId,
            String exclusionCode
    );

    Mono<Void> deleteAllByProductId(
            Long productId
    );
}