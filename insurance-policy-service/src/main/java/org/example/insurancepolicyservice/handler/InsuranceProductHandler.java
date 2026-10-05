package org.example.insurancepolicyservice.handler;

// Handles HTTP requests for insurance products, coverages, and exclusions.
// This file connects functional routes to InsuranceProductService and validates request bodies.

import org.example.insurancepolicyservice.dto.request.CreateInsuranceProductRequestDTO;
import org.example.insurancepolicyservice.dto.request.ProductCoverageRequestDTO;
import org.example.insurancepolicyservice.dto.request.ProductExclusionRequestDTO;
import org.example.insurancepolicyservice.dto.request.ProductStatusRequestDTO;
import org.example.insurancepolicyservice.dto.request.UpdateInsuranceProductRequestDTO;
import org.example.insurancepolicyservice.exception.InvalidRequestException;
import org.example.insurancepolicyservice.service.InsuranceProductService;
import org.example.insurancepolicyservice.utility.RequestValidator;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class InsuranceProductHandler {

    private final InsuranceProductService productService;
    private final RequestValidator requestValidator;

    public InsuranceProductHandler(
            InsuranceProductService productService,
            RequestValidator requestValidator) {

        this.productService = productService;
        this.requestValidator = requestValidator;
    }

    public Mono<ServerResponse> createProduct(
            ServerRequest serverRequest) {

        return readAndValidateBody(
                serverRequest,
                CreateInsuranceProductRequestDTO.class
        )
                .flatMap(productService::createProduct)
                .flatMap(response ->
                        ServerResponse
                                .status(201)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .bodyValue(response)
                );
    }

    public Mono<ServerResponse> getActiveProducts(
            ServerRequest serverRequest) {

        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        productService.getActiveProducts(),
                        Object.class
                );
    }

    public Mono<ServerResponse> getActiveProductDetails(
            ServerRequest serverRequest) {

        Long productId =
                getLongPathVariable(
                        serverRequest,
                        "productId"
                );

        return productService
                .getActiveProductDetails(productId)
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .bodyValue(response)
                );
    }

    public Mono<ServerResponse> getAllProducts(
            ServerRequest serverRequest) {

        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        productService.getAllProducts(),
                        Object.class
                );
    }

    public Mono<ServerResponse> getProductDetailsForAdmin(
            ServerRequest serverRequest) {

        Long productId =
                getLongPathVariable(
                        serverRequest,
                        "productId"
                );

        return productService
                .getProductDetailsForAdmin(productId)
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .bodyValue(response)
                );
    }

    public Mono<ServerResponse> updateProduct(
            ServerRequest serverRequest) {

        Long productId =
                getLongPathVariable(
                        serverRequest,
                        "productId"
                );

        return readAndValidateBody(
                serverRequest,
                UpdateInsuranceProductRequestDTO.class
        )
                .flatMap(request ->
                        productService.updateProduct(
                                productId,
                                request
                        )
                )
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .bodyValue(response)
                );
    }

    public Mono<ServerResponse> updateProductStatus(
            ServerRequest serverRequest) {

        Long productId =
                getLongPathVariable(
                        serverRequest,
                        "productId"
                );

        return readAndValidateBody(
                serverRequest,
                ProductStatusRequestDTO.class
        )
                .flatMap(request ->
                        productService.updateProductStatus(
                                productId,
                                request
                        )
                )
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .bodyValue(response)
                );
    }

    public Mono<ServerResponse> addCoverage(
            ServerRequest serverRequest) {

        Long productId =
                getLongPathVariable(
                        serverRequest,
                        "productId"
                );

        return readAndValidateBody(
                serverRequest,
                ProductCoverageRequestDTO.class
        )
                .flatMap(request ->
                        productService.addCoverage(
                                productId,
                                request
                        )
                )
                .flatMap(response ->
                        ServerResponse
                                .status(201)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .bodyValue(response)
                );
    }

    public Mono<ServerResponse> updateCoverage(
            ServerRequest serverRequest) {

        Long productId =
                getLongPathVariable(
                        serverRequest,
                        "productId"
                );

        return readAndValidateBody(
                serverRequest,
                ProductCoverageRequestDTO.class
        )
                .flatMap(request ->
                        productService.updateCoverage(
                                productId,
                                request
                        )
                )
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .bodyValue(response)
                );
    }

    public Mono<ServerResponse> addExclusion(
            ServerRequest serverRequest) {

        Long productId =
                getLongPathVariable(
                        serverRequest,
                        "productId"
                );

        return readAndValidateBody(
                serverRequest,
                ProductExclusionRequestDTO.class
        )
                .flatMap(request ->
                        productService.addExclusion(
                                productId,
                                request
                        )
                )
                .flatMap(response ->
                        ServerResponse
                                .status(201)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .bodyValue(response)
                );
    }

    public Mono<ServerResponse> updateExclusion(
            ServerRequest serverRequest) {

        Long productId =
                getLongPathVariable(
                        serverRequest,
                        "productId"
                );

        return readAndValidateBody(
                serverRequest,
                ProductExclusionRequestDTO.class
        )
                .flatMap(request ->
                        productService.updateExclusion(
                                productId,
                                request
                        )
                )
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .bodyValue(response)
                );
    }

    private <T> Mono<T> readAndValidateBody(
            ServerRequest serverRequest,
            Class<T> requestType) {

        return serverRequest
                .bodyToMono(requestType)
                .switchIfEmpty(
                        Mono.error(
                                new InvalidRequestException(
                                        "Request body is required"
                                )
                        )
                )
                .flatMap(requestValidator::validate);
    }

    private Long getLongPathVariable(
            ServerRequest serverRequest,
            String variableName) {

        String value =
                serverRequest.pathVariable(variableName);

        try {

            return Long.valueOf(value);

        } catch (NumberFormatException exception) {

            throw new InvalidRequestException(
                    variableName
                            + " must be a valid number"
            );
        }
    }
}