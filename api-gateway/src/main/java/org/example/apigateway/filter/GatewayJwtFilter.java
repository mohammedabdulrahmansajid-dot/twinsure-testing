package org.example.apigateway.filter;

// Reads the JWT from the Bearer header or TWINSURE_TOKEN HttpOnly cookie.
// It creates the authenticated reactive security context used by Gateway
// security rules while preserving the original request for downstream services.

import org.example.apigateway.utility.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

public class GatewayJwtFilter implements WebFilter {

    private final JwtUtil jwtUtil;
    private final String cookieName;

    public GatewayJwtFilter(
            JwtUtil jwtUtil,
            String cookieName) {

        this.jwtUtil = jwtUtil;
        this.cookieName = cookieName;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            WebFilterChain chain) {

        String token =
                extractToken(exchange);

        if (token == null
                || !jwtUtil.isTokenValid(token)) {

            return chain.filter(exchange);
        }

        String username =
                jwtUtil.extractUsername(token);

        String role =
                jwtUtil.extractRole(token);

        if (username == null
                || role == null) {

            return chain.filter(exchange);
        }

        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(
                        "ROLE_" + role
                );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of(authority)
                );

        return chain.filter(exchange)
                .contextWrite(
                        ReactiveSecurityContextHolder
                                .withAuthentication(
                                        authentication
                                )
                );
    }

    private String extractToken(
            ServerWebExchange exchange) {

        String authorizationHeader =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(
                                HttpHeaders.AUTHORIZATION
                        );

        if (authorizationHeader != null
                && authorizationHeader.startsWith(
                "Bearer "
        )) {

            return authorizationHeader.substring(7);
        }

        var authenticationCookie =
                exchange.getRequest()
                        .getCookies()
                        .getFirst(cookieName);

        if (authenticationCookie != null) {
            return authenticationCookie.getValue();
        }

        return null;
    }
}