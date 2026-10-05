package org.example.claimsservice.service;

// Reports incidents created from insured AI action violations.
// It validates action evidence, policy ownership, coverage rules,
// action dates, and loss limits before saving an incident.

import org.example.claimsservice.dto.request.ReportIncidentRequestDTO;
import org.example.claimsservice.dto.response.ActionViolationEvidenceResponseDTO;
import org.example.claimsservice.dto.response.AiActionEvidenceResponseDTO;
import org.example.claimsservice.dto.response.IncidentDetailsResponseDTO;
import org.example.claimsservice.dto.response.IncidentResponseDTO;
import org.example.claimsservice.dto.response.PolicyCoverageResponseDTO;
import org.example.claimsservice.dto.response.PolicyCoverageRuleResponseDTO;
import org.example.claimsservice.dto.response.PolicyIncidentValidationResponseDTO;
import org.example.claimsservice.enums.IncidentStatus;
import org.example.claimsservice.enums.IncidentType;
import org.example.claimsservice.exception.ClaimEligibilityException;
import org.example.claimsservice.exception.DownstreamServiceUnavailableException;
import org.example.claimsservice.exception.DownstreamValidationException;
import org.example.claimsservice.exception.DuplicateIncidentException;
import org.example.claimsservice.exception.IncidentNotFoundException;
import org.example.claimsservice.exception.InvalidRequestException;
import org.example.claimsservice.model.Incident;
import org.example.claimsservice.repo.IncidentRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class IncidentService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(IncidentService.class);

    private final IncidentRepo incidentRepo;
    private final WebClient.Builder webClientBuilder;

    public IncidentService(
            IncidentRepo incidentRepo,
            WebClient.Builder webClientBuilder) {

        this.incidentRepo = incidentRepo;
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<IncidentResponseDTO> reportIncident(
            Long customerId,
            String jwtToken,
            ReportIncidentRequestDTO request) {

        validateCustomerId(customerId);

        return incidentRepo
                .existsByActionId(request.actionId())
                .flatMap(incidentExists -> {

                    if (incidentExists) {

                        LOGGER.warn(
                                "Duplicate incident rejected: actionId={}, customerId={}",
                                request.actionId(),
                                customerId
                        );

                        return Mono.error(
                                new DuplicateIncidentException(
                                        "An incident has already been reported "
                                                + "for AI action ID: "
                                                + request.actionId()
                                )
                        );
                    }

                    Mono<AiActionEvidenceResponseDTO> actionEvidenceMono =
                            getActionEvidence(
                                    request.actionId(),
                                    jwtToken
                            );

                    Mono<PolicyCoverageResponseDTO> policyCoverageMono =
                            getPolicyCoverage(
                                    request.policyId(),
                                    jwtToken
                            );

                    return Mono.zip(
                            actionEvidenceMono,
                            policyCoverageMono
                    );
                })
                .flatMap(result -> {

                    AiActionEvidenceResponseDTO actionEvidence =
                            result.getT1();

                    PolicyCoverageResponseDTO policyCoverage =
                            result.getT2();

                    validateActionEvidence(
                            customerId,
                            request,
                            actionEvidence
                    );

                    validatePolicyCoverageOwnership(
                            customerId,
                            request,
                            actionEvidence,
                            policyCoverage
                    );

                    ActionViolationEvidenceResponseDTO matchingViolation =
                            findMatchingViolation(
                                    request.incidentType(),
                                    actionEvidence
                            );

                    PolicyCoverageRuleResponseDTO matchingCoverage =
                            findMatchingCoverage(
                                    request.incidentType(),
                                    actionEvidence.actionType(),
                                    policyCoverage
                            );

                    validateLossAmount(
                            request.lossAmount(),
                            policyCoverage,
                            matchingCoverage
                    );

                    LocalDate actionDate =
                            actionEvidence
                                    .occurredAt()
                                    .toLocalDate();

                    return validatePolicyOnActionDate(
                            request.policyId(),
                            customerId,
                            actionEvidence.twinId(),
                            actionDate,
                            jwtToken
                    )
                            .flatMap(policyValidation -> {

                                if (!policyValidation.valid()) {

                                    return Mono.error(
                                            new ClaimEligibilityException(
                                                    "The policy was not valid "
                                                            + "for this Customer, "
                                                            + "AI Twin, and action date"
                                            )
                                    );
                                }

                                return saveIncident(
                                        customerId,
                                        request,
                                        actionEvidence,
                                        matchingViolation
                                );
                            });
                })
                .map(this::convertToIncidentResponse)
                .doOnSuccess(response ->
                        LOGGER.info(
                                "Incident reported: incidentId={}, customerId={}, "
                                        + "twinId={}, policyId={}, actionId={}, type={}",
                                response.incidentId(),
                                response.customerId(),
                                response.twinId(),
                                response.policyId(),
                                response.actionId(),
                                response.incidentType()
                        )
                );
    }

    public Flux<IncidentResponseDTO> getMyIncidents(
            Long customerId) {

        validateCustomerId(customerId);

        return incidentRepo
                .findAllByCustomerId(customerId)
                .map(this::convertToIncidentResponse);
    }

    public Mono<IncidentDetailsResponseDTO> getOwnedIncidentDetails(
            Long incidentId,
            Long customerId,
            String jwtToken) {

        validateCustomerId(customerId);

        return incidentRepo
                .findByIncidentIdAndCustomerId(
                        incidentId,
                        customerId
                )
                .switchIfEmpty(
                        Mono.error(
                                new IncidentNotFoundException(
                                        "Incident not found"
                                )
                        )
                )
                .flatMap(incident ->
                        buildIncidentDetails(
                                incident,
                                jwtToken
                        )
                );
    }

    public Flux<IncidentResponseDTO> getAllIncidents() {

        return incidentRepo
                .findAll()
                .map(this::convertToIncidentResponse);
    }

    public Mono<IncidentDetailsResponseDTO> getIncidentDetailsForAdmin(
            Long incidentId,
            String jwtToken) {

        return findIncidentById(incidentId)
                .flatMap(incident ->
                        buildIncidentDetails(
                                incident,
                                jwtToken
                        )
                );
    }

    public Mono<IncidentResponseDTO> getInternalIncident(
            Long incidentId) {

        return findIncidentById(incidentId)
                .map(this::convertToIncidentResponse);
    }

    private Mono<Incident> saveIncident(
            Long customerId,
            ReportIncidentRequestDTO request,
            AiActionEvidenceResponseDTO actionEvidence,
            ActionViolationEvidenceResponseDTO matchingViolation) {

        Incident incident = new Incident();

        incident.setCustomerId(customerId);

        incident.setTwinId(
                actionEvidence.twinId()
        );

        incident.setPolicyId(
                request.policyId()
        );

        incident.setActionId(
                request.actionId()
        );

        incident.setIncidentType(
                IncidentType.valueOf(
                        matchingViolation.violationType()
                )
        );

        incident.setDescription(
                request.description().trim()
        );

        incident.setLossAmount(
                request.lossAmount()
        );

        incident.setActionOccurredAt(
                actionEvidence.occurredAt()
        );

        incident.setReportedAt(
                LocalDateTime.now()
        );

        incident.setStatus(
                IncidentStatus.REPORTED
        );

        return incidentRepo.save(incident);
    }

    private void validateActionEvidence(
            Long customerId,
            ReportIncidentRequestDTO request,
            AiActionEvidenceResponseDTO actionEvidence) {

        if (actionEvidence == null) {

            throw new DownstreamValidationException(
                    "AI action evidence is unavailable"
            );
        }

        if (!request.actionId().equals(
                actionEvidence.actionId()
        )) {

            throw new DownstreamValidationException(
                    "AI action evidence does not match "
                            + "the requested action"
            );
        }

        if (!customerId.equals(
                actionEvidence.customerId()
        )) {

            throw new ClaimEligibilityException(
                    "The AI action does not belong "
                            + "to the authenticated Customer"
            );
        }

        if (!Boolean.TRUE.equals(
                actionEvidence.insured()
        )) {

            throw new ClaimEligibilityException(
                    "The AI action was not insured "
                            + "when it was evaluated"
            );
        }

        if (!request.policyId().equals(
                actionEvidence.policyId()
        )) {

            throw new ClaimEligibilityException(
                    "The policy does not match "
                            + "the AI action evidence"
            );
        }

        if (!"VIOLATION_DETECTED".equals(
                actionEvidence.actionStatus()
        )) {

            throw new ClaimEligibilityException(
                    "Only an AI action with detected "
                            + "violations can be reported"
            );
        }

        if (actionEvidence.occurredAt() == null) {

            throw new DownstreamValidationException(
                    "AI action occurrence time is unavailable"
            );
        }

        if (actionEvidence.violations() == null
                || actionEvidence.violations().isEmpty()) {

            throw new ClaimEligibilityException(
                    "The AI action does not contain "
                            + "claimable violation evidence"
            );
        }
    }

    private void validatePolicyCoverageOwnership(
            Long customerId,
            ReportIncidentRequestDTO request,
            AiActionEvidenceResponseDTO actionEvidence,
            PolicyCoverageResponseDTO policyCoverage) {

        if (policyCoverage == null) {

            throw new DownstreamValidationException(
                    "Policy coverage information is unavailable"
            );
        }

        if (!request.policyId().equals(
                policyCoverage.policyId()
        )) {

            throw new ClaimEligibilityException(
                    "Policy coverage does not match "
                            + "the requested policy"
            );
        }

        if (!customerId.equals(
                policyCoverage.customerId()
        )) {

            throw new ClaimEligibilityException(
                    "The policy does not belong "
                            + "to the authenticated Customer"
            );
        }

        if (!actionEvidence.twinId().equals(
                policyCoverage.twinId()
        )) {

            throw new ClaimEligibilityException(
                    "The policy does not cover "
                            + "the AI Twin involved in the action"
            );
        }

        if (!policyCoverage.valid()) {

            throw new ClaimEligibilityException(
                    "The policy is not currently valid"
            );
        }

        if (policyCoverage.coverages() == null
                || policyCoverage.coverages().isEmpty()) {

            throw new ClaimEligibilityException(
                    "The policy has no active coverage rules"
            );
        }
    }

    private ActionViolationEvidenceResponseDTO findMatchingViolation(
            IncidentType incidentType,
            AiActionEvidenceResponseDTO actionEvidence) {

        return actionEvidence
                .violations()
                .stream()
                .filter(violation ->
                        incidentType
                                .name()
                                .equals(
                                        violation.violationType()
                                )
                )
                .filter(violation ->
                        Boolean.TRUE.equals(
                                violation.claimEligible()
                        )
                )
                .findFirst()
                .orElseThrow(() ->
                        new ClaimEligibilityException(
                                "The reported incident type does not "
                                        + "match a claim-eligible "
                                        + "AI action violation"
                        )
                );
    }

    private PolicyCoverageRuleResponseDTO findMatchingCoverage(
            IncidentType incidentType,
            String actionType,
            PolicyCoverageResponseDTO policyCoverage) {

        return policyCoverage
                .coverages()
                .stream()
                .filter(coverage ->
                        Boolean.TRUE.equals(
                                coverage.active()
                        )
                )
                .filter(coverage ->
                        actionType.equals(
                                coverage.actionType()
                        )
                )
                .filter(coverage ->
                        incidentType
                                .name()
                                .equals(
                                        coverage.violationType()
                                )
                )
                .findFirst()
                .orElseThrow(() ->
                        new ClaimEligibilityException(
                                "The reported AI action and violation "
                                        + "are not covered by this policy"
                        )
                );
    }

    private void validateLossAmount(
            BigDecimal lossAmount,
            PolicyCoverageResponseDTO policyCoverage,
            PolicyCoverageRuleResponseDTO matchingCoverage) {

        if (policyCoverage.coverageLimit() == null) {

            throw new DownstreamValidationException(
                    "Policy coverage limit is unavailable"
            );
        }

        BigDecimal applicableLimit;

        if (matchingCoverage.coverageLimit() == null) {

            applicableLimit =
                    policyCoverage.coverageLimit();

        } else {

            applicableLimit =
                    matchingCoverage
                            .coverageLimit()
                            .min(
                                    policyCoverage.coverageLimit()
                            );
        }

        if (lossAmount.compareTo(
                applicableLimit
        ) > 0) {

            throw new ClaimEligibilityException(
                    "Reported loss amount cannot exceed "
                            + "the applicable coverage limit of "
                            + applicableLimit
            );
        }
    }

    private Mono<AiActionEvidenceResponseDTO> getActionEvidence(
            Long actionId,
            String jwtToken) {

        String url =
                "http://AI-ACTION-SERVICE"
                        + "/internal/ai-actions/"
                        + actionId
                        + "/claim-evidence";

        return webClientBuilder
                .build()
                .get()
                .uri(url)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtToken
                )
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        response ->
                                Mono.error(
                                        new DownstreamValidationException(
                                                "AI action evidence "
                                                        + "was not found"
                                        )
                                )
                )
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        response ->
                                Mono.error(
                                        new DownstreamValidationException(
                                                "AI action evidence "
                                                        + "could not be retrieved"
                                        )
                                )
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response ->
                                Mono.error(
                                        new DownstreamServiceUnavailableException(
                                                "AI Action Service is "
                                                        + "currently unavailable"
                                        )
                                )
                )
                .bodyToMono(
                        AiActionEvidenceResponseDTO.class
                )
                .onErrorMap(
                        WebClientRequestException.class,
                        exception ->
                                new DownstreamServiceUnavailableException(
                                        "AI Action Service is "
                                                + "currently unavailable"
                                )
                );
    }

    private Mono<PolicyCoverageResponseDTO> getPolicyCoverage(
            Long policyId,
            String jwtToken) {

        String url =
                "http://INSURANCE-POLICY-SERVICE"
                        + "/internal/policies/"
                        + policyId
                        + "/coverage";

        return webClientBuilder
                .build()
                .get()
                .uri(url)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtToken
                )
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        response ->
                                Mono.error(
                                        new DownstreamValidationException(
                                                "Policy coverage "
                                                        + "was not found"
                                        )
                                )
                )
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        response ->
                                Mono.error(
                                        new DownstreamValidationException(
                                                "Policy coverage "
                                                        + "could not be retrieved"
                                        )
                                )
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response ->
                                Mono.error(
                                        new DownstreamServiceUnavailableException(
                                                "Insurance Policy Service is "
                                                        + "currently unavailable"
                                        )
                                )
                )
                .bodyToMono(
                        PolicyCoverageResponseDTO.class
                )
                .onErrorMap(
                        WebClientRequestException.class,
                        exception ->
                                new DownstreamServiceUnavailableException(
                                        "Insurance Policy Service is "
                                                + "currently unavailable"
                                )
                );
    }

    private Mono<PolicyIncidentValidationResponseDTO>
    validatePolicyOnActionDate(
            Long policyId,
            Long customerId,
            Long twinId,
            LocalDate actionDate,
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
                                        "/internal/policies/"
                                                + policyId
                                                + "/incident-validation"
                                )
                                .queryParam(
                                        "customerId",
                                        customerId
                                )
                                .queryParam(
                                        "twinId",
                                        twinId
                                )
                                .queryParam(
                                        "actionDate",
                                        actionDate
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
                                        new DownstreamValidationException(
                                                "Policy incident validation failed"
                                        )
                                )
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response ->
                                Mono.error(
                                        new DownstreamServiceUnavailableException(
                                                "Insurance Policy Service is "
                                                        + "currently unavailable"
                                        )
                                )
                )
                .bodyToMono(
                        PolicyIncidentValidationResponseDTO.class
                )
                .onErrorMap(
                        WebClientRequestException.class,
                        exception ->
                                new DownstreamServiceUnavailableException(
                                        "Insurance Policy Service is "
                                                + "currently unavailable"
                                )
                );
    }

    private Mono<IncidentDetailsResponseDTO> buildIncidentDetails(
            Incident incident,
            String jwtToken) {

        return getActionEvidence(
                incident.getActionId(),
                jwtToken
        )
                .map(actionEvidence ->
                        new IncidentDetailsResponseDTO(
                                convertToIncidentResponse(
                                        incident
                                ),
                                actionEvidence
                        )
                );
    }

    private Mono<Incident> findIncidentById(
            Long incidentId) {

        return incidentRepo
                .findById(incidentId)
                .switchIfEmpty(
                        Mono.error(
                                new IncidentNotFoundException(
                                        "Incident not found with ID: "
                                                + incidentId
                                )
                        )
                );
    }

    private IncidentResponseDTO convertToIncidentResponse(
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
}