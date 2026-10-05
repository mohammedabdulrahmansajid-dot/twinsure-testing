package org.example.claimsservice.service;

// Builds the authoritative Claims Adjuster review context for one claim.
// It validates assignment, reloads AI action and policy evidence,
// verifies historical policy validity, and calculates the maximum payable amount.

import org.example.claimsservice.dto.response.ActionViolationEvidenceResponseDTO;
import org.example.claimsservice.dto.response.AiActionEvidenceResponseDTO;
import org.example.claimsservice.dto.response.ClaimReviewContextResponseDTO;
import org.example.claimsservice.dto.response.PolicyCoverageResponseDTO;
import org.example.claimsservice.dto.response.PolicyCoverageRuleResponseDTO;
import org.example.claimsservice.dto.response.PolicyExclusionResponseDTO;
import org.example.claimsservice.dto.response.PolicyIncidentValidationResponseDTO;
import org.example.claimsservice.exception.ClaimNotFoundException;
import org.example.claimsservice.exception.DownstreamServiceUnavailableException;
import org.example.claimsservice.exception.DownstreamValidationException;
import org.example.claimsservice.exception.IncidentNotFoundException;
import org.example.claimsservice.exception.InvalidRequestException;
import org.example.claimsservice.exception.ResourceAccessDeniedException;
import org.example.claimsservice.model.Claim;
import org.example.claimsservice.model.Incident;
import org.example.claimsservice.repo.ClaimRepo;
import org.example.claimsservice.repo.IncidentRepo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class ClaimReviewContextService {

    private static final BigDecimal ZERO =
            BigDecimal.ZERO;

    private final ClaimRepo claimRepo;
    private final IncidentRepo incidentRepo;
    private final WebClient.Builder webClientBuilder;

    public ClaimReviewContextService(
            ClaimRepo claimRepo,
            IncidentRepo incidentRepo,
            WebClient.Builder webClientBuilder) {

        this.claimRepo = claimRepo;
        this.incidentRepo = incidentRepo;
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<ClaimReviewContextResponseDTO>
    getReviewContext(
            Long claimId,
            Long adjusterUserId,
            String jwtToken) {

        validateUserId(
                adjusterUserId
        );

        validateJwtToken(
                jwtToken
        );

        return findAssignedClaim(
                claimId,
                adjusterUserId
        )
                .flatMap(claim ->
                        findIncident(
                                claim.getIncidentId()
                        )
                                .flatMap(incident ->
                                        buildReviewContext(
                                                claim,
                                                incident,
                                                jwtToken
                                        )
                                )
                );
    }

    private Mono<ClaimReviewContextResponseDTO>
    buildReviewContext(
            Claim claim,
            Incident incident,
            String jwtToken) {

        Mono<AiActionEvidenceResponseDTO>
                actionEvidenceMono =
                getActionEvidence(
                        claim.getActionId(),
                        jwtToken
                );

        Mono<PolicyCoverageResponseDTO>
                policyCoverageMono =
                getPolicyCoverage(
                        claim.getPolicyId(),
                        jwtToken
                );

        LocalDate actionDate =
                incident
                        .getActionOccurredAt()
                        .toLocalDate();

        Mono<PolicyIncidentValidationResponseDTO>
                policyValidationMono =
                validatePolicyOnActionDate(
                        claim.getPolicyId(),
                        claim.getCustomerId(),
                        claim.getTwinId(),
                        actionDate,
                        jwtToken
                );

        return Mono.zip(
                        actionEvidenceMono,
                        policyCoverageMono,
                        policyValidationMono
                )
                .map(result ->
                        calculateReviewContext(
                                claim,
                                incident,
                                result.getT1(),
                                result.getT2(),
                                result.getT3()
                        )
                );
    }

    private ClaimReviewContextResponseDTO
    calculateReviewContext(
            Claim claim,
            Incident incident,
            AiActionEvidenceResponseDTO actionEvidence,
            PolicyCoverageResponseDTO policyCoverage,
            PolicyIncidentValidationResponseDTO policyValidation) {

        validateResponseIdentity(
                claim,
                actionEvidence,
                policyCoverage,
                policyValidation
        );

        ActionViolationEvidenceResponseDTO matchingViolation =
                findMatchingViolation(
                        incident,
                        actionEvidence
                );

        PolicyCoverageRuleResponseDTO matchingCoverage =
                findMatchingCoverage(
                        incident,
                        actionEvidence,
                        policyCoverage
                );

        boolean actionViolationFound =
                matchingViolation != null;

        boolean actionMarkedClaimEligible =
                matchingViolation != null
                        && Boolean.TRUE.equals(
                        matchingViolation
                                .claimEligible()
                );

        boolean coverageMatched =
                matchingCoverage != null;

        List<PolicyCoverageRuleResponseDTO>
                matchingCoverages =
                policyCoverage.coverages() == null
                        ? List.of()
                        : policyCoverage
                        .coverages()
                        .stream()
                        .filter(coverage ->
                                Boolean.TRUE.equals(
                                        coverage.active()
                                )
                        )
                        .filter(coverage ->
                                Objects.equals(
                                        actionEvidence.actionType(),
                                        coverage.actionType()
                                )
                        )
                        .filter(coverage ->
                                Objects.equals(
                                        incident
                                                .getIncidentType()
                                                .name(),
                                        coverage
                                                .violationType()
                                )
                        )
                        .toList();

        List<PolicyExclusionResponseDTO>
                activeExclusions =
                policyCoverage.exclusions() == null
                        ? List.of()
                        : policyCoverage
                        .exclusions()
                        .stream()
                        .filter(exclusion ->
                                Boolean.TRUE.equals(
                                        exclusion.active()
                                )
                        )
                        .toList();

        boolean activeExclusionsPresent =
                !activeExclusions.isEmpty();

        /*
         * The current exclusion contract has only code and description.
         * It does not contain structured action or violation mappings.
         * Therefore, automatic exclusion matching is intentionally disabled.
         */
        boolean automaticExclusionDetected =
                false;

        boolean manualExclusionReviewRequired =
                activeExclusionsPresent;

        BigDecimal claimedAmount =
                requireNonNegative(
                        claim.getClaimedAmount(),
                        "Claimed amount"
                );

        BigDecimal reportedLossAmount =
                requireNonNegative(
                        incident.getLossAmount(),
                        "Reported loss amount"
                );

        BigDecimal overallCoverageLimit =
                requireNonNegative(
                        policyCoverage.coverageLimit(),
                        "Overall policy coverage limit"
                );

        BigDecimal applicableCoverageLimit =
                determineApplicableCoverageLimit(
                        overallCoverageLimit,
                        matchingCoverage
                );

        BigDecimal policyDeductible =
                policyCoverage.deductible() == null
                        ? ZERO
                        : requireNonNegative(
                        policyCoverage.deductible(),
                        "Policy deductible"
                );

        BigDecimal grossEligibleAmount =
                minimum(
                        claimedAmount,
                        reportedLossAmount,
                        applicableCoverageLimit,
                        overallCoverageLimit
                );

        BigDecimal deductibleApplied =
                policyDeductible.min(
                        grossEligibleAmount
                );

        BigDecimal maximumPayableAmount =
                grossEligibleAmount
                        .subtract(
                                deductibleApplied
                        )
                        .max(ZERO);

        boolean policyValidOnActionDate =
                policyValidation.valid()
                        && policyValidation
                        .activeOnActionDate();

        boolean eligible =
                actionViolationFound
                        && actionMarkedClaimEligible
                        && policyValidation
                        .customerMatches()
                        && policyValidation
                        .twinMatches()
                        && policyValidOnActionDate
                        && coverageMatched
                        && !automaticExclusionDetected
                        && maximumPayableAmount
                        .compareTo(ZERO) > 0;

        String explanation =
                buildExplanation(
                        actionViolationFound,
                        actionMarkedClaimEligible,
                        policyValidation,
                        coverageMatched,
                        activeExclusionsPresent,
                        automaticExclusionDetected,
                        manualExclusionReviewRequired,
                        grossEligibleAmount,
                        deductibleApplied,
                        maximumPayableAmount,
                        eligible
                );

        return new ClaimReviewContextResponseDTO(
                claim.getClaimId(),
                claim.getClaimNumber(),
                claim.getIncidentId(),
                claim.getActionId(),
                claim.getPolicyId(),
                claim.getCustomerId(),
                claim.getTwinId(),

                actionEvidence.actionType(),
                incident
                        .getIncidentType()
                        .name(),

                matchingViolation == null
                        ? null
                        : matchingViolation
                        .violationType(),

                matchingViolation == null
                        ? null
                        : matchingViolation
                        .severity(),

                incident
                        .getActionOccurredAt()
                        .toLocalDate(),

                actionViolationFound,
                actionMarkedClaimEligible,

                policyValidation
                        .customerMatches(),

                policyValidation
                        .twinMatches(),

                policyValidOnActionDate,

                coverageMatched,

                activeExclusionsPresent,
                automaticExclusionDetected,
                manualExclusionReviewRequired,

                claimedAmount,
                reportedLossAmount,

                overallCoverageLimit,
                applicableCoverageLimit,

                policyDeductible,
                deductibleApplied,

                grossEligibleAmount,
                maximumPayableAmount,

                eligible,

                explanation,

                matchingCoverages,
                activeExclusions
        );
    }

    private void validateResponseIdentity(
            Claim claim,
            AiActionEvidenceResponseDTO actionEvidence,
            PolicyCoverageResponseDTO policyCoverage,
            PolicyIncidentValidationResponseDTO policyValidation) {

        if (actionEvidence == null) {

            throw new DownstreamValidationException(
                    "AI action evidence is unavailable"
            );
        }

        if (!claim.getActionId()
                .equals(
                        actionEvidence.actionId()
                )) {

            throw new DownstreamValidationException(
                    "AI action evidence does not match the claim"
            );
        }

        if (!claim.getCustomerId()
                .equals(
                        actionEvidence.customerId()
                )) {

            throw new DownstreamValidationException(
                    "AI action Customer does not match the claim"
            );
        }

        if (!claim.getTwinId()
                .equals(
                        actionEvidence.twinId()
                )) {

            throw new DownstreamValidationException(
                    "AI action Twin does not match the claim"
            );
        }

        if (!claim.getPolicyId()
                .equals(
                        actionEvidence.policyId()
                )) {

            throw new DownstreamValidationException(
                    "AI action policy does not match the claim"
            );
        }

        if (!Boolean.TRUE.equals(
                actionEvidence.insured()
        )) {

            throw new DownstreamValidationException(
                    "The AI action was not marked as insured"
            );
        }

        if (!"VIOLATION_DETECTED".equals(
                actionEvidence.actionStatus()
        )) {

            throw new DownstreamValidationException(
                    "The AI action does not have a violation result"
            );
        }

        if (policyCoverage == null
                || !claim.getPolicyId()
                .equals(
                        policyCoverage.policyId()
                )) {

            throw new DownstreamValidationException(
                    "Policy coverage does not match the claim"
            );
        }

        if (!claim.getCustomerId()
                .equals(
                        policyCoverage.customerId()
                )) {

            throw new DownstreamValidationException(
                    "Policy Customer does not match the claim"
            );
        }

        if (!claim.getTwinId()
                .equals(
                        policyCoverage.twinId()
                )) {

            throw new DownstreamValidationException(
                    "Policy Twin does not match the claim"
            );
        }

        if (policyValidation == null
                || !claim.getPolicyId()
                .equals(
                        policyValidation.policyId()
                )) {

            throw new DownstreamValidationException(
                    "Historical policy validation does not match the claim"
            );
        }
    }

    private ActionViolationEvidenceResponseDTO
    findMatchingViolation(
            Incident incident,
            AiActionEvidenceResponseDTO actionEvidence) {

        if (actionEvidence.violations() == null) {
            return null;
        }

        return actionEvidence
                .violations()
                .stream()
                .filter(violation ->
                        Objects.equals(
                                incident
                                        .getIncidentType()
                                        .name(),
                                violation.violationType()
                        )
                )
                .findFirst()
                .orElse(null);
    }

    private PolicyCoverageRuleResponseDTO
    findMatchingCoverage(
            Incident incident,
            AiActionEvidenceResponseDTO actionEvidence,
            PolicyCoverageResponseDTO policyCoverage) {

        if (policyCoverage.coverages() == null) {
            return null;
        }

        return policyCoverage
                .coverages()
                .stream()
                .filter(coverage ->
                        Boolean.TRUE.equals(
                                coverage.active()
                        )
                )
                .filter(coverage ->
                        Objects.equals(
                                actionEvidence.actionType(),
                                coverage.actionType()
                        )
                )
                .filter(coverage ->
                        Objects.equals(
                                incident
                                        .getIncidentType()
                                        .name(),
                                coverage.violationType()
                        )
                )
                .findFirst()
                .orElse(null);
    }

    private BigDecimal determineApplicableCoverageLimit(
            BigDecimal overallCoverageLimit,
            PolicyCoverageRuleResponseDTO matchingCoverage) {

        if (matchingCoverage == null
                || matchingCoverage.coverageLimit()
                == null) {

            return overallCoverageLimit;
        }

        return matchingCoverage
                .coverageLimit()
                .min(
                        overallCoverageLimit
                );
    }

    private BigDecimal requireNonNegative(
            BigDecimal value,
            String fieldName) {

        if (value == null) {

            throw new DownstreamValidationException(
                    fieldName + " is unavailable"
            );
        }

        if (value.compareTo(ZERO) < 0) {

            throw new DownstreamValidationException(
                    fieldName + " cannot be negative"
            );
        }

        return value;
    }

    private BigDecimal minimum(
            BigDecimal first,
            BigDecimal second,
            BigDecimal third,
            BigDecimal fourth) {

        return first
                .min(second)
                .min(third)
                .min(fourth);
    }

    private String buildExplanation(
            boolean actionViolationFound,
            boolean actionMarkedClaimEligible,
            PolicyIncidentValidationResponseDTO policyValidation,
            boolean coverageMatched,
            boolean activeExclusionsPresent,
            boolean automaticExclusionDetected,
            boolean manualExclusionReviewRequired,
            BigDecimal grossEligibleAmount,
            BigDecimal deductibleApplied,
            BigDecimal maximumPayableAmount,
            boolean eligible) {

        return "Violation found: "
                + actionViolationFound
                + ", violation marked claim eligible: "
                + actionMarkedClaimEligible
                + ", Customer matches policy: "
                + policyValidation.customerMatches()
                + ", AI Twin matches policy: "
                + policyValidation.twinMatches()
                + ", policy valid on action date: "
                + policyValidation.activeOnActionDate()
                + ", coverage matched: "
                + coverageMatched
                + ", active exclusions present: "
                + activeExclusionsPresent
                + ", automatic exclusion detected: "
                + automaticExclusionDetected
                + ", manual exclusion review required: "
                + manualExclusionReviewRequired
                + ", gross eligible amount: "
                + grossEligibleAmount
                + ", deductible applied: "
                + deductibleApplied
                + ", maximum payable amount: "
                + maximumPayableAmount
                + ", automatic eligibility result: "
                + eligible
                + ".";
    }

    private Mono<Claim> findAssignedClaim(
            Long claimId,
            Long adjusterUserId) {

        return claimRepo
                .findById(claimId)
                .switchIfEmpty(
                        Mono.error(
                                new ClaimNotFoundException(
                                        "Claim not found with ID: "
                                                + claimId
                                )
                        )
                )
                .flatMap(claim -> {

                    if (claim
                            .getAssignedAdjusterId()
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

                    return Mono.just(claim);
                });
    }

    private Mono<Incident> findIncident(
            Long incidentId) {

        return incidentRepo
                .findById(incidentId)
                .switchIfEmpty(
                        Mono.error(
                                new IncidentNotFoundException(
                                        "Incident linked to the claim "
                                                + "was not found"
                                )
                        )
                );
    }

    private Mono<AiActionEvidenceResponseDTO>
    getActionEvidence(
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
                        status ->
                                status.value() == 404,
                        response ->
                                Mono.error(
                                        new DownstreamValidationException(
                                                "AI action evidence was not found"
                                        )
                                )
                )
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        response ->
                                Mono.error(
                                        new DownstreamValidationException(
                                                "AI action evidence could not "
                                                        + "be retrieved"
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

    private Mono<PolicyCoverageResponseDTO>
    getPolicyCoverage(
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
                        status ->
                                status.value() == 404,
                        response ->
                                Mono.error(
                                        new DownstreamValidationException(
                                                "Policy coverage was not found"
                                        )
                                )
                )
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        response ->
                                Mono.error(
                                        new DownstreamValidationException(
                                                "Policy coverage could not "
                                                        + "be retrieved"
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

    private void validateUserId(
            Long userId) {

        if (userId == null) {

            throw new InvalidRequestException(
                    "Claims Adjuster user ID is required"
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