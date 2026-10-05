package org.example.insurancepolicyservice.repo;

// Provides reactive access to policy-level AI Twin configuration snapshots.
// One immutable Twin snapshot is stored for each issued policy.

import org.example.insurancepolicyservice.model.PolicyTwinSnapshot;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PolicyTwinSnapshotRepo
        extends ReactiveCrudRepository<PolicyTwinSnapshot, Long> {

    Mono<PolicyTwinSnapshot> findByPolicyId(
            Long policyId
    );

    Mono<Boolean> existsByPolicyId(
            Long policyId
    );
}