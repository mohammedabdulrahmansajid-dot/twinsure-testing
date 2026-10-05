package org.example.aiactionservice.config;

// Defines endpoint access rules and installs JWT authentication.
// Customers simulate and view owned actions, staff review relevant actions,
// and Admins access monitoring and complete action information.

import org.example.aiactionservice.filter.JwtFilter;
import org.example.aiactionservice.utility.JwtUtil;
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
                                        "/actuator/metrics",
                                        "/actuator/metrics/**"
                                )
                                .hasRole("ADMIN")

                                .pathMatchers(
                                        "/api/ai-actions/simulate",
                                        "/api/ai-actions/my",
                                        "/api/ai-actions/*"
                                )
                                .hasRole("CUSTOMER")

                                .pathMatchers(
                                        "/api/admin/ai-actions/**"
                                )
                                .hasRole("ADMIN")

                                .pathMatchers(
                                        "/api/claims/ai-actions/**"
                                )
                                .hasAnyRole(
                                        "CLAIMS_ADJUSTER",
                                        "ADMIN"
                                )

                                .pathMatchers(
                                        "/internal/ai-actions/**"
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

                                            return exchange.getResponse()
                                                    .setComplete();
                                        }
                                )

                                .accessDeniedHandler(
                                        (exchange, exception) -> {

                                            exchange.getResponse()
                                                    .setStatusCode(
                                                            HttpStatus.FORBIDDEN
                                                    );

                                            return exchange.getResponse()
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