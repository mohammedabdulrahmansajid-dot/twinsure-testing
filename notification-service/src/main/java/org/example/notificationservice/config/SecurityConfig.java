package org.example.notificationservice.config;

// Defines access rules for user, Admin, internal, and Actuator endpoints.
// It installs JWT authentication using the TwinSure HttpOnly cookie
// with Bearer-token fallback for internal and manual requests.

import org.example.notificationservice.filter.JwtFilter;
import org.example.notificationservice.utility.JwtUtil;
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
                                        "/api/admin/notifications"
                                )
                                .hasRole("ADMIN")
                                .pathMatchers(
                                        "/internal/notifications"
                                )
                                .authenticated()
                                .pathMatchers(
                                        "/api/notifications/my",
                                        "/api/notifications/my/**",
                                        "/api/notifications/*/read"
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
