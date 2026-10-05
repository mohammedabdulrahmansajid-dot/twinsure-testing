package org.example.insurancepolicyservice.router;

// Defines all functional routes for insurance-product management.
// Public authenticated users view active plans, while Admin configures all product details.

import org.example.insurancepolicyservice.handler.InsuranceProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class InsuranceProductRouter {

    @Bean
    public RouterFunction<ServerResponse> insuranceProductRoutes(
            InsuranceProductHandler handler) {

        return route()

                // Active product catalogue
                .GET(
                        "/api/insurance-products",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getActiveProducts
                )

                .GET(
                        "/api/insurance-products/{productId}",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getActiveProductDetails
                )

                // Admin creates and views products
                .POST(
                        "/api/admin/insurance-products",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::createProduct
                )

                .GET(
                        "/api/admin/insurance-products",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getAllProducts
                )

                .GET(
                        "/api/admin/insurance-products/{productId}",
                        accept(MediaType.APPLICATION_JSON),
                        handler::getProductDetailsForAdmin
                )

                // Admin updates product and status
                .PUT(
                        "/api/admin/insurance-products/{productId}",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::updateProduct
                )

                .PUT(
                        "/api/admin/insurance-products/{productId}/status",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::updateProductStatus
                )

                // Admin creates and updates coverage rules
                .POST(
                        "/api/admin/insurance-products/{productId}/coverages",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::addCoverage
                )

                .PUT(
                        "/api/admin/insurance-products/{productId}/coverages",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::updateCoverage
                )

                // Admin creates and updates exclusions
                .POST(
                        "/api/admin/insurance-products/{productId}/exclusions",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::addExclusion
                )

                .PUT(
                        "/api/admin/insurance-products/{productId}/exclusions",
                        accept(MediaType.APPLICATION_JSON)
                                .and(
                                        contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                ),
                        handler::updateExclusion
                )

                .build();
    }
}