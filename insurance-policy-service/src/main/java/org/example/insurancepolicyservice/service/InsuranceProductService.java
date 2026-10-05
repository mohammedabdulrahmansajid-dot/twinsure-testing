package org.example.insurancepolicyservice.service;

// Manages insurance products, their coverages, exclusions, and lifecycle status.
// This service is needed so Admin can configure insurance plans and Customers
// can view only complete, active plans before submitting policy applications.

import org.example.insurancepolicyservice.dto.request.CreateInsuranceProductRequestDTO;
import org.example.insurancepolicyservice.dto.request.ProductCoverageRequestDTO;
import org.example.insurancepolicyservice.dto.request.ProductExclusionRequestDTO;
import org.example.insurancepolicyservice.dto.request.ProductStatusRequestDTO;
import org.example.insurancepolicyservice.dto.request.UpdateInsuranceProductRequestDTO;
import org.example.insurancepolicyservice.dto.response.InsuranceProductDetailsResponseDTO;
import org.example.insurancepolicyservice.dto.response.InsuranceProductResponseDTO;
import org.example.insurancepolicyservice.dto.response.ProductCoverageResponseDTO;
import org.example.insurancepolicyservice.dto.response.ProductExclusionResponseDTO;
import org.example.insurancepolicyservice.enums.ProductStatus;
import org.example.insurancepolicyservice.exception.DuplicateInsuranceProductException;
import org.example.insurancepolicyservice.exception.DuplicateProductCoverageException;
import org.example.insurancepolicyservice.exception.DuplicateProductExclusionException;
import org.example.insurancepolicyservice.exception.InsuranceProductNotFoundException;
import org.example.insurancepolicyservice.exception.InvalidRequestException;
import org.example.insurancepolicyservice.model.InsuranceProduct;
import org.example.insurancepolicyservice.model.ProductCoverage;
import org.example.insurancepolicyservice.model.ProductExclusion;
import org.example.insurancepolicyservice.repo.InsuranceProductRepo;
import org.example.insurancepolicyservice.repo.ProductCoverageRepo;
import org.example.insurancepolicyservice.repo.ProductExclusionRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;



@Service
public class InsuranceProductService {

    private final InsuranceProductRepo productRepo;
    private final ProductCoverageRepo coverageRepo;
    private final ProductExclusionRepo exclusionRepo;

    public InsuranceProductService(
            InsuranceProductRepo productRepo,
            ProductCoverageRepo coverageRepo,
            ProductExclusionRepo exclusionRepo) {

        this.productRepo = productRepo;
        this.coverageRepo = coverageRepo;
        this.exclusionRepo = exclusionRepo;
    }

    public Mono<InsuranceProductResponseDTO> createProduct(
            CreateInsuranceProductRequestDTO request) {

        validateFinancialValues(
                request.coverageLimit(),
                request.deductible()
        );

        String productCode =
                normalizeCode(request.productCode());

        return productRepo
                .existsByProductCode(productCode)

                .flatMap(productExists -> {

                    if (productExists) {
                        return Mono.error(
                                new DuplicateInsuranceProductException(
                                        "Insurance product already exists "
                                                + "with code: "
                                                + productCode
                                )
                        );
                    }

                    LocalDateTime currentTime =
                            LocalDateTime.now();

                    InsuranceProduct product =
                            new InsuranceProduct();

                    product.setProductCode(productCode);

                    product.setProductName(
                            request.productName().trim()
                    );

                    product.setDescription(
                            request.description().trim()
                    );

                    product.setBasePremium(
                            request.basePremium()
                    );

                    product.setCoverageLimit(
                            request.coverageLimit()
                    );

                    product.setDeductible(
                            request.deductible()
                    );

                    product.setStatus(
                            ProductStatus.DRAFT
                    );

                    product.setCreatedAt(currentTime);
                    product.setUpdatedAt(currentTime);

                    return productRepo.save(product);
                })

                .map(this::convertToProductResponse);
    }

    public Mono<InsuranceProductResponseDTO> updateProduct(
            Long productId,
            UpdateInsuranceProductRequestDTO request) {

        validateFinancialValues(
                request.coverageLimit(),
                request.deductible()
        );

        return findProductById(productId)

                .flatMap(product -> {

                    product.setProductName(
                            request.productName().trim()
                    );

                    product.setDescription(
                            request.description().trim()
                    );

                    product.setBasePremium(
                            request.basePremium()
                    );

                    product.setCoverageLimit(
                            request.coverageLimit()
                    );

                    product.setDeductible(
                            request.deductible()
                    );

                    product.setUpdatedAt(
                            LocalDateTime.now()
                    );

                    return productRepo.save(product);
                })

                .map(this::convertToProductResponse);
    }

    public Mono<InsuranceProductResponseDTO> updateProductStatus(
            Long productId,
            ProductStatusRequestDTO request) {

        return findProductById(productId)

                .flatMap(product -> {

                    if (product.getStatus()
                            == request.status()) {

                        return Mono.just(product);
                    }

                    if (request.status()
                            == ProductStatus.ACTIVE) {

                        return validateProductBeforeActivation(
                                product
                        );
                    }

                    product.setStatus(request.status());

                    product.setUpdatedAt(
                            LocalDateTime.now()
                    );

                    return productRepo.save(product);
                })

                .map(this::convertToProductResponse);
    }

    public Mono<ProductCoverageResponseDTO> addCoverage(
            Long productId,
            ProductCoverageRequestDTO request) {

        return findProductById(productId)

                .flatMap(product -> {

                    ensureProductCanBeConfigured(product);

                    validateCoverageLimit(
                            product,
                            request.coverageLimit()
                    );

                    return coverageRepo
                            .existsByProductIdAndActionTypeAndViolationType(
                                    productId,
                                    request.actionType(),
                                    request.violationType()
                            )

                            .flatMap(coverageExists -> {

                                if (coverageExists) {
                                    return Mono.error(
                                            new DuplicateProductCoverageException(
                                                    "Coverage already exists "
                                                            + "for action "
                                                            + request.actionType()
                                                            + " and violation "
                                                            + request.violationType()
                                            )
                                    );
                                }

                                ProductCoverage coverage =
                                        new ProductCoverage();

                                coverage.setProductId(productId);

                                coverage.setActionType(
                                        request.actionType()
                                );

                                coverage.setViolationType(
                                        request.violationType()
                                );

                                coverage.setCoverageLimit(
                                        request.coverageLimit()
                                );

                                coverage.setActive(true);

                                coverage.setDescription(
                                        request.description().trim()
                                );

                                return coverageRepo.save(
                                        coverage
                                );
                            });
                })

                .map(this::convertToCoverageResponse);
    }

    public Mono<ProductCoverageResponseDTO> updateCoverage(
            Long productId,
            ProductCoverageRequestDTO request) {

        return findProductById(productId)

                .flatMap(product -> {

                    ensureProductCanBeConfigured(product);

                    validateCoverageLimit(
                            product,
                            request.coverageLimit()
                    );

                    return coverageRepo
                            .findByProductIdAndActionTypeAndViolationType(
                                    productId,
                                    request.actionType(),
                                    request.violationType()
                            )

                            .switchIfEmpty(
                                    Mono.error(
                                            new org.example
                                                    .insurancepolicyservice
                                                    .exception
                                                    .ProductCoverageNotFoundException(
                                                    "Coverage not found for action "
                                                            + request.actionType()
                                                            + " and violation "
                                                            + request.violationType()
                                            )
                                    )
                            )

                            .flatMap(coverage -> {

                                coverage.setCoverageLimit(
                                        request.coverageLimit()
                                );

                                coverage.setDescription(
                                        request.description().trim()
                                );

                                coverage.setActive(true);

                                return coverageRepo.save(
                                        coverage
                                );
                            });
                })

                .map(this::convertToCoverageResponse);
    }

    public Mono<ProductExclusionResponseDTO> addExclusion(
            Long productId,
            ProductExclusionRequestDTO request) {

        return findProductById(productId)

                .flatMap(product -> {

                    ensureProductCanBeConfigured(product);

                    String exclusionCode =
                            normalizeCode(
                                    request.exclusionCode()
                            );

                    return exclusionRepo
                            .existsByProductIdAndExclusionCode(
                                    productId,
                                    exclusionCode
                            )

                            .flatMap(exclusionExists -> {

                                if (exclusionExists) {
                                    return Mono.error(
                                            new DuplicateProductExclusionException(
                                                    "Product exclusion already "
                                                            + "exists with code: "
                                                            + exclusionCode
                                            )
                                    );
                                }

                                ProductExclusion exclusion =
                                        new ProductExclusion();

                                exclusion.setProductId(productId);

                                exclusion.setExclusionCode(
                                        exclusionCode
                                );

                                exclusion.setDescription(
                                        request.description().trim()
                                );

                                exclusion.setActive(true);

                                return exclusionRepo.save(
                                        exclusion
                                );
                            });
                })

                .map(this::convertToExclusionResponse);
    }

    public Mono<ProductExclusionResponseDTO> updateExclusion(
            Long productId,
            ProductExclusionRequestDTO request) {

        return findProductById(productId)

                .flatMap(product -> {

                    ensureProductCanBeConfigured(product);

                    String exclusionCode =
                            normalizeCode(
                                    request.exclusionCode()
                            );

                    return exclusionRepo
                            .findByProductIdAndExclusionCode(
                                    productId,
                                    exclusionCode
                            )

                            .switchIfEmpty(
                                    Mono.error(
                                            new org.example
                                                    .insurancepolicyservice
                                                    .exception
                                                    .ProductExclusionNotFoundException(
                                                    "Product exclusion not found "
                                                            + "with code: "
                                                            + exclusionCode
                                            )
                                    )
                            )

                            .flatMap(exclusion -> {

                                exclusion.setDescription(
                                        request.description().trim()
                                );

                                exclusion.setActive(true);

                                return exclusionRepo.save(
                                        exclusion
                                );
                            });
                })

                .map(this::convertToExclusionResponse);
    }

    public Flux<InsuranceProductResponseDTO> getActiveProducts() {

        return productRepo
                .findAllByStatus(ProductStatus.ACTIVE)
                .map(this::convertToProductResponse);
    }

    public Mono<InsuranceProductDetailsResponseDTO>
    getActiveProductDetails(Long productId) {

        return findProductById(productId)

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

                    return buildProductDetails(
                            product,
                            true
                    );
                });
    }

    public Flux<InsuranceProductResponseDTO> getAllProducts() {

        return productRepo
                .findAll()
                .map(this::convertToProductResponse);
    }

    public Mono<InsuranceProductDetailsResponseDTO>
    getProductDetailsForAdmin(Long productId) {

        return findProductById(productId)
                .flatMap(product ->
                        buildProductDetails(
                                product,
                                false
                        )
                );
    }

    private Mono<InsuranceProduct> validateProductBeforeActivation(
            InsuranceProduct product) {

        return coverageRepo
                .findAllByProductIdAndActiveTrue(
                        product.getProductId()
                )
                .hasElements()

                .flatMap(hasActiveCoverage -> {

                    if (!hasActiveCoverage) {
                        return Mono.error(
                                new InvalidRequestException(
                                        "At least one active coverage "
                                                + "is required before activating "
                                                + "an insurance product"
                                )
                        );
                    }

                    product.setStatus(
                            ProductStatus.ACTIVE
                    );

                    product.setUpdatedAt(
                            LocalDateTime.now()
                    );

                    return productRepo.save(product);
                });
    }

    private Mono<InsuranceProductDetailsResponseDTO>
    buildProductDetails(
            InsuranceProduct product,
            boolean activeOnly) {

        Flux<ProductCoverage> coverages;

        Flux<ProductExclusion> exclusions;

        if (activeOnly) {

            coverages =
                    coverageRepo
                            .findAllByProductIdAndActiveTrue(
                                    product.getProductId()
                            );

            exclusions =
                    exclusionRepo
                            .findAllByProductIdAndActiveTrue(
                                    product.getProductId()
                            );

        } else {

            coverages =
                    coverageRepo
                            .findAllByProductId(
                                    product.getProductId()
                            );

            exclusions =
                    exclusionRepo
                            .findAllByProductId(
                                    product.getProductId()
                            );
        }

        return Mono.zip(
                        coverages
                                .map(
                                        this::convertToCoverageResponse
                                )
                                .collectList(),

                        exclusions
                                .map(
                                        this::convertToExclusionResponse
                                )
                                .collectList()
                )

                .map(result ->
                        new InsuranceProductDetailsResponseDTO(
                                product.getProductId(),
                                product.getProductCode(),
                                product.getProductName(),
                                product.getDescription(),
                                product.getBasePremium(),
                                product.getCoverageLimit(),
                                product.getDeductible(),
                                product.getStatus(),
                                result.getT1(),
                                result.getT2(),
                                product.getCreatedAt(),
                                product.getUpdatedAt()
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

    private void validateFinancialValues(
            BigDecimal coverageLimit,
            BigDecimal deductible) {

        if (deductible.compareTo(coverageLimit) > 0) {
            throw new InvalidRequestException(
                    "Deductible cannot be greater "
                            + "than the coverage limit"
            );
        }
    }

    private void validateCoverageLimit(
            InsuranceProduct product,
            BigDecimal coverageLimit) {

        if (coverageLimit == null) {
            return;
        }

        if (coverageLimit.compareTo(
                product.getCoverageLimit()
        ) > 0) {

            throw new InvalidRequestException(
                    "Coverage-specific limit cannot be greater "
                            + "than the product coverage limit"
            );
        }
    }

    private void ensureProductCanBeConfigured(
            InsuranceProduct product) {

        if (product.getStatus()
                == ProductStatus.ACTIVE) {

            throw new InvalidRequestException(
                    "An ACTIVE insurance product cannot have "
                            + "its coverage configuration changed. "
                            + "Set the product to INACTIVE first."
            );
        }
    }

    private String normalizeCode(
            String value) {

        return value
                .trim()
                .toUpperCase()
                .replace(" ", "_");
    }

    private InsuranceProductResponseDTO
    convertToProductResponse(
            InsuranceProduct product) {

        return new InsuranceProductResponseDTO(
                product.getProductId(),
                product.getProductCode(),
                product.getProductName(),
                product.getDescription(),
                product.getBasePremium(),
                product.getCoverageLimit(),
                product.getDeductible(),
                product.getStatus()
        );
    }

    private ProductCoverageResponseDTO
    convertToCoverageResponse(
            ProductCoverage coverage) {

        return new ProductCoverageResponseDTO(
                coverage.getCoverageId(),
                coverage.getProductId(),
                coverage.getActionType(),
                coverage.getViolationType(),
                coverage.getCoverageLimit(),
                coverage.getActive(),
                coverage.getDescription()
        );
    }

    private ProductExclusionResponseDTO
    convertToExclusionResponse(
            ProductExclusion exclusion) {

        return new ProductExclusionResponseDTO(
                exclusion.getExclusionId(),
                exclusion.getProductId(),
                exclusion.getExclusionCode(),
                exclusion.getDescription(),
                exclusion.getActive()
        );
    }
}