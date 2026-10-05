package org.example.identityservice.dto.request;

import jakarta.validation.constraints.NotNull;

public record CustomerLinkRequestDTO(

        @NotNull(message = "Customer ID is required")
        Long customerId

) {
}