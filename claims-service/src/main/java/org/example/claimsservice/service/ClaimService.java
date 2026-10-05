package org.example.claimsservice.service;

// Creates Customer-owned claims and manages supporting evidence.
// Claim lists, documents, and decision history are returned newest first,
// while ownership and workflow-state rules are enforced in the service.

import org.example.claimsservice.dto.request.AddClaimDocumentRequestDTO;
import org.example.claimsservice.dto.request.CreateClaimRequestDTO;
import org.example.claimsservice.dto.response.ClaimDecisionResponseDTO;
import org.example.claimsservice.dto.response.ClaimDetailsResponseDTO;
import org.example.claimsservice.dto.response.ClaimDocumentResponseDTO;
import org.example.claimsservice.dto.response.ClaimResponseDTO;
import org.example.claimsservice.dto.response.IncidentResponseDTO;
import org.example.claimsservice.enums.ClaimStatus;
import org.example.claimsservice.enums.IncidentStatus;
import org.example.claimsservice.exception.ClaimNotFoundException;
import org.example.claimsservice.exception.DuplicateClaimException;
import org.example.claimsservice.exception.DuplicateDocumentException;
import org.example.claimsservice.exception.IncidentNotFoundException;
import org.example.claimsservice.exception.InvalidClaimStateException;
import org.example.claimsservice.exception.InvalidRequestException;
import org.example.claimsservice.exception.ResourceAccessDeniedException;
import org.example.claimsservice.model.Claim;
import org.example.claimsservice.model.ClaimDecision;
import org.example.claimsservice.model.ClaimDocument;
import org.example.claimsservice.model.Incident;
import org.example.claimsservice.repo.ClaimDecisionRepo;
import org.example.claimsservice.repo.ClaimDocumentRepo;
import org.example.claimsservice.repo.ClaimRepo;
import org.example.claimsservice.repo.IncidentRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.example.claimsservice.dto.request.CreateNotificationRequestDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ClaimService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    ClaimService.class
            );

    private final ClaimRepo claimRepo;
    private final IncidentRepo incidentRepo;
    private final ClaimDocumentRepo documentRepo;
    private final ClaimDecisionRepo decisionRepo;
    private final WebClient.Builder webClientBuilder;

    public ClaimService(
            ClaimRepo claimRepo,
            IncidentRepo incidentRepo,
            ClaimDocumentRepo documentRepo,
            ClaimDecisionRepo decisionRepo,
            WebClient.Builder webClientBuilder) {

        this.claimRepo = claimRepo;
        this.incidentRepo = incidentRepo;
        this.documentRepo = documentRepo;
        this.decisionRepo = decisionRepo;
        this.webClientBuilder = webClientBuilder;
    }

    /*
     * CUSTOMER:
     * Creates one formal claim from a Customer-owned reported incident.
     *
     * Claim creation and incident status update are performed in one
     * transaction because both records belong to Claims Service.
     */
    @Transactional
    public Mono<ClaimResponseDTO> createClaim(
            Long customerId,
            CreateClaimRequestDTO request) {

        validateCustomerId(
                customerId
        );

        return incidentRepo
                .findByIncidentIdAndCustomerId(
                        request.incidentId(),
                        customerId
                )
                .switchIfEmpty(
                        Mono.error(
                                new IncidentNotFoundException(
                                        "Incident not found"
                                )
                        )
                )
                .flatMap(incident -> {

                    validateIncidentForClaim(
                            incident,
                            request.claimedAmount()
                    );

                    return claimRepo
                            .existsByIncidentId(
                                    incident.getIncidentId()
                            )
                            .flatMap(claimExists -> {

                                if (claimExists) {

                                    return Mono.error(
                                            new DuplicateClaimException(
                                                    "A formal claim has already "
                                                            + "been created for "
                                                            + "this incident"
                                            )
                                    );
                                }

                                return saveClaim(
                                        incident,
                                        request.claimedAmount()
                                );
                            })
                            .flatMap(savedClaim -> {

                                incident.setStatus(
                                        IncidentStatus
                                                .CLAIM_CREATED
                                );

                                return incidentRepo
                                        .save(incident)
                                        .thenReturn(
                                                savedClaim
                                        );
                            });
                })
                .map(
                        this::convertToClaimResponse
                )
                .doOnSuccess(response ->
                        LOGGER.info(
                                "Claim created: claimId={}, "
                                        + "claimNumber={}, incidentId={}, "
                                        + "customerId={}, policyId={}",
                                response.claimId(),
                                response.claimNumber(),
                                response.incidentId(),
                                response.customerId(),
                                response.policyId()
                        )
                );
    }

    /*
     * CUSTOMER:
     * Returns all claims belonging to the logged-in Customer.
     *
     * The newest submitted claim appears first.
     */
    public Flux<ClaimResponseDTO> getMyClaims(
            Long customerId) {

        validateCustomerId(
                customerId
        );

        return claimRepo
                .findAllByCustomerIdOrderBySubmittedAtDesc(
                        customerId
                )
                .map(
                        this::convertToClaimResponse
                );
    }

    /*
     * CUSTOMER OWNER / ASSIGNED CLAIMS ADJUSTER / ADMIN:
     * Returns the complete local claim record with incident,
     * evidence documents, and workflow decision history.
     */
    public Mono<ClaimDetailsResponseDTO> getClaimDetails(
            Long claimId,
            String role,
            Long customerId,
            Long userId) {

        return findClaimById(
                claimId
        )
                .flatMap(claim -> {

                    validateClaimAccess(
                            claim,
                            role,
                            customerId,
                            userId
                    );

                    return buildClaimDetails(
                            claim
                    );
                });
    }

    /*
     * CUSTOMER:
     * Adds evidence metadata to an owned claim.
     *
     * Evidence is accepted only while the claim remains open.
     * Finalized and closed claims remain read-only.
     */
// Adds Customer evidence while the claim remains open.
// The claim activity time is updated and the assigned Adjuster
// receives a notification that new evidence is available.

    public Mono<ClaimDocumentResponseDTO> addDocument(
            Long claimId,
            Long customerId,
            Long uploaderUserId,
            String jwtToken,
            AddClaimDocumentRequestDTO request) {

        validateCustomerId(
                customerId
        );

        if (uploaderUserId == null
                || uploaderUserId <= 0) {

            return Mono.error(
                    new InvalidRequestException(
                            "Authenticated user ID is required"
                    )
            );
        }

        if (jwtToken == null
                || jwtToken.isBlank()) {

            return Mono.error(
                    new InvalidRequestException(
                            "Authentication token is unavailable"
                    )
            );
        }

        String normalizedReference =
                request
                        .documentReference()
                        .trim();

        return claimRepo
                .findByClaimIdAndCustomerId(
                        claimId,
                        customerId
                )
                .switchIfEmpty(
                        Mono.error(
                                new ClaimNotFoundException(
                                        "Claim not found"
                                )
                        )
                )
                .flatMap(claim -> {

                    ensureDocumentsCanBeAdded(
                            claim
                    );

                    return documentRepo
                            .existsByClaimIdAndDocumentReference(
                                    claimId,
                                    normalizedReference
                            )
                            .flatMap(documentExists -> {

                                if (documentExists) {

                                    return Mono.error(
                                            new DuplicateDocumentException(
                                                    "This document reference "
                                                            + "has already been "
                                                            + "added to the claim"
                                            )
                                    );
                                }

                                LocalDateTime uploadedAt =
                                        LocalDateTime.now();

                                ClaimDocument document =
                                        new ClaimDocument();

                                document.setClaimId(
                                        claimId
                                );

                                document.setDocumentType(
                                        request.documentType()
                                );

                                document.setFileName(
                                        request
                                                .fileName()
                                                .trim()
                                );

                                document.setDocumentReference(
                                        normalizedReference
                                );

                                document.setDescription(
                                        normalizeOptionalText(
                                                request.description()
                                        )
                                );

                                document.setUploadedBy(
                                        uploaderUserId
                                );

                                document.setUploadedAt(
                                        uploadedAt
                                );

                                return documentRepo
                                        .save(document)
                                        .flatMap(savedDocument -> {

                                            claim.setUpdatedAt(
                                                    uploadedAt
                                            );

                                            return claimRepo
                                                    .save(claim)
                                                    .then(
                                                            notifyAssignedAdjuster(
                                                                    claim,
                                                                    savedDocument,
                                                                    jwtToken
                                                            )
                                                    )
                                                    .thenReturn(
                                                            savedDocument
                                                    );
                                        });
                            });
                })
                .map(
                        this::convertToDocumentResponse
                )
                .doOnSuccess(response ->
                        LOGGER.info(
                                "Claim document added: documentId={}, "
                                        + "claimId={}, documentType={}, "
                                        + "uploadedBy={}",
                                response.documentId(),
                                response.claimId(),
                                response.documentType(),
                                response.uploadedBy()
                        )
                );
    }

    /*
     * CUSTOMER OWNER / ASSIGNED CLAIMS ADJUSTER / ADMIN:
     * Returns evidence metadata newest first.
     */
    public Flux<ClaimDocumentResponseDTO> getClaimDocuments(
            Long claimId,
            String role,
            Long customerId,
            Long userId) {

        return findClaimById(
                claimId
        )
                .flatMapMany(claim -> {

                    validateClaimAccess(
                            claim,
                            role,
                            customerId,
                            userId
                    );

                    return documentRepo
                            .findAllByClaimIdOrderByUploadedAtDesc(
                                    claimId
                            );
                })
                .map(
                        this::convertToDocumentResponse
                );
    }

    /*
     * ADMIN:
     * Returns every formal claim.
     */
    public Flux<ClaimResponseDTO> getAllClaims() {

        return claimRepo
                .findAll()
                .sort(
                        (first, second) ->
                                compareDateTimesDescending(
                                        first.getUpdatedAt(),
                                        second.getUpdatedAt()
                                )
                )
                .map(
                        this::convertToClaimResponse
                );
    }

    /*
     * INTERNAL:
     * Returns a standard claim record to another authenticated service.
     */
    public Mono<ClaimResponseDTO> getInternalClaim(
            Long claimId) {

        return findClaimById(
                claimId
        )
                .map(
                        this::convertToClaimResponse
                );
    }

    /*
     * Saves a new claim in SUBMITTED status.
     */
    private Mono<Claim> saveClaim(
            Incident incident,
            BigDecimal claimedAmount) {

        LocalDateTime currentTime =
                LocalDateTime.now();

        Claim claim =
                new Claim();

        claim.setClaimNumber(
                generateClaimNumber()
        );

        claim.setIncidentId(
                incident.getIncidentId()
        );

        claim.setCustomerId(
                incident.getCustomerId()
        );

        claim.setTwinId(
                incident.getTwinId()
        );

        claim.setPolicyId(
                incident.getPolicyId()
        );

        claim.setActionId(
                incident.getActionId()
        );

        claim.setClaimedAmount(
                claimedAmount
        );

        claim.setApprovedAmount(
                null
        );

        claim.setDeductibleApplied(
                null
        );

        claim.setStatus(
                ClaimStatus.SUBMITTED
        );

        claim.setAssignedAdjusterId(
                null
        );

        claim.setSubmittedAt(
                currentTime
        );

        claim.setUpdatedAt(
                currentTime
        );

        claim.setClosedAt(
                null
        );

        return claimRepo.save(
                claim
        );
    }

    /*
     * Validates whether a reported incident can create a claim.
     */
    private void validateIncidentForClaim(
            Incident incident,
            BigDecimal claimedAmount) {

        if (incident.getStatus()
                != IncidentStatus.REPORTED) {

            throw new InvalidClaimStateException(
                    "Only an incident in REPORTED status "
                            + "can create a formal claim"
            );
        }

        if (claimedAmount == null
                || claimedAmount
                .compareTo(
                        BigDecimal.ZERO
                ) <= 0) {

            throw new InvalidRequestException(
                    "Claimed amount must be greater than zero"
            );
        }

        if (incident.getLossAmount()
                == null) {

            throw new InvalidRequestException(
                    "The incident loss amount is unavailable"
            );
        }

        if (claimedAmount.compareTo(
                incident.getLossAmount()
        ) > 0) {

            throw new InvalidRequestException(
                    "Claimed amount cannot be greater "
                            + "than the reported loss amount"
            );
        }
    }

    /*
     * Customers may view only their own claims.
     *
     * Claims Adjusters may view only claims assigned to them.
     *
     * Admins may view every claim.
     */
    private void validateClaimAccess(
            Claim claim,
            String role,
            Long customerId,
            Long userId) {

        if ("ADMIN".equals(role)) {
            return;
        }

        if ("CUSTOMER".equals(role)) {

            if (customerId == null
                    || !claim
                    .getCustomerId()
                    .equals(
                            customerId
                    )) {

                throw new ResourceAccessDeniedException(
                        "You cannot access another "
                                + "Customer's claim"
                );
            }

            return;
        }

        if ("CLAIMS_ADJUSTER".equals(role)) {

            if (userId == null
                    || claim
                    .getAssignedAdjusterId()
                    == null
                    || !claim
                    .getAssignedAdjusterId()
                    .equals(
                            userId
                    )) {

                throw new ResourceAccessDeniedException(
                        "This claim is not assigned "
                                + "to the authenticated "
                                + "Claims Adjuster"
                );
            }

            return;
        }

        throw new ResourceAccessDeniedException(
                "You are not allowed to access this claim"
        );
    }

    /*
     * Evidence is accepted only before the final claim decision.
     *
     * Allowed:
     * SUBMITTED, ASSIGNED, UNDER_REVIEW,
     * MORE_INFORMATION_REQUIRED.
     *
     * Blocked:
     * APPROVED, PARTIALLY_APPROVED, REJECTED, CLOSED.
     */
    private void ensureDocumentsCanBeAdded(
            Claim claim) {

        boolean documentsAllowed =
                claim.getStatus()
                        == ClaimStatus.SUBMITTED
                        || claim.getStatus()
                        == ClaimStatus.ASSIGNED
                        || claim.getStatus()
                        == ClaimStatus.UNDER_REVIEW
                        || claim.getStatus()
                        == ClaimStatus
                        .MORE_INFORMATION_REQUIRED;

        if (!documentsAllowed) {

            throw new InvalidClaimStateException(
                    "Documents can be added only while "
                            + "the claim is open for review"
            );
        }
    }

    /*
     * Builds the complete local claim details response.
     *
     * Documents and decisions are returned newest first.
     */
    private Mono<ClaimDetailsResponseDTO> buildClaimDetails(
            Claim claim) {

        Mono<Incident> incidentMono =
                incidentRepo
                        .findById(
                                claim.getIncidentId()
                        )
                        .switchIfEmpty(
                                Mono.error(
                                        new IncidentNotFoundException(
                                                "Incident linked to the "
                                                        + "claim was not found"
                                        )
                                )
                        );

        Mono<List<ClaimDocumentResponseDTO>>
                documentsMono =
                documentRepo
                        .findAllByClaimIdOrderByUploadedAtDesc(
                                claim.getClaimId()
                        )
                        .map(
                                this::convertToDocumentResponse
                        )
                        .collectList();

        Mono<List<ClaimDecisionResponseDTO>>
                decisionsMono =
                decisionRepo
                        .findAllByClaimIdOrderByDecidedAtDesc(
                                claim.getClaimId()
                        )
                        .map(
                                this::convertToDecisionResponse
                        )
                        .collectList();

        return Mono.zip(
                        incidentMono,
                        documentsMono,
                        decisionsMono
                )
                .map(result ->
                        new ClaimDetailsResponseDTO(
                                convertToClaimResponse(
                                        claim
                                ),
                                convertToIncidentResponse(
                                        result.getT1()
                                ),
                                result.getT2(),
                                result.getT3()
                        )
                );
    }

    private Mono<Claim> findClaimById(
            Long claimId) {

        if (claimId == null
                || claimId <= 0) {

            return Mono.error(
                    new InvalidRequestException(
                            "Claim ID must be a positive number"
                    )
            );
        }

        return claimRepo
                .findById(
                        claimId
                )
                .switchIfEmpty(
                        Mono.error(
                                new ClaimNotFoundException(
                                        "Claim not found with ID: "
                                                + claimId
                                )
                        )
                );
    }

    /*
     * Generates a readable and highly unique claim number.
     *
     * Example:
     * TSC-20260929-A1B2C3D4
     */
    private String generateClaimNumber() {

        String randomPart =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 8)
                        .toUpperCase();

        return "TSC-"
                + LocalDate.now()
                .toString()
                .replace("-", "")
                + "-"
                + randomPart;
    }

    private String normalizeOptionalText(
            String value) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        return value.trim();
    }

    private int compareDateTimesDescending(
            LocalDateTime first,
            LocalDateTime second) {

        if (first == null
                && second == null) {

            return 0;
        }

        if (first == null) {
            return 1;
        }

        if (second == null) {
            return -1;
        }

        return second.compareTo(
                first
        );
    }

    private ClaimResponseDTO convertToClaimResponse(
            Claim claim) {

        return new ClaimResponseDTO(
                claim.getClaimId(),
                claim.getClaimNumber(),
                claim.getIncidentId(),
                claim.getCustomerId(),
                claim.getTwinId(),
                claim.getPolicyId(),
                claim.getActionId(),
                claim.getClaimedAmount(),
                claim.getApprovedAmount(),
                claim.getDeductibleApplied(),
                claim.getStatus(),
                claim.getAssignedAdjusterId(),
                claim.getSubmittedAt(),
                claim.getUpdatedAt(),
                claim.getClosedAt()
        );
    }

    private ClaimDocumentResponseDTO
    convertToDocumentResponse(
            ClaimDocument document) {

        return new ClaimDocumentResponseDTO(
                document.getDocumentId(),
                document.getClaimId(),
                document.getDocumentType(),
                document.getFileName(),
                document.getDocumentReference(),
                document.getDescription(),
                document.getUploadedBy(),
                document.getUploadedAt()
        );
    }

    private ClaimDecisionResponseDTO
    convertToDecisionResponse(
            ClaimDecision decision) {

        return new ClaimDecisionResponseDTO(
                decision.getDecisionId(),
                decision.getClaimId(),
                decision.getAdjusterId(),
                decision.getDecisionType(),
                decision.getPreviousStatus(),
                decision.getNewStatus(),
                decision.getApprovedAmount(),
                decision.getReason(),
                decision.getDecidedAt()
        );
    }

    private IncidentResponseDTO
    convertToIncidentResponse(
            Incident incident) {

        return new IncidentResponseDTO(
                incident.getIncidentId(),
                incident.getCustomerId(),
                incident.getTwinId(),
                incident.getPolicyId(),
                incident.getActionId(),
                incident.getIncidentType(),
                incident.getDescription(),
                incident.getLossAmount(),
                incident.getActionOccurredAt(),
                incident.getReportedAt(),
                incident.getStatus()
        );
    }

    private void validateCustomerId(
            Long customerId) {

        if (customerId == null) {

            throw new InvalidRequestException(
                    "Customer profile is not linked. "
                            + "Create a Customer profile "
                            + "and log in again."
            );
        }
    }

    // Notifies the assigned Adjuster after Customer evidence is added.
// Notification failure does not undo a successful evidence upload.

    private Mono<Void> notifyAssignedAdjuster(
            Claim claim,
            ClaimDocument document,
            String jwtToken) {

        if (claim.getAssignedAdjusterId() == null) {

            return Mono.empty();
        }

        CreateNotificationRequestDTO request =
                new CreateNotificationRequestDTO(
                        claim.getAssignedAdjusterId(),
                        "CLAIM_EVIDENCE_ADDED",
                        "New claim evidence received",
                        "The Customer added "
                                + document.getDocumentType().name()
                                + " evidence to claim "
                                + claim.getClaimNumber()
                                + ".",
                        "CLAIM",
                        claim.getClaimId(),
                        "CLAIMS-SERVICE",
                        "HIGH"
                );

        String url =
                "http://NOTIFICATION-SERVICE"
                        + "/internal/notifications";

        return webClientBuilder
                .build()
                .post()
                .uri(url)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtToken
                )
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(unused ->
                        LOGGER.info(
                                "Claim evidence notification sent: "
                                        + "claimId={}, adjusterId={}, "
                                        + "documentId={}",
                                claim.getClaimId(),
                                claim.getAssignedAdjusterId(),
                                document.getDocumentId()
                        )
                )
                .onErrorResume(exception -> {

                    LOGGER.warn(
                            "Claim evidence notification failed: "
                                    + "claimId={}, adjusterId={}, reason={}",
                            claim.getClaimId(),
                            claim.getAssignedAdjusterId(),
                            exception.getMessage()
                    );

                    return Mono.empty();
                });
    }
}