package org.example.insurancepolicyservice.dto.request;

import jakarta.validation.constraints.NotNull;
import org.example.insurancepolicyservice.enums.ProductStatus;

public record ProductStatusRequestDTO(

        @NotNull(message = "Product status is required")
        ProductStatus status

) {
}