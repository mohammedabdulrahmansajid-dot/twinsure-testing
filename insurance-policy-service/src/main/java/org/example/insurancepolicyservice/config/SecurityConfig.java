package org.example.insurancepolicyservice.config;

// Defines role-based access rules for products, applications, and policies.
// Customer resubmission is explicitly protected before the general
// application-details matcher.

import org.example.insurancepolicyservice.filter.JwtFilter;
import org.example.insurancepolicyservice.utility.JwtUtil;
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

                                .pathMatchers(
                                        "/actuator/health",
                                        "/actuator/info"
                                )
                                .permitAll()

                                .pathMatchers(
                                        "/actuator/metrics/**"
                                )
                                .hasRole("ADMIN")

                                .pathMatchers(
                                        "/api/insurance-products/**"
                                )
                                .hasAnyRole(
                                        "CUSTOMER",
                                        "UNDERWRITER",
                                        "CLAIMS_ADJUSTER",
                                        "ADMIN"
                                )

                                .pathMatchers(
                                        "/api/policy-applications/my"
                                )
                                .hasRole("CUSTOMER")

                                .pathMatchers(
                                        "/api/policy-applications/pending",
                                        "/api/policy-applications/reviewed-by-me"
                                )
                                .hasRole("UNDERWRITER")

                                .pathMatchers(
                                        "/api/policy-applications"
                                )
                                .hasRole("CUSTOMER")

                                .pathMatchers(
                                        "/api/policy-applications/*/assessment",
                                        "/api/policy-applications/*/request-changes",
                                        "/api/policy-applications/*/approve",
                                        "/api/policy-applications/*/reject"
                                )
                                .hasRole("UNDERWRITER")


                                .pathMatchers(
                                        "/api/policy-applications/*/resubmit",
                                        "/api/policy-applications/*/accept",
                                        "/api/policy-applications/*/decline"
                                )
                                .hasRole("CUSTOMER")


                                .pathMatchers(
                                        "/api/policy-applications/*"
                                )
                                .hasAnyRole(
                                        "CUSTOMER",
                                        "UNDERWRITER",
                                        "ADMIN"
                                )

                                .pathMatchers(
                                        "/api/policies/my"
                                )
                                .hasRole("CUSTOMER")

                                .pathMatchers(
                                        "/api/policies/*/coverage"
                                )
                                .hasAnyRole(
                                        "CLAIMS_ADJUSTER",
                                        "ADMIN"
                                )

                                .pathMatchers(
                                        "/api/policies/*"
                                )
                                .hasAnyRole(
                                        "CUSTOMER",
                                        "UNDERWRITER",
                                        "ADMIN"
                                )

                                .pathMatchers(
                                        "/api/admin/insurance-products/**",
                                        "/api/admin/policy-applications/**",
                                        "/api/admin/policies/**"
                                )
                                .hasRole("ADMIN")

                                .pathMatchers(
                                        "/internal/policies/**",
                                        "/internal/policy-reviews/**"
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