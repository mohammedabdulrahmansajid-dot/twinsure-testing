package org.example.insurancepolicyservice.dto.response;

// Combines the complete application with its selected insurance product.
// It gives the Customer or Underwriter enough context to review the application.

public record PolicyApplicationDetailsResponseDTO(

        PolicyApplicationResponseDTO application,
        InsuranceProductDetailsResponseDTO product

) {
}