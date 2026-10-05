package org.example.aiactionservice.service;

// Evaluates simulated AI Twin actions and stores every detected violation.
// Scenario facts are retained for action history, incident evidence,
// and complete claim investigation.

import org.example.aiactionservice.dto.request.CreateNotificationRequestDTO;
import org.example.aiactionservice.dto.request.SimulateActionRequestDTO;
import org.example.aiactionservice.dto.response.ActionEvaluationResponseDTO;
import org.example.aiactionservice.dto.response.ActionViolationResponseDTO;
import org.example.aiactionservice.dto.response.ActivePolicyResponseDTO;
import org.example.aiactionservice.dto.response.AiActionDetailsResponseDTO;
import org.example.aiactionservice.dto.response.AiActionResponseDTO;
import org.example.aiactionservice.dto.response.AiTwinActionRulesResponseDTO;
import org.example.aiactionservice.enums.ActionStatus;
import org.example.aiactionservice.exception.AiActionNotFoundException;
import org.example.aiactionservice.exception.AiTwinValidationException;
import org.example.aiactionservice.exception.DownstreamServiceUnavailableException;
import org.example.aiactionservice.exception.DuplicateTransactionException;
import org.example.aiactionservice.exception.InvalidRequestException;
import org.example.aiactionservice.model.ActionViolation;
import org.example.aiactionservice.model.AiAction;
import org.example.aiactionservice.repo.ActionViolationRepo;
import org.example.aiactionservice.repo.AiActionRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActionEvaluationService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    ActionEvaluationService.class
            );

    private final AiActionRepo actionRepo;
    private final ActionViolationRepo violationRepo;
    private final ActionRuleEvaluator ruleEvaluator;
    private final WebClient.Builder webClientBuilder;

    public ActionEvaluationService(
            AiActionRepo actionRepo,
            ActionViolationRepo violationRepo,
            ActionRuleEvaluator ruleEvaluator,
            WebClient.Builder webClientBuilder) {

        this.actionRepo = actionRepo;
        this.violationRepo = violationRepo;
        this.ruleEvaluator = ruleEvaluator;
        this.webClientBuilder = webClientBuilder;
    }

    @Transactional
    public Mono<ActionEvaluationResponseDTO> simulateAction(
            Long userId,
            Long customerId,
            String jwtToken,
            SimulateActionRequestDTO request) {

        validateUserId(
                userId
        );

        validateCustomerId(
                customerId
        );

        validateJwtToken(
                jwtToken
        );

        String transactionReference =
                normalizeTransactionReference(
                        request.transactionReference()
                );

        return actionRepo
                .existsByTransactionReference(
                        transactionReference
                )
                .flatMap(referenceExists -> {

                    if (referenceExists) {

                        LOGGER.warn(
                                "Duplicate action rejected: "
                                        + "transactionReference={}, "
                                        + "customerId={}, twinId={}",
                                transactionReference,
                                customerId,
                                request.twinId()
                        );

                        return Mono.error(
                                new DuplicateTransactionException(
                                        "An AI action already exists "
                                                + "with transaction reference: "
                                                + transactionReference
                                )
                        );
                    }

                    Mono<AiTwinActionRulesResponseDTO>
                            actionRulesMono =
                            getAiTwinActionRules(
                                    request.twinId(),
                                    customerId,
                                    jwtToken
                            );

                    Mono<ActivePolicyResponseDTO>
                            activePolicyMono =
                            getActivePolicy(
                                    request.twinId(),
                                    jwtToken
                            );

                    return Mono.zip(
                            actionRulesMono,
                            activePolicyMono
                    );
                })
                .flatMap(result -> {

                    AiTwinActionRulesResponseDTO twinRules =
                            result.getT1();

                    ActivePolicyResponseDTO activePolicy =
                            result.getT2();

                    validateTwinOwnership(
                            customerId,
                            request.twinId(),
                            twinRules
                    );

                    boolean insured =
                            isInsured(
                                    customerId,
                                    request.twinId(),
                                    activePolicy
                            );

                    List<ActionRuleEvaluator.ViolationResult>
                            violationResults =
                            ruleEvaluator.evaluate(
                                    request,
                                    twinRules,
                                    insured
                            );

                    ActionStatus actionStatus =
                            violationResults.isEmpty()
                                    ? ActionStatus.COMPLIANT
                                    : ActionStatus.VIOLATION_DETECTED;

                    AiAction action =
                            createAction(
                                    customerId,
                                    request,
                                    transactionReference,
                                    activePolicy,
                                    insured,
                                    actionStatus
                            );

                    return actionRepo
                            .save(action)
                            .flatMap(savedAction ->
                                    saveViolations(
                                            savedAction,
                                            violationResults
                                    )
                                            .flatMap(savedViolations -> {

                                                ActionEvaluationResponseDTO response =
                                                        buildEvaluationResponse(
                                                                savedAction,
                                                                savedViolations
                                                        );

                                                if (savedViolations.isEmpty()) {

                                                    return Mono.just(
                                                            response
                                                    );
                                                }

                                                return sendViolationNotification(
                                                        userId,
                                                        savedAction,
                                                        savedViolations,
                                                        jwtToken
                                                )
                                                        .thenReturn(
                                                                response
                                                        );
                                            })
                            );
                })
                .doOnSuccess(response ->
                        LOGGER.info(
                                "AI action evaluated: actionId={}, "
                                        + "customerId={}, twinId={}, "
                                        + "status={}, insured={}, "
                                        + "violationCount={}",
                                response.actionId(),
                                response.customerId(),
                                response.twinId(),
                                response.actionStatus(),
                                response.insured(),
                                response.violations().size()
                        )
                );
    }

    public Flux<AiActionResponseDTO> getMyActions(
            Long customerId) {

        validateCustomerId(
                customerId
        );

        return actionRepo
                .findAllByCustomerId(
                        customerId
                )
                .sort(
                        (first, second) ->
                                second
                                        .getCreatedAt()
                                        .compareTo(
                                                first.getCreatedAt()
                                        )
                )
                .map(
                        this::convertToActionResponse
                );
    }

    public Mono<AiActionDetailsResponseDTO>
    getOwnedActionDetails(
            Long actionId,
            Long customerId) {

        validateCustomerId(
                customerId
        );

        return actionRepo
                .findByActionIdAndCustomerId(
                        actionId,
                        customerId
                )
                .switchIfEmpty(
                        Mono.error(
                                new AiActionNotFoundException(
                                        "AI action not found"
                                )
                        )
                )
                .flatMap(
                        this::buildActionDetails
                );
    }

    public Flux<AiActionResponseDTO> getAllActions() {

        return actionRepo
                .findAll()
                .sort(
                        (first, second) ->
                                second
                                        .getCreatedAt()
                                        .compareTo(
                                                first.getCreatedAt()
                                        )
                )
                .map(
                        this::convertToActionResponse
                );
    }

    public Mono<AiActionDetailsResponseDTO>
    getActionDetailsForAdmin(
            Long actionId) {

        return findActionById(
                actionId
        )
                .flatMap(
                        this::buildActionDetails
                );
    }

    public Mono<AiActionDetailsResponseDTO>
    getActionForClaimReview(
            Long actionId) {

        return findActionById(
                actionId
        )
                .flatMap(
                        this::buildActionDetails
                );
    }

    public Flux<ActionViolationResponseDTO>
    getClaimEligibleViolations() {

        return violationRepo
                .findAllByClaimEligibleTrue()
                .map(
                        this::convertToViolationResponse
                );
    }

    private AiAction createAction(
            Long customerId,
            SimulateActionRequestDTO request,
            String transactionReference,
            ActivePolicyResponseDTO activePolicy,
            boolean insured,
            ActionStatus actionStatus) {

        LocalDateTime occurredAt =
                request.occurredAt() == null
                        ? LocalDateTime.now()
                        : request.occurredAt();

        if (occurredAt.isAfter(
                LocalDateTime.now()
        )) {

            throw new InvalidRequestException(
                    "Action occurrence time cannot be in the future"
            );
        }

        LocalDateTime currentTime =
                LocalDateTime.now();

        AiAction action =
                new AiAction();

        action.setCustomerId(
                customerId
        );

        action.setTwinId(
                request.twinId()
        );

        action.setPolicyId(
                insured
                        ? activePolicy.policyId()
                        : null
        );

        action.setActionType(
                request.actionType()
        );

        action.setTransactionReference(
                transactionReference
        );

        action.setDescription(
                request.description().trim()
        );

        action.setActionAmount(
                request.actionAmount()
        );

        action.setApprovalProvided(
                request.approvalProvided()
        );

        action.setBookingOutcome(
                request.bookingOutcome()
        );

        action.setPurchaseOutcome(
                request.purchaseOutcome()
        );

        action.setSubscriptionOperation(
                request.subscriptionOperation()
        );

        action.setCancellationCompleted(
                request.cancellationCompleted()
        );

        action.setActionStatus(
                actionStatus
        );

        action.setInsured(
                insured
        );

        action.setOccurredAt(
                occurredAt
        );

        action.setEvaluatedAt(
                currentTime
        );

        action.setCreatedAt(
                currentTime
        );

        return action;
    }

    private Mono<List<ActionViolationResponseDTO>>
    saveViolations(
            AiAction savedAction,
            List<ActionRuleEvaluator.ViolationResult>
                    violationResults) {

        if (violationResults.isEmpty()) {

            return Mono.just(
                    List.of()
            );
        }

        LocalDateTime currentTime =
                LocalDateTime.now();

        List<ActionViolation> violations =
                violationResults
                        .stream()
                        .map(result -> {

                            ActionViolation violation =
                                    new ActionViolation();

                            violation.setActionId(
                                    savedAction.getActionId()
                            );

                            violation.setViolationType(
                                    result.violationType()
                            );

                            violation.setSeverity(
                                    result.severity()
                            );

                            violation.setDescription(
                                    result.description()
                            );

                            violation.setClaimEligible(
                                    result.claimEligible()
                            );

                            violation.setCreatedAt(
                                    currentTime
                            );

                            return violation;
                        })
                        .toList();

        return violationRepo
                .saveAll(
                        violations
                )
                .map(
                        this::convertToViolationResponse
                )
                .collectList();
    }

    private Mono<Void> sendViolationNotification(
            Long recipientUserId,
            AiAction action,
            List<ActionViolationResponseDTO> violations,
            String jwtToken) {

        String violationSummary =
                violations
                        .stream()
                        .map(violation ->
                                violation
                                        .violationType()
                                        .name()
                        )
                        .sorted()
                        .reduce(
                                (first, second) ->
                                        first
                                                + ", "
                                                + second
                        )
                        .orElse(
                                "UNKNOWN"
                        );

        CreateNotificationRequestDTO request =
                new CreateNotificationRequestDTO(
                        recipientUserId,
                        "AI_ACTION_VIOLATION_DETECTED",
                        "AI action violation detected",
                        "AI Twin action "
                                + action.getTransactionReference()
                                + " produced: "
                                + violationSummary
                                + ".",
                        "AI_ACTION",
                        action.getActionId(),
                        "AI-ACTION-SERVICE",
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
                .bodyValue(
                        request
                )
                .retrieve()
                .bodyToMono(
                        Void.class
                )
                .doOnSuccess(unused ->
                        LOGGER.info(
                                "Violation notification sent: "
                                        + "actionId={}, recipientUserId={}",
                                action.getActionId(),
                                recipientUserId
                        )
                )
                .onErrorResume(exception -> {

                    LOGGER.warn(
                            "Violation notification could not be sent: "
                                    + "actionId={}, recipientUserId={}, "
                                    + "reason={}",
                            action.getActionId(),
                            recipientUserId,
                            exception.getMessage()
                    );

                    return Mono.empty();
                });
    }

    private Mono<AiTwinActionRulesResponseDTO>
    getAiTwinActionRules(
            Long twinId,
            Long customerId,
            String jwtToken) {

        return webClientBuilder
                .build()
                .get()
                .uri(uriBuilder ->
                        uriBuilder
                                .scheme("http")
                                .host(
                                        "AI-TWIN-SERVICE"
                                )
                                .path(
                                        "/internal/ai-twins/"
                                                + twinId
                                                + "/action-rules"
                                )
                                .queryParam(
                                        "customerId",
                                        customerId
                                )
                                .build()
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtToken
                )
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        response ->
                                Mono.error(
                                        new AiTwinValidationException(
                                                "AI Twin action rules "
                                                        + "could not be retrieved"
                                        )
                                )
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response ->
                                Mono.error(
                                        new DownstreamServiceUnavailableException(
                                                "AI Twin Service is "
                                                        + "currently unavailable"
                                        )
                                )
                )
                .bodyToMono(
                        AiTwinActionRulesResponseDTO.class
                )
                .onErrorMap(
                        WebClientRequestException.class,
                        exception ->
                                new DownstreamServiceUnavailableException(
                                        "AI Twin Service is "
                                                + "currently unavailable"
                                )
                );
    }

    private Mono<ActivePolicyResponseDTO>
    getActivePolicy(
            Long twinId,
            String jwtToken) {

        return webClientBuilder
                .build()
                .get()
                .uri(uriBuilder ->
                        uriBuilder
                                .scheme("http")
                                .host(
                                        "INSURANCE-POLICY-SERVICE"
                                )
                                .path(
                                        "/internal/policies/active"
                                )
                                .queryParam(
                                        "twinId",
                                        twinId
                                )
                                .build()
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtToken
                )
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        response ->
                                Mono.error(
                                        new InvalidRequestException(
                                                "Active policy lookup failed"
                                        )
                                )
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response ->
                                Mono.error(
                                        new DownstreamServiceUnavailableException(
                                                "Insurance Policy Service "
                                                        + "is currently unavailable"
                                        )
                                )
                )
                .bodyToMono(
                        ActivePolicyResponseDTO.class
                )
                .onErrorMap(
                        WebClientRequestException.class,
                        exception ->
                                new DownstreamServiceUnavailableException(
                                        "Insurance Policy Service "
                                                + "is currently unavailable"
                                )
                );
    }

    private void validateTwinOwnership(
            Long customerId,
            Long twinId,
            AiTwinActionRulesResponseDTO twinRules) {

        if (twinRules == null) {

            throw new AiTwinValidationException(
                    "AI Twin action rules are unavailable"
            );
        }

        if (!twinId.equals(
                twinRules.twinId()
        )) {

            throw new AiTwinValidationException(
                    "AI Twin information does not match "
                            + "the requested Twin"
            );
        }

        if (!customerId.equals(
                twinRules.customerId()
        )) {

            throw new AiTwinValidationException(
                    "AI Twin does not belong to "
                            + "the authenticated Customer"
            );
        }

        if (!twinRules.valid()) {

            throw new AiTwinValidationException(
                    "AI Twin is inactive or invalid"
            );
        }
    }

    private boolean isInsured(
            Long customerId,
            Long twinId,
            ActivePolicyResponseDTO activePolicy) {

        if (activePolicy == null
                || !activePolicy.active()
                || activePolicy.policyId()
                == null) {

            return false;
        }

        return customerId.equals(
                activePolicy.customerId()
        )
                && twinId.equals(
                activePolicy.twinId()
        );
    }

    private Mono<AiAction> findActionById(
            Long actionId) {

        if (actionId == null
                || actionId <= 0) {

            return Mono.error(
                    new InvalidRequestException(
                            "AI action ID must be a positive number"
                    )
            );
        }

        return actionRepo
                .findById(
                        actionId
                )
                .switchIfEmpty(
                        Mono.error(
                                new AiActionNotFoundException(
                                        "AI action not found with ID: "
                                                + actionId
                                )
                        )
                );
    }

    private Mono<AiActionDetailsResponseDTO>
    buildActionDetails(
            AiAction action) {

        return violationRepo
                .findAllByActionId(
                        action.getActionId()
                )
                .sort(
                        (first, second) ->
                                second
                                        .getCreatedAt()
                                        .compareTo(
                                                first.getCreatedAt()
                                        )
                )
                .map(
                        this::convertToViolationResponse
                )
                .collectList()
                .map(violations ->
                        new AiActionDetailsResponseDTO(
                                action.getActionId(),
                                action.getCustomerId(),
                                action.getTwinId(),
                                action.getPolicyId(),
                                action.getActionType(),
                                action.getTransactionReference(),
                                action.getDescription(),
                                action.getActionAmount(),
                                action.getApprovalProvided(),
                                action.getBookingOutcome(),
                                action.getPurchaseOutcome(),
                                action.getSubscriptionOperation(),
                                action.getCancellationCompleted(),
                                action.getActionStatus(),
                                action.getInsured(),
                                violations,
                                action.getOccurredAt(),
                                action.getEvaluatedAt(),
                                action.getCreatedAt()
                        )
                );
    }

    private ActionEvaluationResponseDTO
    buildEvaluationResponse(
            AiAction action,
            List<ActionViolationResponseDTO> violations) {

        boolean compliant =
                action.getActionStatus()
                        == ActionStatus.COMPLIANT;

        boolean claimEligible =
                violations
                        .stream()
                        .anyMatch(violation ->
                                Boolean.TRUE.equals(
                                        violation.claimEligible()
                                )
                        );

        String message =
                compliant
                        ? "AI action complies with all configured rules"
                        : "AI action contains one or more rule violations";

        return new ActionEvaluationResponseDTO(
                action.getActionId(),
                action.getCustomerId(),
                action.getTwinId(),
                action.getPolicyId(),
                action.getActionType(),
                action.getTransactionReference(),
                action.getActionStatus(),
                Boolean.TRUE.equals(
                        action.getInsured()
                ),
                compliant,
                claimEligible,
                violations,
                message
        );
    }

    private AiActionResponseDTO convertToActionResponse(
            AiAction action) {

        return new AiActionResponseDTO(
                action.getActionId(),
                action.getCustomerId(),
                action.getTwinId(),
                action.getPolicyId(),
                action.getActionType(),
                action.getTransactionReference(),
                action.getDescription(),
                action.getActionAmount(),
                action.getApprovalProvided(),
                action.getBookingOutcome(),
                action.getPurchaseOutcome(),
                action.getSubscriptionOperation(),
                action.getCancellationCompleted(),
                action.getActionStatus(),
                action.getInsured(),
                action.getOccurredAt(),
                action.getEvaluatedAt(),
                action.getCreatedAt()
        );
    }

    private ActionViolationResponseDTO
    convertToViolationResponse(
            ActionViolation violation) {

        return new ActionViolationResponseDTO(
                violation.getViolationId(),
                violation.getActionId(),
                violation.getViolationType(),
                violation.getSeverity(),
                violation.getDescription(),
                violation.getClaimEligible(),
                violation.getCreatedAt()
        );
    }

    private String normalizeTransactionReference(
            String transactionReference) {

        return transactionReference
                .trim()
                .toUpperCase();
    }

    private void validateUserId(
            Long userId) {

        if (userId == null
                || userId <= 0) {

            throw new InvalidRequestException(
                    "Authenticated user ID is required"
            );
        }
    }

    private void validateCustomerId(
            Long customerId) {

        if (customerId == null
                || customerId <= 0) {

            throw new InvalidRequestException(
                    "Customer profile is not linked. "
                            + "Create a Customer profile "
                            + "and log in again."
            );
        }
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
}