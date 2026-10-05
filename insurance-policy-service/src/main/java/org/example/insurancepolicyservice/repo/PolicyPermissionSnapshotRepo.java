package org.example.insurancepolicyservice.repo;

// Provides reactive access to the action permissions captured at issuance.
// Permission snapshots are returned consistently by action type.

import org.example.insurancepolicyservice.model.PolicyPermissionSnapshot;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface PolicyPermissionSnapshotRepo
        extends ReactiveCrudRepository<PolicyPermissionSnapshot, Long> {

    Flux<PolicyPermissionSnapshot>
    findAllByPolicyIdOrderByActionTypeAsc(
            Long policyId
    );
}