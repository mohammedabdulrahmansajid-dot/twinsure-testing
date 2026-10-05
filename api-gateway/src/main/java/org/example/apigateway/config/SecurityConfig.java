package org.example.apigateway.config;

// Secures Gateway routes using JWT authentication and high-level role rules.
// Business microservices still perform their own detailed authorization,
// ownership validation, and workflow-state validation.

import org.example.apigateway.filter.GatewayJwtFilter;
import org.example.apigateway.utility.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

        GatewayJwtFilter jwtFilter =
                new GatewayJwtFilter(
                        jwtUtil,
                        cookieName
                );

        return http
                .csrf(
                        ServerHttpSecurity.CsrfSpec::disable
                )
                .cors(cors ->
                        cors.disable()
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
                                        HttpMethod.OPTIONS,
                                        "/**"
                                )
                                .permitAll()

                                .pathMatchers(
                                        "/api/auth/login",
                                        "/api/auth/register",
                                        "/api/auth/logout"
                                )
                                .permitAll()

                                .pathMatchers(
                                        "/actuator/health",
                                        "/actuator/info"
                                )
                                .permitAll()

                                .pathMatchers(
                                        "/actuator/metrics",
                                        "/actuator/metrics/**",
                                        "/actuator/gateway",
                                        "/actuator/gateway/**"
                                )
                                .hasRole("ADMIN")

                                .pathMatchers(
                                        "/api/admin/**"
                                )
                                .hasRole("ADMIN")

                                .pathMatchers(
                                        "/api/claims-adjuster/**",
                                        "/api/claims/ai-actions/**"
                                )
                                .hasRole("CLAIMS_ADJUSTER")

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