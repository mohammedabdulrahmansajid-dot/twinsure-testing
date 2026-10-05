package org.example.aitwinservice.service;

import org.example.aitwinservice.dto.request.AiTwinStatusRequestDTO;
import org.example.aitwinservice.dto.request.CreateAiTwinRequestDTO;
import org.example.aitwinservice.dto.request.TwinPermissionRequestDTO;
import org.example.aitwinservice.dto.request.UpdateAiTwinRequestDTO;
import org.example.aitwinservice.dto.response.AiTwinActionRulesResponseDTO;
import org.example.aitwinservice.dto.response.AiTwinClaimSummaryResponseDTO;
import org.example.aitwinservice.dto.response.AiTwinDetailsResponseDTO;
import org.example.aitwinservice.dto.response.AiTwinResponseDTO;
import org.example.aitwinservice.dto.response.AiTwinRiskProfileResponseDTO;
import org.example.aitwinservice.dto.response.AiTwinValidationResponseDTO;
import org.example.aitwinservice.dto.response.CustomerValidationResponseDTO;
import org.example.aitwinservice.dto.response.TwinPermissionResponseDTO;
import org.example.aitwinservice.enums.AiTwinStatus;
import org.example.aitwinservice.enums.PermissionLevel;
import org.example.aitwinservice.exception.AiTwinNotFoundException;
import org.example.aitwinservice.exception.CustomerServiceUnavailableException;
import org.example.aitwinservice.exception.CustomerValidationException;
import org.example.aitwinservice.exception.DuplicateAiTwinException;
import org.example.aitwinservice.exception.InvalidAiTwinConfigurationException;
import org.example.aitwinservice.model.AiTwin;
import org.example.aitwinservice.model.TwinPermission;
import org.example.aitwinservice.repo.AiTwinRepo;
import org.example.aitwinservice.repo.TwinPermissionRepo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.example.aitwinservice.dto.response.ActivePolicyResponseDTO;
import org.example.aitwinservice.dto.response.AiTwinConfigurationLockResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AiTwinService {

    private final AiTwinRepo aiTwinRepo;
    private final TwinPermissionRepo permissionRepo;
    private final WebClient.Builder webClientBuilder;
    private final AiTwinPolicyLockService policyLockService;

    public AiTwinService(
            AiTwinRepo aiTwinRepo,
            TwinPermissionRepo permissionRepo,
            WebClient.Builder webClientBuilder,
            AiTwinPolicyLockService policyLockService) {

        this.aiTwinRepo = aiTwinRepo;
        this.permissionRepo = permissionRepo;
        this.webClientBuilder = webClientBuilder;
        this.policyLockService = policyLockService;
    }

    public Mono<AiTwinResponseDTO> registerAiTwin(
            Long customerId,
            String jwtToken,
            CreateAiTwinRequestDTO request) {

        validateCustomerId(customerId);

        validateLimits(
                request.transactionLimit(),
                request.approvalThreshold()
        );

        String normalizedTwinName =
                request.twinName().trim();

        return validateCustomer(customerId, jwtToken)
                .then(
                        aiTwinRepo.existsByCustomerIdAndTwinName(
                                customerId,
                                normalizedTwinName
                        )
                )
                .flatMap(twinExists -> {

                    if (twinExists) {
                        return Mono.error(
                                new DuplicateAiTwinException(
                                        "AI Twin already exists with name: "
                                                + normalizedTwinName
                                )
                        );
                    }

                    LocalDateTime currentTime =
                            LocalDateTime.now();

                    AiTwin aiTwin = new AiTwin();

                    aiTwin.setCustomerId(customerId);
                    aiTwin.setTwinName(normalizedTwinName);
                    aiTwin.setProviderName(
                            request.providerName().trim()
                    );
                    aiTwin.setModelName(
                            request.modelName().trim()
                    );
                    aiTwin.setAutonomyLevel(
                            request.autonomyLevel()
                    );
                    aiTwin.setTransactionLimit(
                            request.transactionLimit()
                    );
                    aiTwin.setApprovalThreshold(
                            request.approvalThreshold()
                    );
                    aiTwin.setStatus(AiTwinStatus.ACTIVE);
                    aiTwin.setCreatedAt(currentTime);
                    aiTwin.setUpdatedAt(currentTime);

                    return aiTwinRepo.save(aiTwin);
                })
                .map(this::convertToAiTwinResponse);
    }

    public Flux<AiTwinResponseDTO> getMyAiTwins(
            Long customerId) {

        validateCustomerId(customerId);

        return aiTwinRepo
                .findAllByCustomerId(customerId)
                .map(this::convertToAiTwinResponse);
    }

    public Mono<AiTwinDetailsResponseDTO> getOwnedAiTwinDetails(
            Long twinId,
            Long customerId) {

        return findOwnedAiTwin(twinId, customerId)
                .flatMap(this::buildDetailsResponse);
    }

    public Mono<AiTwinResponseDTO> updateOwnedAiTwin(
            Long twinId,
            Long customerId,
            String jwtToken,
            UpdateAiTwinRequestDTO request) {

        validateLimits(
                request.transactionLimit(),
                request.approvalThreshold()
        );

        return findOwnedAiTwin(
                twinId,
                customerId
        )
                .flatMap(aiTwin -> {

                    ensureCustomerCanModify(
                            aiTwin
                    );

                    return policyLockService
                            .ensureConfigurationUnlocked(
                                    twinId,
                                    jwtToken
                            )
                            .thenReturn(
                                    aiTwin
                            );
                })
                .flatMap(aiTwin -> {

                    aiTwin.setAutonomyLevel(
                            request.autonomyLevel()
                    );

                    aiTwin.setTransactionLimit(
                            request.transactionLimit()
                    );

                    aiTwin.setApprovalThreshold(
                            request.approvalThreshold()
                    );

                    aiTwin.setUpdatedAt(
                            LocalDateTime.now()
                    );

                    return aiTwinRepo.save(
                            aiTwin
                    );
                })
                .map(
                        this::convertToAiTwinResponse
                );
    }

    public Flux<TwinPermissionResponseDTO> getOwnedPermissions(
            Long twinId,
            Long customerId) {

        return findOwnedAiTwin(twinId, customerId)
                .flatMapMany(aiTwin ->
                        permissionRepo.findAllByTwinId(twinId)
                )
                .map(this::convertToPermissionResponse);
    }

    public Mono<TwinPermissionResponseDTO>
    configurePermission(
            Long twinId,
            Long customerId,
            String jwtToken,
            TwinPermissionRequestDTO request) {

        return findOwnedAiTwin(
                twinId,
                customerId
        )
                .flatMap(aiTwin -> {

                    ensureCustomerCanModify(
                            aiTwin
                    );

                    validatePermission(
                            aiTwin,
                            request
                    );

                    return policyLockService
                            .ensureConfigurationUnlocked(
                                    twinId,
                                    jwtToken
                            )
                            .thenReturn(
                                    aiTwin
                            );
                })
                .flatMap(aiTwin ->
                        permissionRepo
                                .findByTwinIdAndActionType(
                                        twinId,
                                        request.actionType()
                                )
                                .switchIfEmpty(
                                        Mono.fromSupplier(() ->
                                                createEmptyPermission(
                                                        twinId,
                                                        request
                                                )
                                        )
                                )
                )
                .flatMap(permission -> {

                    LocalDateTime currentTime =
                            LocalDateTime.now();

                    boolean newPermission =
                            permission.getPermissionId()
                                    == null;

                    permission.setTwinId(
                            twinId
                    );

                    permission.setActionType(
                            request.actionType()
                    );

                    permission.setPermissionLevel(
                            request.permissionLevel()
                    );

                    permission.setActionLimit(
                            request.actionLimit()
                    );

                    permission.setActive(
                            true
                    );

                    if (newPermission) {

                        permission.setCreatedAt(
                                currentTime
                        );
                    }

                    permission.setUpdatedAt(
                            currentTime
                    );

                    return permissionRepo.save(
                            permission
                    );
                })
                .map(
                        this::convertToPermissionResponse
                );
    }

    public Mono<AiTwinRiskProfileResponseDTO> getRiskProfile(
            Long twinId) {

        return findAiTwinById(twinId)
                .flatMap(aiTwin ->
                        permissionRepo
                                .findAllByTwinIdAndActiveTrue(
                                        twinId
                                )
                                .map(
                                        this::convertToPermissionResponse
                                )
                                .collectList()
                                .map(permissions ->
                                        new AiTwinRiskProfileResponseDTO(
                                                aiTwin.getTwinId(),
                                                aiTwin.getCustomerId(),
                                                aiTwin.getTwinName(),
                                                aiTwin.getAutonomyLevel(),
                                                aiTwin.getTransactionLimit(),
                                                aiTwin.getApprovalThreshold(),
                                                aiTwin.getStatus(),
                                                permissions
                                        )
                                )
                );
    }

    public Mono<AiTwinClaimSummaryResponseDTO> getClaimSummary(
            Long twinId) {

        return findAiTwinById(twinId)
                .flatMap(aiTwin ->
                        permissionRepo
                                .findAllByTwinId(twinId)
                                .map(
                                        this::convertToPermissionResponse
                                )
                                .collectList()
                                .map(permissions ->
                                        new AiTwinClaimSummaryResponseDTO(
                                                aiTwin.getTwinId(),
                                                aiTwin.getCustomerId(),
                                                aiTwin.getTwinName(),
                                                aiTwin.getProviderName(),
                                                aiTwin.getModelName(),
                                                aiTwin.getAutonomyLevel(),
                                                aiTwin.getTransactionLimit(),
                                                aiTwin.getApprovalThreshold(),
                                                aiTwin.getStatus(),
                                                permissions
                                        )
                                )
                );
    }

    public Flux<AiTwinResponseDTO> getAllAiTwins() {

        return aiTwinRepo
                .findAll()
                .map(this::convertToAiTwinResponse);
    }

    public Mono<AiTwinDetailsResponseDTO> getAiTwinDetailsForAdmin(
            Long twinId) {

        return findAiTwinById(twinId)
                .flatMap(this::buildDetailsResponse);
    }

    public Mono<AiTwinResponseDTO> updateAiTwinStatus(
            Long twinId,
            AiTwinStatusRequestDTO request) {

        return findAiTwinById(twinId)
                .flatMap(aiTwin -> {

                    aiTwin.setStatus(request.status());
                    aiTwin.setUpdatedAt(
                            LocalDateTime.now()
                    );

                    return aiTwinRepo.save(aiTwin);
                })
                .map(this::convertToAiTwinResponse);
    }

    public Mono<AiTwinValidationResponseDTO> validateAiTwin(
            Long twinId,
            Long customerId) {

        return aiTwinRepo
                .findById(twinId)
                .switchIfEmpty(
                        Mono.error(
                                new AiTwinNotFoundException(
                                        "AI Twin not found with ID: "
                                                + twinId
                                )
                        )
                )
                .map(aiTwin -> {

                    boolean ownedByCustomer =
                            aiTwin.getCustomerId()
                                    .equals(customerId);

                    boolean valid =
                            ownedByCustomer
                                    && aiTwin.getStatus()
                                    == AiTwinStatus.ACTIVE;

                    return new AiTwinValidationResponseDTO(
                            aiTwin.getTwinId(),
                            aiTwin.getCustomerId(),
                            aiTwin.getStatus(),
                            ownedByCustomer,
                            valid
                    );
                });
    }

    public Mono<AiTwinActionRulesResponseDTO> getActionRules(
            Long twinId,
            Long customerId) {

        return aiTwinRepo
                .findById(twinId)
                .switchIfEmpty(
                        Mono.error(
                                new AiTwinNotFoundException(
                                        "AI Twin not found with ID: "
                                                + twinId
                                )
                        )
                )
                .flatMap(aiTwin -> {

                    boolean valid =
                            aiTwin.getCustomerId()
                                    .equals(customerId)
                                    && aiTwin.getStatus()
                                    == AiTwinStatus.ACTIVE;

                    return permissionRepo
                            .findAllByTwinIdAndActiveTrue(
                                    twinId
                            )
                            .map(
                                    this::convertToPermissionResponse
                            )
                            .collectList()
                            .map(permissions ->
                                    new AiTwinActionRulesResponseDTO(
                                            aiTwin.getTwinId(),
                                            aiTwin.getCustomerId(),
                                            aiTwin.getStatus(),
                                            aiTwin.getAutonomyLevel(),
                                            aiTwin.getTransactionLimit(),
                                            aiTwin.getApprovalThreshold(),
                                            permissions,
                                            valid
                                    )
                            );
                });
    }

    private Mono<Void> validateCustomer(
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
                        status -> status.value() == 404,
                        response ->
                                Mono.error(
                                        new CustomerValidationException(
                                                "Customer not found with ID: "
                                                        + customerId
                                        )
                                )
                )
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
                                        new CustomerServiceUnavailableException(
                                                "Customer Service is currently unavailable"
                                        )
                                )
                )
                .bodyToMono(
                        CustomerValidationResponseDTO.class
                )
                .onErrorMap(
                        WebClientRequestException.class,
                        exception ->
                                new CustomerServiceUnavailableException(
                                        "Customer Service is currently unavailable"
                                )
                )
                .flatMap(response -> {

                    if (!response.valid()) {
                        return Mono.error(
                                new CustomerValidationException(
                                        "Customer is not active"
                                )
                        );
                    }

                    return Mono.empty();
                });
    }

    private Mono<AiTwin> findOwnedAiTwin(
            Long twinId,
            Long customerId) {

        validateCustomerId(customerId);

        return aiTwinRepo
                .findByTwinIdAndCustomerId(
                        twinId,
                        customerId
                )
                .switchIfEmpty(
                        Mono.error(
                                new AiTwinNotFoundException(
                                        "AI Twin not found"
                                )
                        )
                );
    }

    private Mono<AiTwin> findAiTwinById(
            Long twinId) {

        return aiTwinRepo
                .findById(twinId)
                .switchIfEmpty(
                        Mono.error(
                                new AiTwinNotFoundException(
                                        "AI Twin not found with ID: "
                                                + twinId
                                )
                        )
                );
    }

    private Mono<AiTwinDetailsResponseDTO> buildDetailsResponse(
            AiTwin aiTwin) {

        return permissionRepo
                .findAllByTwinId(
                        aiTwin.getTwinId()
                )
                .map(this::convertToPermissionResponse)
                .collectList()
                .map(permissions ->
                        new AiTwinDetailsResponseDTO(
                                aiTwin.getTwinId(),
                                aiTwin.getCustomerId(),
                                aiTwin.getTwinName(),
                                aiTwin.getProviderName(),
                                aiTwin.getModelName(),
                                aiTwin.getAutonomyLevel(),
                                aiTwin.getTransactionLimit(),
                                aiTwin.getApprovalThreshold(),
                                aiTwin.getStatus(),
                                permissions,
                                aiTwin.getCreatedAt(),
                                aiTwin.getUpdatedAt()
                        )
                );
    }

    private TwinPermission createEmptyPermission(
            Long twinId,
            TwinPermissionRequestDTO request) {

        TwinPermission permission =
                new TwinPermission();

        permission.setTwinId(twinId);
        permission.setActionType(
                request.actionType()
        );

        return permission;
    }

    private void validateCustomerId(
            Long customerId) {

        if (customerId == null) {
            throw new CustomerValidationException(
                    "Customer profile is not linked. "
                            + "Please create a Customer profile "
                            + "and log in again."
            );
        }
    }

    private void validateLimits(
            BigDecimal transactionLimit,
            BigDecimal approvalThreshold) {

        if (approvalThreshold.compareTo(
                transactionLimit
        ) > 0) {

            throw new InvalidAiTwinConfigurationException(
                    "Approval threshold cannot be greater "
                            + "than transaction limit"
            );
        }
    }

    private void validatePermission(
            AiTwin aiTwin,
            TwinPermissionRequestDTO request) {

        if (request.permissionLevel()
                == PermissionLevel.PROHIBITED) {

            if (request.actionLimit() != null) {
                throw new InvalidAiTwinConfigurationException(
                        "Action limit must be empty for "
                                + "a prohibited action"
                );
            }

            return;
        }

        if (request.actionLimit() == null) {
            throw new InvalidAiTwinConfigurationException(
                    "Action limit is required for "
                            + request.permissionLevel()
            );
        }

        if (request.actionLimit().compareTo(
                aiTwin.getTransactionLimit()
        ) > 0) {

            throw new InvalidAiTwinConfigurationException(
                    "Action limit cannot be greater "
                            + "than the AI Twin transaction limit"
            );
        }
    }

    private void ensureCustomerCanModify(
            AiTwin aiTwin) {

        if (aiTwin.getStatus()
                != AiTwinStatus.ACTIVE) {

            throw new InvalidAiTwinConfigurationException(
                    "Only an ACTIVE AI Twin can be modified"
            );
        }
    }

    private AiTwinResponseDTO convertToAiTwinResponse(
            AiTwin aiTwin) {

        return new AiTwinResponseDTO(
                aiTwin.getTwinId(),
                aiTwin.getCustomerId(),
                aiTwin.getTwinName(),
                aiTwin.getProviderName(),
                aiTwin.getModelName(),
                aiTwin.getAutonomyLevel(),
                aiTwin.getTransactionLimit(),
                aiTwin.getApprovalThreshold(),
                aiTwin.getStatus()
        );
    }

    private TwinPermissionResponseDTO
    convertToPermissionResponse(
            TwinPermission permission) {

        return new TwinPermissionResponseDTO(
                permission.getPermissionId(),
                permission.getTwinId(),
                permission.getActionType(),
                permission.getPermissionLevel(),
                permission.getActionLimit(),
                permission.getActive(),
                permission.getCreatedAt(),
                permission.getUpdatedAt()
        );
    }

    public Mono<AiTwinConfigurationLockResponseDTO>
    getOwnedConfigurationLock(
            Long twinId,
            Long customerId,
            String jwtToken) {

        return findOwnedAiTwin(
                twinId,
                customerId
        )
                .then(
                        policyLockService.getPolicyLock(
                                twinId,
                                jwtToken
                        )
                )
                .map(activePolicy -> {

                    boolean locked =
                            activePolicy.active()
                                    && activePolicy.policyId()
                                    != null;

                    String explanation =
                            locked
                                    ? "Risk-sensitive configuration is "
                                    + "protected while active insurance "
                                    + "covers this AI Twin."
                                    : "No active policy currently locks "
                                    + "the AI Twin configuration.";

                    return new AiTwinConfigurationLockResponseDTO(
                            twinId,
                            locked,
                            activePolicy.policyId(),
                            activePolicy.status(),
                            activePolicy.startDate(),
                            activePolicy.endDate(),
                            explanation
                    );
                });
    }
}