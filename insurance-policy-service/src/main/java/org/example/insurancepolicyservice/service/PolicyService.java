package org.example.insurancepolicyservice.service;

// Issues an insurance policy after a Customer accepts an approved proposal.
// It also provides policy views, claim-coverage details, incident validation,
// and administrative policy-status management.

import org.example.insurancepolicyservice.dto.request.PolicyStatusRequestDTO;
import org.example.insurancepolicyservice.dto.response.ActivePolicyResponseDTO;
import org.example.insurancepolicyservice.dto.response.PolicyCoverageResponseDTO;
import org.example.insurancepolicyservice.dto.response.PolicyDetailsResponseDTO;
import org.example.insurancepolicyservice.dto.response.PolicyIncidentValidationResponseDTO;
import org.example.insurancepolicyservice.dto.response.PolicyResponseDTO;
import org.example.insurancepolicyservice.dto.response.ProductCoverageResponseDTO;
import org.example.insurancepolicyservice.dto.response.ProductExclusionResponseDTO;
import org.example.insurancepolicyservice.enums.PolicyStatus;
import org.example.insurancepolicyservice.exception.InsuranceProductNotFoundException;
import org.example.insurancepolicyservice.exception.InvalidPolicyApplicationStateException;
import org.example.insurancepolicyservice.exception.InvalidRequestException;
import org.example.insurancepolicyservice.exception.PolicyNotFoundException;
import org.example.insurancepolicyservice.exception.ResourceAccessDeniedException;
import org.example.insurancepolicyservice.model.InsuranceProduct;
import org.example.insurancepolicyservice.model.Policy;
import org.example.insurancepolicyservice.model.PolicyApplication;
import org.example.insurancepolicyservice.repo.InsuranceProductRepo;
import org.example.insurancepolicyservice.repo.PolicyRepo;
import org.example.insurancepolicyservice.repo.ProductCoverageRepo;
import org.example.insurancepolicyservice.repo.ProductExclusionRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.example.insurancepolicyservice.dto.response.AiTwinRiskProfileResponseDTO;
import org.example.insurancepolicyservice.dto.response.PolicyPermissionSnapshotResponseDTO;
import org.example.insurancepolicyservice.dto.response.PolicyTwinSnapshotResponseDTO;
import org.example.insurancepolicyservice.model.PolicyPermissionSnapshot;
import org.example.insurancepolicyservice.model.PolicyTwinSnapshot;
import org.example.insurancepolicyservice.repo.PolicyPermissionSnapshotRepo;
import org.example.insurancepolicyservice.repo.PolicyTwinSnapshotRepo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PolicyService {

    private final PolicyRepo policyRepo;
    private final InsuranceProductRepo productRepo;
    private final ProductCoverageRepo coverageRepo;
    private final ProductExclusionRepo exclusionRepo;
    private final PolicyApplicationService applicationService;

    private final PolicyTwinSnapshotRepo twinSnapshotRepo;
    private final PolicyPermissionSnapshotRepo permissionSnapshotRepo;
    private final WebClient.Builder webClientBuilder;

    public PolicyService(
            PolicyRepo policyRepo,
            InsuranceProductRepo productRepo,
            ProductCoverageRepo coverageRepo,
            ProductExclusionRepo exclusionRepo,
            PolicyApplicationService applicationService,
            PolicyTwinSnapshotRepo twinSnapshotRepo,
            PolicyPermissionSnapshotRepo permissionSnapshotRepo,
            WebClient.Builder webClientBuilder) {

        this.policyRepo = policyRepo;
        this.productRepo = productRepo;
        this.coverageRepo = coverageRepo;
        this.exclusionRepo = exclusionRepo;
        this.applicationService = applicationService;
        this.twinSnapshotRepo = twinSnapshotRepo;
        this.permissionSnapshotRepo =
                permissionSnapshotRepo;
        this.webClientBuilder = webClientBuilder;
    }

    /*
     * CUSTOMER:
     * Accept an approved proposal and issue the final policy.
     *
     * The transaction ensures that policy creation and application
     * acceptance succeed or fail together in the same database.
     */
    @Transactional
    public Mono<PolicyResponseDTO> acceptApplication(
            Long applicationId,
            Long customerId,
            String jwtToken) {

        validateCustomerId(
                customerId
        );

        validateJwtToken(
                jwtToken
        );

        return applicationService
                .getApprovedOwnedApplication(
                        applicationId,
                        customerId
                )
                .flatMap(application ->
                        policyRepo
                                .existsByApplicationId(
                                        applicationId
                                )
                                .flatMap(policyExists -> {

                                    if (policyExists) {

                                        return Mono.error(
                                                new InvalidPolicyApplicationStateException(
                                                        "A policy has already been "
                                                                + "issued for this application"
                                                )
                                        );
                                    }

                                    return policyRepo
                                            .existsByTwinIdAndStatus(
                                                    application.getTwinId(),
                                                    PolicyStatus.ACTIVE
                                            );
                                })
                                .flatMap(activePolicyExists -> {

                                    if (activePolicyExists) {

                                        return Mono.error(
                                                new InvalidPolicyApplicationStateException(
                                                        "The AI Twin already has "
                                                                + "an active policy"
                                                )
                                        );
                                    }

                                    return getAiTwinRiskProfile(
                                            application.getTwinId(),
                                            jwtToken
                                    )
                                            .flatMap(twinProfile -> {

                                                validateSnapshotProfile(
                                                        application,
                                                        twinProfile
                                                );

                                                return createPolicy(
                                                        application
                                                )
                                                        .flatMap(policy ->
                                                                savePolicySnapshot(
                                                                        policy,
                                                                        twinProfile
                                                                )
                                                                        .thenReturn(
                                                                                policy
                                                                        )
                                                        );
                                            });
                                })
                                .flatMap(policy ->
                                        applicationService
                                                .markApplicationAccepted(
                                                        application
                                                )
                                                .thenReturn(
                                                        policy
                                                )
                                )
                )
                .map(
                        this::convertToPolicyResponse
                );
    }

    private Mono<AiTwinRiskProfileResponseDTO>
    getAiTwinRiskProfile(
            Long twinId,
            String jwtToken) {

        String url =
                "http://AI-TWIN-SERVICE"
                        + "/internal/ai-twins/"
                        + twinId
                        + "/risk-profile";

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
                        HttpStatusCode::is4xxClientError,
                        response ->
                                Mono.error(
                                        new InvalidPolicyApplicationStateException(
                                                "AI Twin risk profile could not "
                                                        + "be captured for policy issuance"
                                        )
                                )
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response ->
                                Mono.error(
                                        new InvalidPolicyApplicationStateException(
                                                "AI Twin Service is unavailable. "
                                                        + "Policy issuance cannot continue "
                                                        + "without an underwriting snapshot."
                                        )
                                )
                )
                .bodyToMono(
                        AiTwinRiskProfileResponseDTO.class
                )
                .switchIfEmpty(
                        Mono.error(
                                new InvalidPolicyApplicationStateException(
                                        "AI Twin risk profile response is empty"
                                )
                        )
                )
                .onErrorMap(
                        WebClientRequestException.class,
                        exception ->
                                new InvalidPolicyApplicationStateException(
                                        "AI Twin Service is unavailable. "
                                                + "Policy issuance cannot continue "
                                                + "without an underwriting snapshot."
                                )
                );
    }


    private void validateSnapshotProfile(
            PolicyApplication application,
            AiTwinRiskProfileResponseDTO twinProfile) {

        if (twinProfile == null) {

            throw new InvalidPolicyApplicationStateException(
                    "AI Twin risk profile is unavailable"
            );
        }

        if (!application
                .getTwinId()
                .equals(
                        twinProfile.twinId()
                )) {

            throw new InvalidPolicyApplicationStateException(
                    "AI Twin risk profile does not match "
                            + "the approved application"
            );
        }

        if (!application
                .getCustomerId()
                .equals(
                        twinProfile.customerId()
                )) {

            throw new InvalidPolicyApplicationStateException(
                    "AI Twin risk profile Customer does not match "
                            + "the approved application"
            );
        }

        if (!"ACTIVE".equals(
                twinProfile.status()
        )) {

            throw new InvalidPolicyApplicationStateException(
                    "Only an ACTIVE AI Twin can receive "
                            + "an issued policy"
            );
        }

        if (twinProfile.twinName() == null
                || twinProfile.twinName().isBlank()) {

            throw new InvalidPolicyApplicationStateException(
                    "AI Twin name is unavailable"
            );
        }

        if (twinProfile.autonomyLevel() == null
                || twinProfile.autonomyLevel().isBlank()) {

            throw new InvalidPolicyApplicationStateException(
                    "AI Twin autonomy level is unavailable"
            );
        }

        if (twinProfile.transactionLimit() == null
                || twinProfile.approvalThreshold() == null) {

            throw new InvalidPolicyApplicationStateException(
                    "AI Twin underwriting configuration is incomplete"
            );
        }

        if (twinProfile.permissions() == null
                || twinProfile.permissions().isEmpty()) {

            throw new InvalidPolicyApplicationStateException(
                    "AI Twin permission configuration is unavailable"
            );
        }
    }

    private Mono<Void> savePolicySnapshot(
            Policy policy,
            AiTwinRiskProfileResponseDTO twinProfile) {

        PolicyTwinSnapshot twinSnapshot =
                new PolicyTwinSnapshot();

        twinSnapshot.setPolicyId(
                policy.getPolicyId()
        );

        twinSnapshot.setTwinId(
                twinProfile.twinId()
        );

        twinSnapshot.setTwinName(
                twinProfile.twinName()
        );

        twinSnapshot.setAutonomyLevel(
                twinProfile.autonomyLevel()
        );

        twinSnapshot.setTransactionLimit(
                twinProfile.transactionLimit()
        );

        twinSnapshot.setApprovalThreshold(
                twinProfile.approvalThreshold()
        );

        twinSnapshot.setTwinStatus(
                twinProfile.status()
        );

        twinSnapshot.setCapturedAt(
                LocalDateTime.now()
        );

        return twinSnapshotRepo
                .save(
                        twinSnapshot
                )
                .thenMany(
                        Flux.fromIterable(
                                twinProfile.permissions()
                        )
                )
                .map(permission -> {

                    PolicyPermissionSnapshot snapshot =
                            new PolicyPermissionSnapshot();

                    snapshot.setPolicyId(
                            policy.getPolicyId()
                    );

                    snapshot.setActionType(
                            permission.actionType()
                    );

                    snapshot.setPermissionLevel(
                            permission.permissionLevel()
                    );

                    snapshot.setActionLimit(
                            permission.actionLimit()
                    );

                    snapshot.setActive(
                            permission.active()
                    );

                    return snapshot;
                })
                .collectList()
                .flatMap(permissionSnapshots -> {

                    if (permissionSnapshots.isEmpty()) {
                        return Mono.empty();
                    }

                    return permissionSnapshotRepo
                            .saveAll(
                                    permissionSnapshots
                            )
                            .then();
                });
    }


    private Mono<PolicyTwinSnapshotResponseDTO>
    buildPolicyTwinSnapshot(
            Long policyId) {

        return twinSnapshotRepo
                .findByPolicyId(
                        policyId
                )
                .flatMap(twinSnapshot ->
                        permissionSnapshotRepo
                                .findAllByPolicyIdOrderByActionTypeAsc(
                                        policyId
                                )
                                .map(permission ->
                                        new PolicyPermissionSnapshotResponseDTO(
                                                permission.getActionType(),
                                                permission.getPermissionLevel(),
                                                permission.getActionLimit(),
                                                permission.getActive()
                                        )
                                )
                                .collectList()
                                .map(permissions ->
                                        new PolicyTwinSnapshotResponseDTO(
                                                twinSnapshot.getTwinId(),
                                                twinSnapshot.getTwinName(),
                                                twinSnapshot.getAutonomyLevel(),
                                                twinSnapshot.getTransactionLimit(),
                                                twinSnapshot.getApprovalThreshold(),
                                                twinSnapshot.getTwinStatus(),
                                                twinSnapshot.getCapturedAt(),
                                                permissions
                                        )
                                )
                );
    }

    /*
     * CUSTOMER:
     * Return all policies belonging to the logged-in Customer.
     */
    public Flux<PolicyResponseDTO> getMyPolicies(
            Long customerId) {

        validateCustomerId(customerId);

        return policyRepo
                .findAllByCustomerId(customerId)
                .map(this::convertToPolicyResponse);
    }

    /*
     * CUSTOMER OWNER / UNDERWRITER / ADMIN:
     * Return complete policy details.
     *
     * Customer ownership is checked here because role-based route
     * security alone cannot verify which Customer owns the policy.
     */
    public Mono<PolicyDetailsResponseDTO> getPolicyDetails(
            Long policyId,
            String role,
            Long customerId) {

        return findPolicyById(policyId)
                .flatMap(policy -> {

                    validatePolicyAccess(
                            policy,
                            role,
                            customerId
                    );

                    return buildPolicyDetails(policy);
                });
    }

    /*
     * CLAIMS_ADJUSTER / ADMIN:
     * Return the policy terms required during claim evaluation.
     *
     * Coverage rules and exclusions are loaded separately because
     * R2DBC does not automatically load table relationships.
     */
    public Mono<PolicyCoverageResponseDTO> getPolicyCoverage(
            Long policyId) {

        return findPolicyById(policyId)
                .flatMap(this::buildPolicyCoverage);
    }

    /*
     * ADMIN:
     * Return every issued policy, regardless of current status.
     */
    public Flux<PolicyResponseDTO> getAllPolicies() {

        return policyRepo
                .findAll()
                .map(this::convertToPolicyResponse);
    }

    /*
     * ADMIN:
     * Cancel or expire an issued policy with a stored reason.
     *
     * ACTIVE is rejected here because this administrative operation
     * is intended for deactivation rather than policy reactivation.
     */
    public Mono<PolicyResponseDTO> updatePolicyStatus(
            Long policyId,
            PolicyStatusRequestDTO request) {

        if (request.status() == PolicyStatus.ACTIVE) {
            return Mono.error(
                    new InvalidRequestException(
                            "This operation cannot reactivate a policy. "
                                    + "Use CANCELLED or EXPIRED."
                    )
            );
        }

        return findPolicyById(policyId)
                .flatMap(policy -> {

                    if (policy.getStatus()
                            == request.status()) {

                        return Mono.just(policy);
                    }

                    if (policy.getStatus()
                            != PolicyStatus.ACTIVE) {

                        return Mono.error(
                                new InvalidPolicyApplicationStateException(
                                        "Only an ACTIVE policy can be "
                                                + "cancelled or expired"
                                )
                        );
                    }

                    policy.setStatus(
                            request.status()
                    );

                    policy.setCancellationReason(
                            request.reason().trim()
                    );

                    policy.setUpdatedAt(
                            LocalDateTime.now()
                    );

                    return policyRepo.save(policy);
                })
                .map(this::convertToPolicyResponse);
    }

    /*
     * INTERNAL:
     * Find whether an AI Twin currently has active insurance.
     *
     * AI Action Service can use this endpoint before deciding whether
     * a violation should become an insurance-related incident.
     */
    public Mono<ActivePolicyResponseDTO> getActivePolicyForTwin(
            Long twinId) {

        return policyRepo
                .findFirstByTwinIdAndStatus(
                        twinId,
                        PolicyStatus.ACTIVE
                )
                .map(policy ->
                        new ActivePolicyResponseDTO(
                                policy.getPolicyId(),
                                policy.getCustomerId(),
                                policy.getTwinId(),
                                policy.getStatus(),
                                policy.getStartDate(),
                                policy.getEndDate(),
                                isPolicyActiveToday(policy)
                        )
                )
                .switchIfEmpty(
                        Mono.just(
                                new ActivePolicyResponseDTO(
                                        null,
                                        null,
                                        twinId,
                                        null,
                                        null,
                                        null,
                                        false
                                )
                        )
                );
    }

    /*
     * INTERNAL:
     * Return claim coverage using a service-to-service route.
     *
     * The response is the same coverage contract used by authorized
     * Claims Adjuster operations.
     */
    public Mono<PolicyCoverageResponseDTO>
    getInternalPolicyCoverage(
            Long policyId) {

        return getPolicyCoverage(policyId);
    }

    /*
     * INTERNAL:
     * Validate whether the policy covered the Customer and AI Twin
     * on the date when the reported AI action occurred.
     */
    public Mono<PolicyIncidentValidationResponseDTO>
    validatePolicyForIncident(
            Long policyId,
            Long customerId,
            Long twinId,
            LocalDate actionDate) {

        if (customerId == null) {
            return Mono.error(
                    new InvalidRequestException(
                            "customerId is required"
                    )
            );
        }

        if (twinId == null) {
            return Mono.error(
                    new InvalidRequestException(
                            "twinId is required"
                    )
            );
        }

        if (actionDate == null) {
            return Mono.error(
                    new InvalidRequestException(
                            "actionDate is required"
                    )
            );
        }

        return findPolicyById(policyId)
                .map(policy -> {

                    boolean customerMatches =
                            policy.getCustomerId()
                                    .equals(customerId);

                    boolean twinMatches =
                            policy.getTwinId()
                                    .equals(twinId);

                    boolean activeOnActionDate =
                            !actionDate.isBefore(
                                    policy.getStartDate()
                            )
                                    && !actionDate.isAfter(
                                    policy.getEndDate()
                            );

                    boolean valid =
                            customerMatches
                                    && twinMatches
                                    && activeOnActionDate
                                    && policy.getStatus()
                                    == PolicyStatus.ACTIVE;

                    return new PolicyIncidentValidationResponseDTO(
                            policy.getPolicyId(),
                            policy.getCustomerId(),
                            policy.getTwinId(),
                            policy.getStatus(),
                            policy.getStartDate(),
                            policy.getEndDate(),
                            actionDate,
                            customerMatches,
                            twinMatches,
                            activeOnActionDate,
                            valid
                    );
                });
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

    /*
     * Builds and saves a one-year policy using the proposal values
     * recorded by the Underwriter on the approved application.
     */
    private Mono<Policy> createPolicy(
            PolicyApplication application) {

        validateApprovedProposal(application);

        LocalDate currentDate =
                LocalDate.now();

        LocalDateTime currentTime =
                LocalDateTime.now();

        Policy policy = new Policy();

        policy.setPolicyNumber(
                generatePolicyNumber()
        );

        policy.setApplicationId(
                application.getApplicationId()
        );

        policy.setCustomerId(
                application.getCustomerId()
        );

        policy.setTwinId(
                application.getTwinId()
        );

        policy.setProductId(
                application.getProductId()
        );

        policy.setPremium(
                application.getProposedPremium()
        );

        policy.setCoverageLimit(
                application.getProposedCoverageLimit()
        );

        policy.setDeductible(
                application.getProposedDeductible()
        );

        policy.setStartDate(currentDate);

        policy.setEndDate(
                currentDate.plusYears(1)
                        .minusDays(1)
        );

        policy.setStatus(
                PolicyStatus.ACTIVE
        );

        policy.setCancellationReason(null);
        policy.setCreatedAt(currentTime);
        policy.setUpdatedAt(currentTime);

        return policyRepo.save(policy);
    }

    /*
     * Verifies that all financial terms required for policy issuance
     * were recorded during Underwriter approval.
     */
    private void validateApprovedProposal(
            PolicyApplication application) {

        if (application.getProposedPremium() == null
                || application
                .getProposedCoverageLimit() == null
                || application
                .getProposedDeductible() == null) {

            throw new InvalidPolicyApplicationStateException(
                    "Approved proposal financial terms "
                            + "are incomplete"
            );
        }
    }

    /*
     * Combines Policy and InsuranceProduct records into one details DTO.
     */
    // Builds a complete issued-policy contract using local policy and product data.
// Active coverage rules and exclusions are sorted for consistent display.

    private Mono<PolicyDetailsResponseDTO> buildPolicyDetails(
            Policy policy) {

        Mono<InsuranceProduct> productMono =
                findProductById(
                        policy.getProductId()
                );

        Mono<java.util.List<ProductCoverageResponseDTO>>
                coveragesMono =
                coverageRepo
                        .findAllByProductIdAndActiveTrue(
                                policy.getProductId()
                        )
                        .sort(
                                (first, second) -> {

                                    int actionComparison =
                                            first.getActionType()
                                                    .name()
                                                    .compareTo(
                                                            second.getActionType()
                                                                    .name()
                                                    );

                                    if (actionComparison != 0) {
                                        return actionComparison;
                                    }

                                    return first.getViolationType()
                                            .name()
                                            .compareTo(
                                                    second.getViolationType()
                                                            .name()
                                            );
                                }
                        )
                        .map(coverage ->
                                new ProductCoverageResponseDTO(
                                        coverage.getCoverageId(),
                                        coverage.getProductId(),
                                        coverage.getActionType(),
                                        coverage.getViolationType(),
                                        coverage.getCoverageLimit(),
                                        coverage.getActive(),
                                        coverage.getDescription()
                                )
                        )
                        .collectList();

        Mono<java.util.List<ProductExclusionResponseDTO>>
                exclusionsMono =
                exclusionRepo
                        .findAllByProductIdAndActiveTrue(
                                policy.getProductId()
                        )
                        .sort(
                                (first, second) ->
                                        first.getExclusionCode()
                                                .compareTo(
                                                        second.getExclusionCode()
                                                )
                        )
                        .map(exclusion ->
                                new ProductExclusionResponseDTO(
                                        exclusion.getExclusionId(),
                                        exclusion.getProductId(),
                                        exclusion.getExclusionCode(),
                                        exclusion.getDescription(),
                                        exclusion.getActive()
                                )
                        )
                        .collectList();

        Mono<PolicyTwinSnapshotResponseDTO>
                snapshotMono =
                buildPolicyTwinSnapshot(
                        policy.getPolicyId()
                )
                        .defaultIfEmpty(
                                new PolicyTwinSnapshotResponseDTO(
                                        policy.getTwinId(),
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        java.util.List.of()
                                )
                        );

        return Mono.zip(
                        productMono,
                        coveragesMono,
                        exclusionsMono,
                        snapshotMono
                )
                .map(result -> {

                    InsuranceProduct product =
                            result.getT1();

                    return new PolicyDetailsResponseDTO(
                            policy.getPolicyId(),
                            policy.getPolicyNumber(),
                            policy.getApplicationId(),
                            policy.getCustomerId(),
                            policy.getTwinId(),
                            policy.getProductId(),

                            product.getProductCode(),
                            product.getProductName(),
                            product.getDescription(),

                            policy.getPremium(),
                            policy.getCoverageLimit(),
                            policy.getDeductible(),

                            policy.getStartDate(),
                            policy.getEndDate(),
                            policy.getStatus(),

                            policy.getCancellationReason(),

                            result.getT2(),
                            result.getT3(),

                            result.getT4(),

                            policy.getCreatedAt(),
                            policy.getUpdatedAt()
                    );
                });
    }

    /*
     * Loads active product coverages and exclusions and combines them
     * with the issued policy terms for Claims Service.
     */
    private Mono<PolicyCoverageResponseDTO> buildPolicyCoverage(
            Policy policy) {

        Mono<java.util.List<ProductCoverageResponseDTO>>
                coveragesMono =
                coverageRepo
                        .findAllByProductIdAndActiveTrue(
                                policy.getProductId()
                        )
                        .map(coverage ->
                                new ProductCoverageResponseDTO(
                                        coverage.getCoverageId(),
                                        coverage.getProductId(),
                                        coverage.getActionType(),
                                        coverage.getViolationType(),
                                        coverage.getCoverageLimit(),
                                        coverage.getActive(),
                                        coverage.getDescription()
                                )
                        )
                        .collectList();

        Mono<java.util.List<ProductExclusionResponseDTO>>
                exclusionsMono =
                exclusionRepo
                        .findAllByProductIdAndActiveTrue(
                                policy.getProductId()
                        )
                        .map(exclusion ->
                                new ProductExclusionResponseDTO(
                                        exclusion.getExclusionId(),
                                        exclusion.getProductId(),
                                        exclusion.getExclusionCode(),
                                        exclusion.getDescription(),
                                        exclusion.getActive()
                                )
                        )
                        .collectList();

        return Mono.zip(
                        coveragesMono,
                        exclusionsMono
                )
                .map(result -> {

                    boolean valid =
                            policy.getStatus()
                                    == PolicyStatus.ACTIVE
                                    && isPolicyActiveToday(policy);

                    return new PolicyCoverageResponseDTO(
                            policy.getPolicyId(),
                            policy.getCustomerId(),
                            policy.getTwinId(),
                            policy.getProductId(),
                            policy.getStatus(),
                            policy.getStartDate(),
                            policy.getEndDate(),
                            policy.getCoverageLimit(),
                            policy.getDeductible(),
                            result.getT1(),
                            result.getT2(),
                            valid
                    );
                });
    }

    private Mono<Policy> findPolicyById(
            Long policyId) {

        return policyRepo
                .findById(policyId)
                .switchIfEmpty(
                        Mono.error(
                                new PolicyNotFoundException(
                                        "Policy not found with ID: "
                                                + policyId
                                )
                        )
                );
    }

    private Mono<InsuranceProduct> findProductById(
            Long productId) {

        return productRepo
                .findById(productId)
                .switchIfEmpty(
                        Mono.error(
                                new InsuranceProductNotFoundException(
                                        "Insurance product not found "
                                                + "with ID: "
                                                + productId
                                )
                        )
                );
    }

    /*
     * Customers can view only their own policies.
     * Underwriters and Admins may view any policy through authorized routes.
     */
    private void validatePolicyAccess(
            Policy policy,
            String role,
            Long customerId) {

        if ("CUSTOMER".equals(role)
                && !policy.getCustomerId()
                .equals(customerId)) {

            throw new ResourceAccessDeniedException(
                    "You cannot access another "
                            + "Customer's policy"
            );
        }
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

    private boolean isPolicyActiveToday(
            Policy policy) {

        LocalDate currentDate =
                LocalDate.now();

        return policy.getStatus()
                == PolicyStatus.ACTIVE
                && !currentDate.isBefore(
                policy.getStartDate()
        )
                && !currentDate.isAfter(
                policy.getEndDate()
        );
    }

    /*
     * Creates a readable and highly unique policy number.
     *
     * Example:
     * TSP-20260918-A1B2C3D4
     */
    private String generatePolicyNumber() {

        String randomPart =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 8)
                        .toUpperCase();

        return "TSP-"
                + LocalDate.now()
                .toString()
                .replace("-", "")
                + "-"
                + randomPart;
    }

    private PolicyResponseDTO convertToPolicyResponse(
            Policy policy) {

        return new PolicyResponseDTO(
                policy.getPolicyId(),
                policy.getPolicyNumber(),
                policy.getApplicationId(),
                policy.getCustomerId(),
                policy.getTwinId(),
                policy.getProductId(),
                policy.getPremium(),
                policy.getCoverageLimit(),
                policy.getDeductible(),
                policy.getStartDate(),
                policy.getEndDate(),
                policy.getStatus(),
                policy.getCancellationReason()
        );
    }
}