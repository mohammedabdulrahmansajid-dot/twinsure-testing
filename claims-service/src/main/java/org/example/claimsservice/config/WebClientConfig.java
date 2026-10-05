package org.example.claimsservice.config;

// Creates a load-balanced WebClient builder for service-to-service calls.
// Claims Service uses Eureka service names to call Customer, AI Twin,
// AI Action, Insurance Policy, and Identity services.

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {

        return WebClient.builder();
    }
}