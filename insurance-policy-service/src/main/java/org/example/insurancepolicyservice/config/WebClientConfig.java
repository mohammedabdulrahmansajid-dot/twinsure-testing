package org.example.insurancepolicyservice.config;

// Creates a load-balanced WebClient builder for inter-service communication.
// Insurance Service uses it to call Customer Service and AI Twin Service through Eureka.

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