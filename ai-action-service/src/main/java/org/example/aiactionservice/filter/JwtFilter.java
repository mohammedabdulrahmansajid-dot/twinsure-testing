package org.example.aiactionservice.filter;

// Reads the TwinSure JWT from the HttpOnly cookie or Bearer header.
// It creates the authenticated security context containing userId,
// customerId, role, and the original token for downstream service calls.

import org.example.aiactionservice.utility.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

public class JwtFilter implements WebFilter {

    private final JwtUtil jwtUtil;
    private final String cookieName;

    public JwtFilter(
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

        Long userId =
                jwtUtil.extractUserId(token);

        Long customerId =
                jwtUtil.extractCustomerId(token);

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

        authentication.setDetails(
                new JwtUserDetails(
                        userId,
                        customerId,
                        role,
                        token
                )
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

    public record JwtUserDetails(

            Long userId,
            Long customerId,
            String role,
            String token

    ) {
    }
}
