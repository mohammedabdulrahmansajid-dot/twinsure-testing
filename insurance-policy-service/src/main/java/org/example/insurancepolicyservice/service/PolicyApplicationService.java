package org.example.insurancepolicyservice.service;

// Manages policy applications from submission through underwriting decisions.
// The service validates Customers, AI Twins, products, active policies,
// workflow transitions, risk assessments, and Customer ownership.

import org.example.insurancepolicyservice.dto.request.CreatePolicyApplicationRequestDTO;
import org.example.insurancepolicyservice.dto.request.PolicyApprovalRequestDTO;
import org.example.insurancepolicyservice.dto.request.PolicyChangesRequestDTO;
import org.example.insurancepolicyservice.dto.request.PolicyRejectionRequestDTO;
import org.example.insurancepolicyservice.dto.request.RiskAssessmentRequestDTO;
import org.example.insurancepolicyservice.dto.response.AiTwinRiskProfileResponseDTO;
import org.example.insurancepolicyservice.dto.response.AiTwinValidationResponseDTO;
import org.example.insurancepolicyservice.dto.response.CustomerValidationResponseDTO;
import org.example.insurancepolicyservice.dto.response.InsuranceProductDetailsResponseDTO;
import org.example.insurancepolicyservice.dto.response.PolicyApplicationDetailsResponseDTO;
import org.example.insurancepolicyservice.dto.response.PolicyApplicationResponseDTO;
import org.example.insurancepolicyservice.dto.response.ProductCoverageResponseDTO;
import org.example.insurancepolicyservice.dto.response.ProductExclusionResponseDTO;
import org.example.insurancepolicyservice.dto.response.RiskAssessmentResponseDTO;
import org.example.insurancepolicyservice.enums.PolicyApplicationStatus;
import org.example.insurancepolicyservice.enums.PolicyStatus;
import org.example.insurancepolicyservice.enums.ProductStatus;
import org.example.insurancepolicyservice.enums.UnderwritingRecommendation;
import org.example.insurancepolicyservice.exception.AiTwinValidationException;
import org.example.insurancepolicyservice.exception.CustomerValidationException;
import org.example.insurancepolicyservice.exception.DownstreamServiceUnavailableException;
import org.example.insurancepolicyservice.exception.DuplicatePolicyApplicationException;
import org.example.insurancepolicyservice.exception.InsuranceProductNotFoundException;
import org.example.insurancepolicyservice.exception.InvalidPolicyApplicationStateException;
import org.example.insurancepolicyservice.exception.InvalidRequestException;
import org.example.insurancepolicyservice.exception.PolicyApplicationNotFoundException;
import org.example.insurancepolicyservice.exception.ResourceAccessDeniedException;
import org.example.insurancepolicyservice.model.InsuranceProduct;
import org.example.insurancepolicyservice.model.PolicyApplication;
import org.example.insurancepolicyservice.repo.InsuranceProductRepo;
import org.example.insurancepolicyservice.repo.PolicyApplicationRepo;
import org.example.insurancepolicyservice.repo.PolicyRepo;
import org.example.insurancepolicyservice.repo.ProductCoverageRepo;
import org.example.insurancepolicyservice.repo.ProductExclusionRepo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class PolicyApplicationService {

    private final PolicyApplicationRepo applicationRepo;
    private final InsuranceProductRepo productRepo;
    private final ProductCoverageRepo coverageRepo;
    private final ProductExclusionRepo exclusionRepo;
    private final PolicyRepo policyRepo;
    private final UnderwritingCalculator underwritingCalculator;
    private final WebClient.Builder webClientBuilder;

    public PolicyApplicationService(
            PolicyApplicationRepo applicationRepo,
            InsuranceProductRepo productRepo,
            ProductCoverageRepo coverageRepo,
            ProductExclusionRepo exclusionRepo,
            PolicyRepo policyRepo,
            UnderwritingCalculator underwritingCalculator,
            WebClient.Builder webClientBuilder) {

        this.applicationRepo = applicationRepo;
        this.productRepo = productRepo;
        this.coverageRepo = coverageRepo;
        this.exclusionRepo = exclusionRepo;
        this.policyRepo = policyRepo;
        this.underwritingCalculator =
                underwritingCalculator;
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<PolicyApplicationResponseDTO> createApplication(
            Long customerId,
            String jwtToken,
            CreatePolicyApplicationRequestDTO request) {

        validateLinkedCustomer(customerId);

        Mono<CustomerValidationResponseDTO> customerValidation =
                validateCustomer(
                        customerId,
                        jwtToken
                );

        Mono<AiTwinValidationResponseDTO> twinValidation =
                validateAiTwin(
                        request.twinId(),
                        customerId,
                        jwtToken
                );

        Mono<InsuranceProduct> productMono =
                findActiveProduct(
                        request.productId()
                );

        return Mono.zip(
                        customerValidation,
                        twinValidation,
                        productMono
                )
                .flatMap(result -> {

                    CustomerValidationResponseDTO customer =
                            result.getT1();

                    AiTwinValidationResponseDTO twin =
                            result.getT2();

                    InsuranceProduct product =
                            result.getT3();

                    if (!customer.valid()) {

                        return Mono.error(
                                new CustomerValidationException(
                                        "Customer is not active"
                                )
                        );
                    }

                    if (!twin.ownedByCustomer()
                            || !twin.valid()) {

                        return Mono.error(
                                new AiTwinValidationException(
                                        "AI Twin is not active or "
                                                + "does not belong to "
                                                + "the Customer"
                                )
                        );
                    }

                    return Mono.when(
                            ensureNoOpenApplication(
                                    request.twinId(),
                                    product.getProductId()
                            ),
                            ensureNoActivePolicy(
                                    request.twinId()
                            )
                    );
                })
                .then(
                        Mono.defer(() -> {

                            PolicyApplication application =
                                    new PolicyApplication();

                            application.setCustomerId(
                                    customerId
                            );

                            application.setTwinId(
                                    request.twinId()
                            );

                            application.setProductId(
                                    request.productId()
                            );

                            application.setStatus(
                                    PolicyApplicationStatus
                                            .PENDING_REVIEW
                            );

                            clearAssessmentValues(
                                    application
                            );

                            clearProposalValues(
                                    application
                            );

                            application.setReviewedBy(
                                    null
                            );

                            application.setDecisionReason(
                                    null
                            );

                            application.setSubmittedAt(
                                    LocalDateTime.now()
                            );

                            application.setReviewedAt(
                                    null
                            );

                            return applicationRepo.save(
                                    application
                            );
                        })
                )
                .map(
                        this::convertToApplicationResponse
                );
    }

    public Flux<PolicyApplicationResponseDTO>
    getMyApplications(
            Long customerId) {

        validateLinkedCustomer(customerId);

        return applicationRepo
                .findAllByCustomerId(
                        customerId
                )
                .map(
                        this::convertToApplicationResponse
                );
    }

    public Flux<PolicyApplicationResponseDTO>
    getPendingApplications() {

        return applicationRepo
                .findAllByStatus(
                        PolicyApplicationStatus
                                .PENDING_REVIEW
                )
                .map(
                        this::convertToApplicationResponse
                );
    }

    public Flux<PolicyApplicationResponseDTO>
    getReviewedByUnderwriter(
            Long underwriterUserId) {

        if (underwriterUserId == null) {

            return Flux.error(
                    new InvalidRequestException(
                            "Underwriter user ID is required"
                    )
            );
        }

        return applicationRepo
                .findAllByReviewedBy(
                        underwriterUserId
                )
                .map(
                        this::convertToApplicationResponse
                );
    }

    public Mono<PolicyApplicationDetailsResponseDTO>
    getApplicationDetails(
            Long applicationId,
            String role,
            Long customerId) {

        return findApplicationById(
                applicationId
        )
                .flatMap(application -> {

                    validateApplicationAccess(
                            application,
                            role,
                            customerId
                    );

                    return buildApplicationDetails(
                            application
                    );
                });
    }

    public Mono<RiskAssessmentResponseDTO>
    assessApplication(
            Long applicationId,
            Long underwriterUserId,
            String jwtToken,
            RiskAssessmentRequestDTO request) {

        return findApplicationById(
                applicationId
        )
                .flatMap(application -> {

                    ensureAssessmentAllowed(
                            application
                    );

                    Mono<InsuranceProduct> productMono =
                            findProductById(
                                    application.getProductId()
                            );

                    Mono<AiTwinRiskProfileResponseDTO>
                            riskProfileMono =
                            getAiTwinRiskProfile(
                                    application.getTwinId(),
                                    jwtToken
                            );

                    return Mono.zip(
                                    productMono,
                                    riskProfileMono
                            )
                            .flatMap(result -> {

                                InsuranceProduct product =
                                        result.getT1();

                                AiTwinRiskProfileResponseDTO
                                        twinProfile =
                                        result.getT2();

                                if (!application
                                        .getCustomerId()
                                        .equals(
                                                twinProfile
                                                        .customerId()
                                        )) {

                                    return Mono.error(
                                            new AiTwinValidationException(
                                                    "AI Twin does not belong "
                                                            + "to the application Customer"
                                            )
                                    );
                                }

                                RiskAssessmentResponseDTO
                                        assessment =
                                        underwritingCalculator
                                                .calculateRisk(
                                                        applicationId,
                                                        product,
                                                        twinProfile,
                                                        request
                                                );

                                application.setRiskScore(
                                        assessment.riskScore()
                                );

                                application.setRiskLevel(
                                        assessment.riskLevel()
                                );

                                application
                                        .setSystemRecommendation(
                                                assessment
                                                        .recommendation()
                                        );

                                application.setProposedPremium(
                                        assessment
                                                .calculatedPremium()
                                );

                                application
                                        .setProposedCoverageLimit(
                                                assessment
                                                        .recommendedCoverageLimit()
                                        );

                                application
                                        .setProposedDeductible(
                                                assessment
                                                        .recommendedDeductible()
                                        );

                                application.setReviewedBy(
                                        underwriterUserId
                                );

                                application.setDecisionReason(
                                        normalizeOptionalText(
                                                request
                                                        .assessmentRemarks()
                                        )
                                );

                                application.setReviewedAt(
                                        LocalDateTime.now()
                                );

                                return applicationRepo
                                        .save(application)
                                        .thenReturn(
                                                assessment
                                        );
                            });
                });
    }

    public Mono<PolicyApplicationResponseDTO>
    requestChanges(
            Long applicationId,
            Long underwriterUserId,
            PolicyChangesRequestDTO request) {

        return findApplicationById(
                applicationId
        )
                .flatMap(application -> {

                    ensureUnderwriterDecisionAllowed(
                            application
                    );

                    application.setStatus(
                            PolicyApplicationStatus
                                    .CHANGES_REQUIRED
                    );

                    application.setReviewedBy(
                            underwriterUserId
                    );

                    application.setDecisionReason(
                            request.reason().trim()
                    );

                    application.setReviewedAt(
                            LocalDateTime.now()
                    );

                    /*
                     * The risk result may be retained for audit and
                     * explaining why changes were requested.
                     *
                     * Proposal values are cleared because no valid
                     * Customer offer exists in CHANGES_REQUIRED.
                     */
                    clearProposalValues(
                            application
                    );

                    return applicationRepo.save(
                            application
                    );
                })
                .map(
                        this::convertToApplicationResponse
                );
    }

    public Mono<PolicyApplicationResponseDTO>
    approveApplication(
            Long applicationId,
            Long underwriterUserId,
            PolicyApprovalRequestDTO request) {

        return findApplicationById(
                applicationId
        )
                .flatMap(application -> {

                    ensureApplicationCanBeApproved(
                            application
                    );

                    validateApprovalValues(
                            request
                    );

                    boolean recommendationOverride =
                            application
                                    .getSystemRecommendation()
                                    != UnderwritingRecommendation
                                    .APPROVE;

                    if (recommendationOverride
                            && request.reason()
                            .trim()
                            .isEmpty()) {

                        return Mono.error(
                                new InvalidRequestException(
                                        "Override reason is required "
                                                + "when approving against "
                                                + "the system recommendation"
                                )
                        );
                    }

                    application.setStatus(
                            PolicyApplicationStatus
                                    .APPROVED
                    );

                    application.setProposedPremium(
                            request.proposedPremium()
                    );

                    application
                            .setProposedCoverageLimit(
                                    request
                                            .proposedCoverageLimit()
                            );

                    application
                            .setProposedDeductible(
                                    request
                                            .proposedDeductible()
                            );

                    application.setReviewedBy(
                            underwriterUserId
                    );

                    application.setDecisionReason(
                            request.reason().trim()
                    );

                    application.setReviewedAt(
                            LocalDateTime.now()
                    );

                    application.setProposalExpiresAt(
                            LocalDateTime.now()
                                    .plusDays(7)
                    );

                    application.setAcceptedAt(
                            null
                    );

                    return applicationRepo.save(
                            application
                    );
                })
                .map(
                        this::convertToApplicationResponse
                );
    }

    public Mono<PolicyApplicationResponseDTO>
    rejectApplication(
            Long applicationId,
            Long underwriterUserId,
            PolicyRejectionRequestDTO request) {

        return findApplicationById(
                applicationId
        )
                .flatMap(application -> {

                    ensureUnderwriterDecisionAllowed(
                            application
                    );

                    application.setStatus(
                            PolicyApplicationStatus
                                    .REJECTED
                    );

                    application.setReviewedBy(
                            underwriterUserId
                    );

                    application.setDecisionReason(
                            request.reason().trim()
                    );

                    application.setReviewedAt(
                            LocalDateTime.now()
                    );

                    /*
                     * A rejected application has no valid proposal.
                     * Risk values remain available for staff audit.
                     */
                    clearProposalValues(
                            application
                    );

                    return applicationRepo.save(
                            application
                    );
                })
                .map(
                        this::convertToApplicationResponse
                );
    }

    public Mono<PolicyApplicationResponseDTO>
    resubmitApplication(
            Long applicationId,
            Long customerId,
            String jwtToken) {

        validateLinkedCustomer(
                customerId
        );

        return applicationRepo
                .findByApplicationIdAndCustomerId(
                        applicationId,
                        customerId
                )
                .switchIfEmpty(
                        Mono.error(
                                new PolicyApplicationNotFoundException(
                                        "Policy application not found"
                                )
                        )
                )
                .flatMap(application -> {

                    if (application.getStatus()
                            != PolicyApplicationStatus
                            .CHANGES_REQUIRED) {

                        return Mono.error(
                                new InvalidPolicyApplicationStateException(
                                        "Only a CHANGES_REQUIRED application "
                                                + "can be resubmitted"
                                )
                        );
                    }

                    Mono<CustomerValidationResponseDTO>
                            customerValidation =
                            validateCustomer(
                                    customerId,
                                    jwtToken
                            );

                    Mono<AiTwinValidationResponseDTO>
                            twinValidation =
                            validateAiTwin(
                                    application.getTwinId(),
                                    customerId,
                                    jwtToken
                            );

                    Mono<InsuranceProduct>
                            productValidation =
                            findActiveProduct(
                                    application.getProductId()
                            );

                    return Mono.zip(
                                    customerValidation,
                                    twinValidation,
                                    productValidation
                            )
                            .flatMap(result -> {

                                CustomerValidationResponseDTO
                                        customer =
                                        result.getT1();

                                AiTwinValidationResponseDTO
                                        twin =
                                        result.getT2();

                                if (!customer.valid()) {

                                    return Mono.error(
                                            new CustomerValidationException(
                                                    "Customer is not active"
                                            )
                                    );
                                }

                                if (!twin.valid()
                                        || !twin.ownedByCustomer()) {

                                    return Mono.error(
                                            new AiTwinValidationException(
                                                    "AI Twin is not active "
                                                            + "or does not belong "
                                                            + "to the Customer"
                                            )
                                    );
                                }

                                return ensureNoActivePolicy(
                                        application.getTwinId()
                                )
                                        .then(
                                                Mono.defer(() -> {

                                                    application.setStatus(
                                                            PolicyApplicationStatus
                                                                    .PENDING_REVIEW
                                                    );

                                                    clearAssessmentValues(
                                                            application
                                                    );

                                                    clearProposalValues(
                                                            application
                                                    );

                                                    application.setReviewedBy(
                                                            null
                                                    );

                                                    application.setDecisionReason(
                                                            null
                                                    );

                                                    application.setReviewedAt(
                                                            null
                                                    );

                                                    return applicationRepo
                                                            .save(application);
                                                })
                                        );
                            });
                })
                .map(
                        this::convertToApplicationResponse
                );
    }

    // Allows the owning Customer to decline an approved proposal.
// The proposed terms remain stored as an audit record, but the offer
// becomes closed and can no longer produce an insurance policy.

    public Mono<PolicyApplicationResponseDTO>
    declineApplication(
            Long applicationId,
            Long customerId) {

        validateLinkedCustomer(customerId);

        return applicationRepo
                .findByApplicationIdAndCustomerId(
                        applicationId,
                        customerId
                )
                .switchIfEmpty(
                        Mono.error(
                                new PolicyApplicationNotFoundException(
                                        "Policy application not found"
                                )
                        )
                )
                .flatMap(application -> {

                    if (application.getStatus()
                            != PolicyApplicationStatus.APPROVED) {

                        return Mono.error(
                                new InvalidPolicyApplicationStateException(
                                        "Only an APPROVED policy proposal "
                                                + "can be declined"
                                )
                        );
                    }

                    if (application.getProposalExpiresAt()
                            == null) {

                        return Mono.error(
                                new InvalidPolicyApplicationStateException(
                                        "The approved policy proposal "
                                                + "does not have an expiry time"
                                )
                        );
                    }

                    if (application.getProposalExpiresAt()
                            .isBefore(LocalDateTime.now())) {

                        return Mono.error(
                                new InvalidPolicyApplicationStateException(
                                        "The approved policy proposal "
                                                + "has already expired"
                                )
                        );
                    }

                    application.setStatus(
                            PolicyApplicationStatus.DECLINED
                    );

                    application.setAcceptedAt(null);

                    return applicationRepo.save(
                            application
                    );
                })
                .map(this::convertToApplicationResponse);
    }


    public Flux<PolicyApplicationResponseDTO>
    getAllApplications() {

        return applicationRepo
                .findAll()
                .map(
                        this::convertToApplicationResponse
                );
    }

    public Mono<PolicyApplication>
    getApprovedOwnedApplication(
            Long applicationId,
            Long customerId) {

        validateLinkedCustomer(
                customerId
        );

        return applicationRepo
                .findByApplicationIdAndCustomerId(
                        applicationId,
                        customerId
                )
                .switchIfEmpty(
                        Mono.error(
                                new PolicyApplicationNotFoundException(
                                        "Policy application not found"
                                )
                        )
                )
                .flatMap(application -> {

                    if (application.getStatus()
                            == PolicyApplicationStatus
                            .ACCEPTED) {

                        return Mono.error(
                                new InvalidPolicyApplicationStateException(
                                        "This policy application "
                                                + "has already been accepted"
                                )
                        );
                    }

                    if (application.getStatus()
                            == PolicyApplicationStatus
                            .REJECTED) {

                        return Mono.error(
                                new InvalidPolicyApplicationStateException(
                                        "A rejected policy application "
                                                + "cannot be accepted"
                                )
                        );
                    }

                    if (application.getStatus()
                            == PolicyApplicationStatus
                            .CHANGES_REQUIRED) {

                        return Mono.error(
                                new InvalidPolicyApplicationStateException(
                                        "Required changes must be reviewed "
                                                + "before this application "
                                                + "can be accepted"
                                )
                        );
                    }

                    if (application.getStatus()
                            == PolicyApplicationStatus
                            .PENDING_REVIEW) {

                        return Mono.error(
                                new InvalidPolicyApplicationStateException(
                                        "The policy application is still "
                                                + "awaiting Underwriter approval"
                                )
                        );
                    }

                    if (application.getStatus()
                            != PolicyApplicationStatus
                            .APPROVED) {

                        return Mono.error(
                                new InvalidPolicyApplicationStateException(
                                        "Only an APPROVED application "
                                                + "can be accepted"
                                )
                        );
                    }

                    if (application
                            .getProposalExpiresAt()
                            == null) {

                        return Mono.error(
                                new InvalidPolicyApplicationStateException(
                                        "The approved policy proposal "
                                                + "does not have an expiry time"
                                )
                        );
                    }

                    if (application
                            .getProposalExpiresAt()
                            .isBefore(
                                    LocalDateTime.now()
                            )) {

                        return Mono.error(
                                new InvalidPolicyApplicationStateException(
                                        "The approved policy proposal "
                                                + "has expired"
                                )
                        );
                    }

                    return Mono.just(
                            application
                    );
                });
    }

    public Mono<PolicyApplication>
    markApplicationAccepted(
            PolicyApplication application) {

        application.setStatus(
                PolicyApplicationStatus
                        .ACCEPTED
        );

        application.setAcceptedAt(
                LocalDateTime.now()
        );

        return applicationRepo.save(
                application
        );
    }

    private Mono<Void> ensureNoOpenApplication(
            Long twinId,
            Long productId) {

        Mono<Boolean> pendingExists =
                applicationRepo
                        .existsByTwinIdAndProductIdAndStatus(
                                twinId,
                                productId,
                                PolicyApplicationStatus
                                        .PENDING_REVIEW
                        );

        Mono<Boolean> changesRequiredExists =
                applicationRepo
                        .existsByTwinIdAndProductIdAndStatus(
                                twinId,
                                productId,
                                PolicyApplicationStatus
                                        .CHANGES_REQUIRED
                        );

        Mono<Boolean> approvedExists =
                applicationRepo
                        .existsByTwinIdAndProductIdAndStatus(
                                twinId,
                                productId,
                                PolicyApplicationStatus
                                        .APPROVED
                        );

        return Mono.zip(
                        pendingExists,
                        changesRequiredExists,
                        approvedExists
                )
                .flatMap(result -> {

                    boolean openApplicationExists =
                            result.getT1()
                                    || result.getT2()
                                    || result.getT3();

                    if (openApplicationExists) {

                        return Mono.error(
                                new DuplicatePolicyApplicationException(
                                        "An open policy application "
                                                + "already exists for this "
                                                + "AI Twin and product"
                                )
                        );
                    }

                    return Mono.empty();
                });
    }

    // Prevents another application while the AI Twin has active coverage.
    private Mono<Void> ensureNoActivePolicy(
            Long twinId) {

        return policyRepo
                .existsByTwinIdAndStatus(
                        twinId,
                        PolicyStatus.ACTIVE
                )
                .flatMap(activePolicyExists -> {

                    if (activePolicyExists) {

                        return Mono.error(
                                new InvalidPolicyApplicationStateException(
                                        "This AI Twin already has an active "
                                                + "insurance policy. Cancel or "
                                                + "expire the current policy "
                                                + "before applying again."
                                )
                        );
                    }

                    return Mono.empty();
                });
    }

    private Mono<CustomerValidationResponseDTO>
    validateCustomer(
            Long customerId,
            String jwtToken) {

        return webClientBuilder
                .build()
                .get()
                .uri(
                        "http://CUSTOMER-SERVICE"
                                + "/internal/customers/"
                                + customerId
                                + "/validation"
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
                                        new CustomerValidationException(
                                                "Customer validation failed"
                                        )
                                )
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response ->
                                Mono.error(
                                        new DownstreamServiceUnavailableException(
                                                "Customer Service is "
                                                        + "currently unavailable"
                                        )
                                )
                )
                .bodyToMono(
                        CustomerValidationResponseDTO.class
                )
                .onErrorMap(
                        WebClientRequestException.class,
                        exception ->
                                new DownstreamServiceUnavailableException(
                                        "Customer Service is "
                                                + "currently unavailable"
                                )
                );
    }

    private Mono<AiTwinValidationResponseDTO>
    validateAiTwin(
            Long twinId,
            Long customerId,
            String jwtToken) {

        return webClientBuilder
                .build()
                .get()
                .uri(uriBuilder ->
                        uriBuilder
                                .scheme("http")
                                .host("AI-TWIN-SERVICE")
                                .path(
                                        "/internal/ai-twins/"
                                                + twinId
                                                + "/policy-validation"
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
                                                "AI Twin validation failed"
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
                        AiTwinValidationResponseDTO.class
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

    private Mono<AiTwinRiskProfileResponseDTO>
    getAiTwinRiskProfile(
            Long twinId,
            String jwtToken) {

        return webClientBuilder
                .build()
                .get()
                .uri(
                        "http://AI-TWIN-SERVICE"
                                + "/api/ai-twins/"
                                + twinId
                                + "/risk-profile"
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
                                                "AI Twin risk profile "
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
                        AiTwinRiskProfileResponseDTO.class
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

    private Mono<InsuranceProduct>
    findActiveProduct(
            Long productId) {

        return findProductById(
                productId
        )
                .flatMap(product -> {

                    if (product.getStatus()
                            != ProductStatus.ACTIVE) {

                        return Mono.error(
                                new InsuranceProductNotFoundException(
                                        "Active insurance product "
                                                + "not found with ID: "
                                                + productId
                                )
                        );
                    }

                    return Mono.just(
                            product
                    );
                });
    }

    private Mono<InsuranceProduct>
    findProductById(
            Long productId) {

        return productRepo
                .findById(
                        productId
                )
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

    private Mono<PolicyApplication>
    findApplicationById(
            Long applicationId) {

        return applicationRepo
                .findById(
                        applicationId
                )
                .switchIfEmpty(
                        Mono.error(
                                new PolicyApplicationNotFoundException(
                                        "Policy application not found "
                                                + "with ID: "
                                                + applicationId
                                )
                        )
                );
    }

    private Mono<PolicyApplicationDetailsResponseDTO>
    buildApplicationDetails(
            PolicyApplication application) {

        return findProductById(
                application.getProductId()
        )
                .flatMap(product ->
                        Mono.zip(
                                        coverageRepo
                                                .findAllByProductId(
                                                        product
                                                                .getProductId()
                                                )
                                                .map(coverage ->
                                                        new ProductCoverageResponseDTO(
                                                                coverage
                                                                        .getCoverageId(),
                                                                coverage
                                                                        .getProductId(),
                                                                coverage
                                                                        .getActionType(),
                                                                coverage
                                                                        .getViolationType(),
                                                                coverage
                                                                        .getCoverageLimit(),
                                                                coverage
                                                                        .getActive(),
                                                                coverage
                                                                        .getDescription()
                                                        )
                                                )
                                                .collectList(),

                                        exclusionRepo
                                                .findAllByProductId(
                                                        product
                                                                .getProductId()
                                                )
                                                .map(exclusion ->
                                                        new ProductExclusionResponseDTO(
                                                                exclusion
                                                                        .getExclusionId(),
                                                                exclusion
                                                                        .getProductId(),
                                                                exclusion
                                                                        .getExclusionCode(),
                                                                exclusion
                                                                        .getDescription(),
                                                                exclusion
                                                                        .getActive()
                                                        )
                                                )
                                                .collectList()
                                )
                                .map(result -> {

                                    InsuranceProductDetailsResponseDTO
                                            productDetails =
                                            new InsuranceProductDetailsResponseDTO(
                                                    product
                                                            .getProductId(),
                                                    product
                                                            .getProductCode(),
                                                    product
                                                            .getProductName(),
                                                    product
                                                            .getDescription(),
                                                    product
                                                            .getBasePremium(),
                                                    product
                                                            .getCoverageLimit(),
                                                    product
                                                            .getDeductible(),
                                                    product
                                                            .getStatus(),
                                                    result.getT1(),
                                                    result.getT2(),
                                                    product
                                                            .getCreatedAt(),
                                                    product
                                                            .getUpdatedAt()
                                            );

                                    return new PolicyApplicationDetailsResponseDTO(
                                            convertToApplicationResponse(
                                                    application
                                            ),
                                            productDetails
                                    );
                                })
                );
    }

    private void validateApplicationAccess(
            PolicyApplication application,
            String role,
            Long customerId) {

        if ("CUSTOMER".equals(role)
                && !application
                .getCustomerId()
                .equals(customerId)) {

            throw new ResourceAccessDeniedException(
                    "You cannot access another "
                            + "Customer's policy application"
            );
        }
    }

    private void ensureAssessmentAllowed(
            PolicyApplication application) {

        if (application.getStatus()
                != PolicyApplicationStatus
                .PENDING_REVIEW) {

            throw new InvalidPolicyApplicationStateException(
                    "Risk assessment is allowed only for "
                            + "a PENDING_REVIEW application"
            );
        }
    }

    private void ensureUnderwriterDecisionAllowed(
            PolicyApplication application) {

        if (application.getStatus()
                != PolicyApplicationStatus
                .PENDING_REVIEW) {

            throw new InvalidPolicyApplicationStateException(
                    "Underwriting decision is allowed only for "
                            + "a PENDING_REVIEW application"
            );
        }
    }

    private void ensureApplicationCanBeApproved(
            PolicyApplication application) {

        ensureUnderwriterDecisionAllowed(
                application
        );

        if (application.getRiskScore() == null
                || application.getRiskLevel() == null
                || application
                .getSystemRecommendation()
                == null) {

            throw new InvalidPolicyApplicationStateException(
                    "Risk assessment must be completed "
                            + "before application approval"
            );
        }
    }

    private void validateApprovalValues(
            PolicyApprovalRequestDTO request) {

        if (request.proposedDeductible()
                .compareTo(
                        request
                                .proposedCoverageLimit()
                ) > 0) {

            throw new InvalidRequestException(
                    "Proposed deductible cannot be greater "
                            + "than the proposed coverage limit"
            );
        }
    }

    private void validateLinkedCustomer(
            Long customerId) {

        if (customerId == null) {

            throw new CustomerValidationException(
                    "Customer profile is not linked. "
                            + "Create a Customer profile "
                            + "and log in again."
            );
        }
    }

    private String normalizeOptionalText(
            String value) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        return value.trim();
    }

    // Clears assessment values before the corrected application is reviewed.
    private void clearAssessmentValues(
            PolicyApplication application) {

        application.setRiskScore(
                null
        );

        application.setRiskLevel(
                null
        );

        application.setSystemRecommendation(
                null
        );
    }

    // Clears proposal values whenever no valid Customer offer exists.
    private void clearProposalValues(
            PolicyApplication application) {

        application.setProposedPremium(
                null
        );

        application.setProposedCoverageLimit(
                null
        );

        application.setProposedDeductible(
                null
        );

        application.setProposalExpiresAt(
                null
        );

        application.setAcceptedAt(
                null
        );
    }

    public PolicyApplicationResponseDTO
    convertToApplicationResponse(
            PolicyApplication application) {

        return new PolicyApplicationResponseDTO(
                application.getApplicationId(),
                application.getCustomerId(),
                application.getTwinId(),
                application.getProductId(),
                application.getStatus(),
                application.getRiskScore(),
                application.getRiskLevel(),
                application
                        .getSystemRecommendation(),
                application.getProposedPremium(),
                application
                        .getProposedCoverageLimit(),
                application
                        .getProposedDeductible(),
                application.getReviewedBy(),
                application.getDecisionReason(),
                application.getSubmittedAt(),
                application.getReviewedAt(),
                application.getProposalExpiresAt(),
                application.getAcceptedAt()
        );
    }
}