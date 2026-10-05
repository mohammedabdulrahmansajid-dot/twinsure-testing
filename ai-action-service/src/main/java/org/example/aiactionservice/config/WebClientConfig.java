package org.example.aiactionservice.config;

// Creates a load-balanced WebClient used for service-to-service requests.
// AI Action Service calls AI Twin Service and Insurance Policy Service
// through Eureka service names instead of fixed host and port values.

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