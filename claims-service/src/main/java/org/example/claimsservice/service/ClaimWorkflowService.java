package org.example.claimsservice.service;

// Manages Admin assignment and Claims Adjuster workflow decisions.
// Protects assigned-Adjuster ownership, validates status transitions,
// calculates payable amounts, and stores every audit decision.

import org.example.claimsservice.dto.request.ApproveClaimRequestDTO;
import org.example.claimsservice.dto.request.AssignClaimRequestDTO;
import org.example.claimsservice.dto.request.ClaimReasonRequestDTO;
import org.example.claimsservice.dto.request.CreateNotificationRequestDTO;
import org.example.claimsservice.dto.response.ClaimDecisionResponseDTO;
import org.example.claimsservice.dto.response.ClaimDecisionResultResponseDTO;
import org.example.claimsservice.dto.response.ClaimResponseDTO;
import org.example.claimsservice.dto.response.ClaimReviewContextResponseDTO;
import org.example.claimsservice.enums.ClaimDecisionType;
import org.example.claimsservice.enums.ClaimStatus;
import org.example.claimsservice.enums.IncidentStatus;
import org.example.claimsservice.exception.ClaimNotFoundException;
import org.example.claimsservice.exception.IncidentNotFoundException;
import org.example.claimsservice.exception.InvalidClaimStateException;
import org.example.claimsservice.exception.InvalidRequestException;
import org.example.claimsservice.exception.ResourceAccessDeniedException;
import org.example.claimsservice.model.Claim;
import org.example.claimsservice.model.ClaimDecision;
import org.example.claimsservice.model.Incident;
import org.example.claimsservice.repo.ClaimDecisionRepo;
import org.example.claimsservice.repo.ClaimRepo;
import org.example.claimsservice.repo.IncidentRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class ClaimWorkflowService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    ClaimWorkflowService.class
            );

    private static final BigDecimal ZERO =
            BigDecimal.ZERO;

    private final ClaimRepo claimRepo;
    private final IncidentRepo incidentRepo;
    private final ClaimDecisionRepo decisionRepo;
    private final ClaimReviewContextService reviewContextService;
    private final AdjusterAssignmentValidator assignmentValidator;
    private final WebClient.Builder webClientBuilder;

    public ClaimWorkflowService(
            ClaimRepo claimRepo,
            IncidentRepo incidentRepo,
            ClaimDecisionRepo decisionRepo,
            ClaimReviewContextService reviewContextService,
            AdjusterAssignmentValidator assignmentValidator,
            WebClient.Builder webClientBuilder) {

        this.claimRepo = claimRepo;
        this.incidentRepo = incidentRepo;
        this.decisionRepo = decisionRepo;
        this.reviewContextService =
                reviewContextService;
        this.assignmentValidator =
                assignmentValidator;
        this.webClientBuilder =
                webClientBuilder;
    }

    @Transactional
    public Mono<ClaimDecisionResultResponseDTO> assignClaim(
            Long claimId,
            Long adminUserId,
            String jwtToken,
            AssignClaimRequestDTO request) {

        validateUserId(
                adminUserId,
                "Admin user ID"
        );

        validateUserId(
                request.adjusterId(),
                "Claims Adjuster user ID"
        );

        validateJwtToken(
                jwtToken
        );

        return assignmentValidator
                .validate(
                        request.adjusterId(),
                        jwtToken
                )
                .then(
                        findClaimById(
                                claimId
                        )
                )
                .flatMap(claim -> {

                    if (claim.getStatus()
                            != ClaimStatus.SUBMITTED) {

                        return Mono.error(
                                new InvalidClaimStateException(
                                        "Only a SUBMITTED claim "
                                                + "can be assigned"
                                )
                        );
                    }

                    ClaimStatus previousStatus =
                            claim.getStatus();

                    claim.setAssignedAdjusterId(
                            request.adjusterId()
                    );

                    claim.setStatus(
                            ClaimStatus.ASSIGNED
                    );

                    claim.setUpdatedAt(
                            LocalDateTime.now()
                    );

                    claim.setClosedAt(
                            null
                    );

                    return claimRepo
                            .save(claim)
                            .flatMap(savedClaim ->
                                    saveDecision(
                                            savedClaim,
                                            adminUserId,
                                            ClaimDecisionType.ASSIGN,
                                            previousStatus,
                                            ClaimStatus.ASSIGNED,
                                            null,
                                            "Claim assigned to Claims Adjuster "
                                                    + request.adjusterId()
                                    )
                                            .flatMap(decision -> {

                                                ClaimDecisionResultResponseDTO result =
                                                        buildDecisionResult(
                                                                savedClaim,
                                                                decision
                                                        );

                                                return sendAssignmentNotification(
                                                        request.adjusterId(),
                                                        savedClaim,
                                                        jwtToken
                                                )
                                                        .thenReturn(
                                                                result
                                                        );
                                            })
                            );
                })
                .doOnSuccess(result ->
                        LOGGER.info(
                                "Claim assigned: claimId={}, "
                                        + "adjusterId={}, adminId={}",
                                result.claim().claimId(),
                                result.claim()
                                        .assignedAdjusterId(),
                                adminUserId
                        )
                );
    }

    private void validateJwtToken(
            String jwtToken) {

        if (jwtToken == null
                || jwtToken.isBlank()) {

            throw new InvalidRequestException(
                    "Authentication token is unavailable"
            );
        }
    }

    public Flux<ClaimResponseDTO> getMyAssignedClaims(
            Long adjusterUserId) {

        validateUserId(
                adjusterUserId,
                "Claims Adjuster user ID"
        );

        return claimRepo
                .findAllByAssignedAdjusterIdOrderByUpdatedAtDesc(
                        adjusterUserId
                )
                .map(
                        this::convertToClaimResponse
                );
    }

    @Transactional
    public Mono<ClaimDecisionResultResponseDTO> startReview(
            Long claimId,
            Long adjusterUserId,
            ClaimReasonRequestDTO request) {

        return findAssignedClaim(
                claimId,
                adjusterUserId
        )
                .flatMap(claim -> {

                    boolean reviewCanStart =
                            claim.getStatus()
                                    == ClaimStatus.ASSIGNED
                                    || claim.getStatus()
                                    == ClaimStatus
                                    .MORE_INFORMATION_REQUIRED;

                    if (!reviewCanStart) {

                        return Mono.error(
                                new InvalidClaimStateException(
                                        "Review can start only for an "
                                                + "ASSIGNED or "
                                                + "MORE_INFORMATION_REQUIRED "
                                                + "claim"
                                )
                        );
                    }

                    return updateClaimAndSaveDecision(
                            claim,
                            adjusterUserId,
                            ClaimDecisionType.START_REVIEW,
                            ClaimStatus.UNDER_REVIEW,
                            null,
                            request.reason()
                    );
                });
    }

    @Transactional
    public Mono<ClaimDecisionResultResponseDTO> requestInformation(
            Long claimId,
            Long adjusterUserId,
            ClaimReasonRequestDTO request) {

        return findAssignedClaim(
                claimId,
                adjusterUserId
        )
                .flatMap(claim -> {

                    ensureClaimUnderReview(
                            claim
                    );

                    return updateClaimAndSaveDecision(
                            claim,
                            adjusterUserId,
                            ClaimDecisionType.REQUEST_INFORMATION,
                            ClaimStatus.MORE_INFORMATION_REQUIRED,
                            null,
                            request.reason()
                    );
                });
    }

    @Transactional
    public Mono<ClaimDecisionResultResponseDTO> approveClaim(
            Long claimId,
            Long adjusterUserId,
            String jwtToken,
            ApproveClaimRequestDTO request) {

        return processApproval(
                claimId,
                adjusterUserId,
                jwtToken,
                request,
                ClaimDecisionType.APPROVE,
                ClaimStatus.APPROVED,
                true
        );
    }

    @Transactional
    public Mono<ClaimDecisionResultResponseDTO> partiallyApproveClaim(
            Long claimId,
            Long adjusterUserId,
            String jwtToken,
            ApproveClaimRequestDTO request) {

        return processApproval(
                claimId,
                adjusterUserId,
                jwtToken,
                request,
                ClaimDecisionType.PARTIALLY_APPROVE,
                ClaimStatus.PARTIALLY_APPROVED,
                false
        );
    }

    @Transactional
    public Mono<ClaimDecisionResultResponseDTO> rejectClaim(
            Long claimId,
            Long adjusterUserId,
            ClaimReasonRequestDTO request) {

        return findAssignedClaim(
                claimId,
                adjusterUserId
        )
                .flatMap(claim -> {

                    ensureClaimUnderReview(
                            claim
                    );

                    // Null means no compensation was approved.
                    // This avoids presenting rejection as a zero settlement.
                    claim.setApprovedAmount(
                            null
                    );

                    claim.setDeductibleApplied(
                            null
                    );

                    return updateClaimAndSaveDecision(
                            claim,
                            adjusterUserId,
                            ClaimDecisionType.REJECT,
                            ClaimStatus.REJECTED,
                            null,
                            request.reason()
                    );
                });
    }

    @Transactional
    public Mono<ClaimDecisionResultResponseDTO> closeClaim(
            Long claimId,
            Long adjusterUserId,
            ClaimReasonRequestDTO request) {

        return findAssignedClaim(
                claimId,
                adjusterUserId
        )
                .flatMap(claim -> {

                    boolean finalDecisionExists =
                            claim.getStatus()
                                    == ClaimStatus.APPROVED
                                    || claim.getStatus()
                                    == ClaimStatus.PARTIALLY_APPROVED
                                    || claim.getStatus()
                                    == ClaimStatus.REJECTED;

                    if (!finalDecisionExists) {

                        return Mono.error(
                                new InvalidClaimStateException(
                                        "Only an APPROVED, "
                                                + "PARTIALLY_APPROVED, or "
                                                + "REJECTED claim can be closed"
                                )
                        );
                    }

                    ClaimStatus previousStatus =
                            claim.getStatus();

                    claim.setStatus(
                            ClaimStatus.CLOSED
                    );

                    claim.setClosedAt(
                            LocalDateTime.now()
                    );

                    claim.setUpdatedAt(
                            LocalDateTime.now()
                    );

                    return claimRepo
                            .save(claim)
                            .flatMap(savedClaim ->
                                    closeIncident(
                                            savedClaim.getIncidentId()
                                    )
                                            .then(
                                                    saveDecision(
                                                            savedClaim,
                                                            adjusterUserId,
                                                            ClaimDecisionType.CLOSE,
                                                            previousStatus,
                                                            ClaimStatus.CLOSED,
                                                            savedClaim
                                                                    .getApprovedAmount(),
                                                            request.reason()
                                                    )
                                            )
                                            .map(decision ->
                                                    buildDecisionResult(
                                                            savedClaim,
                                                            decision
                                                    )
                                            )
                            );
                })
                .doOnSuccess(result ->
                        LOGGER.info(
                                "Claim closed: claimId={}, "
                                        + "adjusterId={}, approvedAmount={}",
                                result.claim().claimId(),
                                adjusterUserId,
                                result.claim().approvedAmount()
                        )
                );
    }

    private Mono<ClaimDecisionResultResponseDTO> processApproval(
            Long claimId,
            Long adjusterUserId,
            String jwtToken,
            ApproveClaimRequestDTO request,
            ClaimDecisionType decisionType,
            ClaimStatus newStatus,
            boolean fullApproval) {

        return findAssignedClaim(
                claimId,
                adjusterUserId
        )
                .flatMap(claim -> {

                    ensureClaimUnderReview(
                            claim
                    );

                    return reviewContextService
                            .getReviewContext(
                                    claimId,
                                    adjusterUserId,
                                    jwtToken
                            )
                            .flatMap(reviewContext -> {

                                validateReviewContextForApproval(
                                        reviewContext
                                );

                                validateApprovalAmount(
                                        request.approvedAmount(),
                                        reviewContext
                                                .maximumPayableAmount(),
                                        fullApproval
                                );

                                ClaimStatus previousStatus =
                                        claim.getStatus();

                                claim.setApprovedAmount(
                                        request.approvedAmount()
                                );

                                // The approved amount is the final payable amount.
                                // The deductible is stored separately for explanation.
                                claim.setDeductibleApplied(
                                        reviewContext
                                                .deductibleApplied()
                                );

                                claim.setStatus(
                                        newStatus
                                );

                                claim.setUpdatedAt(
                                        LocalDateTime.now()
                                );

                                claim.setClosedAt(
                                        null
                                );

                                return claimRepo
                                        .save(claim)
                                        .flatMap(savedClaim ->
                                                saveDecision(
                                                        savedClaim,
                                                        adjusterUserId,
                                                        decisionType,
                                                        previousStatus,
                                                        newStatus,
                                                        request.approvedAmount(),
                                                        request.reason()
                                                )
                                                        .map(decision ->
                                                                buildDecisionResult(
                                                                        savedClaim,
                                                                        decision
                                                                )
                                                        )
                                        );
                            });
                })
                .doOnSuccess(result ->
                        LOGGER.info(
                                "Claim decision completed: claimId={}, "
                                        + "adjusterId={}, status={}, "
                                        + "approvedAmount={}, deductible={}",
                                result.claim().claimId(),
                                adjusterUserId,
                                result.claim().status(),
                                result.claim().approvedAmount(),
                                result.claim().deductibleApplied()
                        )
                );
    }

    private void validateReviewContextForApproval(
            ClaimReviewContextResponseDTO reviewContext) {

        if (!reviewContext
                .actionViolationFound()) {

            throw new InvalidRequestException(
                    "The claim cannot be approved because "
                            + "matching AI action violation "
                            + "evidence was not found"
            );
        }

        if (!reviewContext
                .actionMarkedClaimEligible()) {

            throw new InvalidRequestException(
                    "The matching AI action violation "
                            + "is not marked claim eligible"
            );
        }

        if (!reviewContext
                .customerMatches()) {

            throw new InvalidRequestException(
                    "The policy Customer does not match the claim"
            );
        }

        if (!reviewContext
                .twinMatches()) {

            throw new InvalidRequestException(
                    "The policy AI Twin does not match the claim"
            );
        }

        if (!reviewContext
                .policyValidOnActionDate()) {

            throw new InvalidRequestException(
                    "The policy was not valid on the "
                            + "AI action date"
            );
        }

        if (!reviewContext
                .coverageMatched()) {

            throw new InvalidRequestException(
                    "No active policy coverage rule matches "
                            + "the AI action and incident type"
            );
        }

        if (reviewContext
                .automaticExclusionDetected()) {

            throw new InvalidRequestException(
                    "The claim matches an active policy exclusion"
            );
        }

        if (!reviewContext.eligible()) {

            throw new InvalidRequestException(
                    "The claim failed automatic eligibility "
                            + "or payable-amount validation"
            );
        }

        if (reviewContext
                .maximumPayableAmount()
                == null
                || reviewContext
                .maximumPayableAmount()
                .compareTo(ZERO) <= 0) {

            throw new InvalidRequestException(
                    "The claim does not have a positive "
                            + "maximum payable amount"
            );
        }
    }

    private void validateApprovalAmount(
            BigDecimal approvedAmount,
            BigDecimal maximumPayableAmount,
            boolean fullApproval) {

        if (approvedAmount == null) {

            throw new InvalidRequestException(
                    "Approved amount is required"
            );
        }

        if (maximumPayableAmount == null
                || maximumPayableAmount
                .compareTo(ZERO) <= 0) {

            throw new InvalidRequestException(
                    "Maximum payable amount is unavailable "
                            + "or not positive"
            );
        }

        if (approvedAmount
                .compareTo(ZERO) <= 0) {

            throw new InvalidRequestException(
                    "Approved amount must be greater than zero"
            );
        }

        if (approvedAmount
                .compareTo(
                        maximumPayableAmount
                ) > 0) {

            throw new InvalidRequestException(
                    "Approved amount cannot exceed the "
                            + "maximum payable amount of "
                            + maximumPayableAmount
            );
        }

        if (fullApproval
                && approvedAmount
                .compareTo(
                        maximumPayableAmount
                ) != 0) {

            throw new InvalidRequestException(
                    "Full approval amount must equal the "
                            + "maximum payable amount of "
                            + maximumPayableAmount
            );
        }

        if (!fullApproval
                && approvedAmount
                .compareTo(
                        maximumPayableAmount
                ) >= 0) {

            throw new InvalidRequestException(
                    "Partial approval amount must be lower "
                            + "than the maximum payable amount of "
                            + maximumPayableAmount
            );
        }
    }

    private Mono<ClaimDecisionResultResponseDTO>
    updateClaimAndSaveDecision(
            Claim claim,
            Long adjusterUserId,
            ClaimDecisionType decisionType,
            ClaimStatus newStatus,
            BigDecimal approvedAmount,
            String reason) {

        ClaimStatus previousStatus =
                claim.getStatus();

        claim.setStatus(
                newStatus
        );

        claim.setUpdatedAt(
                LocalDateTime.now()
        );

        return claimRepo
                .save(claim)
                .flatMap(savedClaim ->
                        saveDecision(
                                savedClaim,
                                adjusterUserId,
                                decisionType,
                                previousStatus,
                                newStatus,
                                approvedAmount,
                                reason
                        )
                                .map(decision ->
                                        buildDecisionResult(
                                                savedClaim,
                                                decision
                                        )
                                )
                );
    }

    private Mono<ClaimDecision> saveDecision(
            Claim claim,
            Long decisionUserId,
            ClaimDecisionType decisionType,
            ClaimStatus previousStatus,
            ClaimStatus newStatus,
            BigDecimal approvedAmount,
            String reason) {

        ClaimDecision decision =
                new ClaimDecision();

        decision.setClaimId(
                claim.getClaimId()
        );

        decision.setAdjusterId(
                decisionUserId
        );

        decision.setDecisionType(
                decisionType
        );

        decision.setPreviousStatus(
                previousStatus
        );

        decision.setNewStatus(
                newStatus
        );

        decision.setApprovedAmount(
                approvedAmount
        );

        decision.setReason(
                reason.trim()
        );

        decision.setDecidedAt(
                LocalDateTime.now()
        );

        return decisionRepo.save(
                decision
        );
    }

    private Mono<Void> sendAssignmentNotification(
            Long recipientUserId,
            Claim claim,
            String jwtToken) {

        CreateNotificationRequestDTO request =
                new CreateNotificationRequestDTO(
                        recipientUserId,
                        "CLAIM_ASSIGNED",
                        "Claim assigned",
                        "Claim "
                                + claim.getClaimNumber()
                                + " has been assigned to you "
                                + "for review.",
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
                                "Claim assignment notification sent: "
                                        + "claimId={}, adjusterId={}",
                                claim.getClaimId(),
                                recipientUserId
                        )
                )
                .onErrorResume(exception -> {

                    LOGGER.warn(
                            "Claim assignment notification failed: "
                                    + "claimId={}, adjusterId={}, reason={}",
                            claim.getClaimId(),
                            recipientUserId,
                            exception.getMessage()
                    );

                    return Mono.empty();
                });
    }

    private Mono<Claim> findAssignedClaim(
            Long claimId,
            Long adjusterUserId) {

        validateUserId(
                adjusterUserId,
                "Claims Adjuster user ID"
        );

        return findClaimById(
                claimId
        )
                .flatMap(claim -> {

                    if (claim.getAssignedAdjusterId()
                            == null
                            || !claim
                            .getAssignedAdjusterId()
                            .equals(
                                    adjusterUserId
                            )) {

                        return Mono.error(
                                new ResourceAccessDeniedException(
                                        "This claim is not assigned "
                                                + "to the authenticated "
                                                + "Claims Adjuster"
                                )
                        );
                    }

                    return Mono.just(
                            claim
                    );
                });
    }

    private void ensureClaimUnderReview(
            Claim claim) {

        if (claim.getStatus()
                != ClaimStatus.UNDER_REVIEW) {

            throw new InvalidClaimStateException(
                    "This decision requires the claim "
                            + "to be in UNDER_REVIEW status"
            );
        }
    }

    private Mono<Incident> closeIncident(
            Long incidentId) {

        return incidentRepo
                .findById(
                        incidentId
                )
                .switchIfEmpty(
                        Mono.error(
                                new IncidentNotFoundException(
                                        "Incident linked to the claim "
                                                + "was not found"
                                )
                        )
                )
                .flatMap(incident -> {

                    incident.setStatus(
                            IncidentStatus.CLOSED
                    );

                    return incidentRepo.save(
                            incident
                    );
                });
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

    private void validateUserId(
            Long userId,
            String fieldName) {

        if (userId == null
                || userId <= 0) {

            throw new InvalidRequestException(
                    fieldName
                            + " must be a positive number"
            );
        }
    }

    private ClaimDecisionResultResponseDTO
    buildDecisionResult(
            Claim claim,
            ClaimDecision decision) {

        return new ClaimDecisionResultResponseDTO(
                convertToClaimResponse(
                        claim
                ),
                convertToDecisionResponse(
                        decision
                )
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
}