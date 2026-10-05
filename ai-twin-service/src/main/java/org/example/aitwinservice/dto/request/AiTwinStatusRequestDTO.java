package org.example.aitwinservice.dto.request;

import jakarta.validation.constraints.NotNull;
import org.example.aitwinservice.enums.AiTwinStatus;

public record AiTwinStatusRequestDTO(

        @NotNull(message = "AI Twin status is required")
        AiTwinStatus status

) {
}