package org.example.aitwinservice.config;

import org.example.aitwinservice.filter.JwtFilter;
import org.example.aitwinservice.utility.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            JwtUtil jwtUtil,
            @Value("${jwt.cookie.name}") String cookieName) {

        JwtFilter jwtFilter =
                new JwtFilter(
                        jwtUtil,
                        cookieName
                );

        return http
                .csrf(
                        ServerHttpSecurity.CsrfSpec::disable
                )

                .httpBasic(
                        ServerHttpSecurity.HttpBasicSpec::disable
                )

                .formLogin(
                        ServerHttpSecurity.FormLoginSpec::disable
                )

                .logout(
                        ServerHttpSecurity.LogoutSpec::disable
                )

                .authorizeExchange(authorize ->
                        authorize

                                // Customer-owned AI Twin operations
                                .pathMatchers(
                                        "/api/ai-twins",
                                        "/api/ai-twins/my",
                                        "/api/ai-twins/*",
                                        "/api/ai-twins/*/permissions"
                                )
                                .hasRole("CUSTOMER")

                                // Underwriting risk view
                                .pathMatchers(
                                        "/api/ai-twins/*/risk-profile"
                                )
                                .hasAnyRole(
                                        "UNDERWRITER",
                                        "ADMIN"
                                )

                                // Claims Adjuster view
                                .pathMatchers(
                                        "/api/ai-twins/*/claim-summary"
                                )
                                .hasAnyRole(
                                        "CLAIMS_ADJUSTER",
                                        "ADMIN"
                                )

                                // Admin AI Twin operations
                                .pathMatchers(
                                        "/api/admin/ai-twins/**"
                                )
                                .hasRole("ADMIN")

                                // Service-to-service endpoints
                                .pathMatchers(
                                        "/internal/ai-twins/**"
                                )
                                .authenticated()

                                .anyExchange()
                                .authenticated()
                )

                .exceptionHandling(exceptionHandling ->
                        exceptionHandling

                                .authenticationEntryPoint(
                                        (exchange, exception) -> {

                                            exchange.getResponse()
                                                    .setStatusCode(
                                                            HttpStatus.UNAUTHORIZED
                                                    );

                                            return exchange
                                                    .getResponse()
                                                    .setComplete();
                                        }
                                )

                                .accessDeniedHandler(
                                        (exchange, exception) -> {

                                            exchange.getResponse()
                                                    .setStatusCode(
                                                            HttpStatus.FORBIDDEN
                                                    );

                                            return exchange
                                                    .getResponse()
                                                    .setComplete();
                                        }
                                )
                )

                .addFilterAt(
                        jwtFilter,
                        SecurityWebFiltersOrder.AUTHENTICATION
                )

                .build();
    }
}