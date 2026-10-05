package org.example.insurancepolicyservice.repo;

// Provides reactive database operations for insurance products.
// It is needed to manage plans and retrieve active plans for Customers.

import org.example.insurancepolicyservice.enums.ProductStatus;
import org.example.insurancepolicyservice.model.InsuranceProduct;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InsuranceProductRepo
        extends ReactiveCrudRepository<InsuranceProduct, Long> {

    Mono<Boolean> existsByProductCode(
            String productCode
    );

    Mono<InsuranceProduct> findByProductCode(
            String productCode
    );

    Flux<InsuranceProduct> findAllByStatus(
            ProductStatus status
    );
}