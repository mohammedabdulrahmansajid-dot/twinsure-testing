package org.example.claimsservice.repo;

// Provides reactive access to claim evidence metadata.
// Evidence is returned newest first so recently submitted Customer
// documents appear first during Claims Adjuster review.

import org.example.claimsservice.enums.DocumentType;
import org.example.claimsservice.model.ClaimDocument;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClaimDocumentRepo
        extends ReactiveCrudRepository<ClaimDocument, Long> {

    Flux<ClaimDocument> findAllByClaimIdOrderByUploadedAtDesc(
            Long claimId
    );

    Flux<ClaimDocument>
    findAllByClaimIdAndDocumentTypeOrderByUploadedAtDesc(
            Long claimId,
            DocumentType documentType
    );

    Mono<ClaimDocument> findByDocumentIdAndClaimId(
            Long documentId,
            Long claimId
    );

    Mono<Boolean> existsByClaimIdAndDocumentReference(
            Long claimId,
            String documentReference
    );

    Mono<Void> deleteAllByClaimId(
            Long claimId
    );
}