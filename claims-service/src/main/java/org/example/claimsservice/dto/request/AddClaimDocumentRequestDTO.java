package org.example.claimsservice.dto.request;

// Carries metadata for evidence submitted in support of a claim.
// The POC stores the document reference and description instead
// of uploading binary file content into the H2 database.

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.claimsservice.enums.DocumentType;

public record AddClaimDocumentRequestDTO(

        @NotNull(message = "Document type is required")
        DocumentType documentType,

        @NotBlank(message = "File name is required")
        @Size(
                max = 255,
                message = "File name must not exceed 255 characters"
        )
        String fileName,

        @NotBlank(message = "Document reference is required")
        @Size(
                max = 500,
                message = "Document reference must not exceed 500 characters"
        )
        String documentReference,

        @Size(
                max = 500,
                message = "Document description must not exceed 500 characters"
        )
        String description

) {
}