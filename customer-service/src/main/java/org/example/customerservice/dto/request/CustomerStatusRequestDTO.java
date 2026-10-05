package org.example.customerservice.dto.request;

import jakarta.validation.constraints.NotNull;
import org.example.customerservice.enums.CustomerStatus;

public record CustomerStatusRequestDTO(

        @NotNull(message = "Customer status is required")
        CustomerStatus status

) {
}