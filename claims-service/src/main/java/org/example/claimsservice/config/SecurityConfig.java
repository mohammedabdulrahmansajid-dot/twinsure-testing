package org.example.claimsservice.config;

// Defines role-based access rules for incidents, claims, evidence,
// Adjuster review context, workflow decisions, and Admin operations.
// JWT authentication supports the TwinSure cookie and Bearer fallback.

import org.example.claimsservice.filter.JwtFilter;
import org.example.claimsservice.utility.JwtUtil;
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
    public SecurityWebFilterChain
    securityWebFilterChain(
            ServerHttpSecurity http,
            JwtUtil jwtUtil,
            @Value("${jwt.cookie.name}")
            String cookieName) {

        JwtFilter jwtFilter =
                new JwtFilter(
                        jwtUtil,
                        cookieName
                );

        return http
                .csrf(
                        ServerHttpSecurity
                                .CsrfSpec::disable
                )
                .httpBasic(
                        ServerHttpSecurity
                                .HttpBasicSpec::disable
                )
                .formLogin(
                        ServerHttpSecurity
                                .FormLoginSpec::disable
                )
                .logout(
                        ServerHttpSecurity
                                .LogoutSpec::disable
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
                                        "/api/incidents",
                                        "/api/incidents/my",
                                        "/api/incidents/*"
                                )
                                .hasRole("CUSTOMER")

                                .pathMatchers(
                                        "/api/claims",
                                        "/api/claims/my"
                                )
                                .hasRole("CUSTOMER")

                                .pathMatchers(
                                        HttpMethod.POST,
                                        "/api/claims/*/documents"
                                )
                                .hasRole("CUSTOMER")

                                .pathMatchers(
                                        HttpMethod.GET,
                                        "/api/claims/*/documents"
                                )
                                .hasAnyRole(
                                        "CUSTOMER",
                                        "CLAIMS_ADJUSTER",
                                        "ADMIN"
                                )

                                .pathMatchers(
                                        HttpMethod.GET,
                                        "/api/claims-adjuster/claims/"
                                                + "*/review-context"
                                )
                                .hasRole(
                                        "CLAIMS_ADJUSTER"
                                )

                                .pathMatchers(
                                        "/api/claims-adjuster/claims/**"
                                )
                                .hasRole(
                                        "CLAIMS_ADJUSTER"
                                )

                                .pathMatchers(
                                        "/api/admin/claims/**",
                                        "/api/admin/incidents/**"
                                )
                                .hasRole("ADMIN")

                                .pathMatchers(
                                        "/api/claims/*"
                                )
                                .hasAnyRole(
                                        "CUSTOMER",
                                        "CLAIMS_ADJUSTER",
                                        "ADMIN"
                                )

                                .pathMatchers(
                                        "/internal/claims/**",
                                        "/internal/incidents/**"
                                )

                                .authenticated()
                                .pathMatchers("/h2-console").permitAll()
                                .anyExchange()
                                .authenticated()
                )
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint(
                                        (exchange, exception) -> {

                                            exchange
                                                    .getResponse()
                                                    .setStatusCode(
                                                            HttpStatus
                                                                    .UNAUTHORIZED
                                                    );

                                            return exchange
                                                    .getResponse()
                                                    .setComplete();
                                        }
                                )
                                .accessDeniedHandler(
                                        (exchange, exception) -> {

                                            exchange
                                                    .getResponse()
                                                    .setStatusCode(
                                                            HttpStatus
                                                                    .FORBIDDEN
                                                    );

                                            return exchange
                                                    .getResponse()
                                                    .setComplete();
                                        }
                                )
                )
                .addFilterAt(
                        jwtFilter,
                        SecurityWebFiltersOrder
                                .AUTHENTICATION
                )
                .build();
    }
}